package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class Armazem {
    
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    public Map<String,Entry> armazem;
    
    //Classe da Entry
    private class Entry {
    
        protected ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        protected Condition cond = l.writeLock().newCondition(); //ReadLock porque é getWhen, e não putWhen
        private byte[] dados;

        //private LocalDateTime ultima_modificacao;

        //Construtor
        private Entry(byte[] arg) {

            if (arg != null)
                this.dados = arg.clone();
            else
                this.dados = null;
            //this.ultima_modificacao = LocalDateTime.now();

        }

        private Entry() {

        }

        //Obtem os dados
        protected byte[] get() {
            return this.dados;
        }

        //Coloca os dados
        protected void set(byte[] arg) {
            this.dados = arg.clone();
            //this.ultima_modificacao = LocalDateTime.now();
            this.cond.signalAll();
        }

    }

    //Construtor
    public Armazem() {

        this.armazem = new HashMap<>();

    }

    //Result : coloca um value no armazem
    public void put(String key, byte[] value) {

        this.l.writeLock().lock();

        //Obtem a entry
        Entry e = this.armazem.get(key);

        //Se entry não existe, cria uma entry vazia
        if (e == null) {
            e = new Entry();
            this.armazem.put(key,e);
        }

        e.l.writeLock().lock();
        this.l.writeLock().unlock();

        e.set(value);
        e.l.writeLock().unlock();

    }

    //Result : coloca os values no armazem
    public void multiPut(Map<String,byte[]> pairs) {

        Set<Map.Entry<String,byte[]>> entriesPairs = pairs.entrySet();
        Map<String,Entry> entries = new HashMap<>();

        this.l.writeLock().lock();

        //Coletar todos os locks das entries
        for (Map.Entry<String,byte[]> e : entriesPairs) {

            String eKey = e.getKey();
            Entry entry = this.armazem.get(eKey);

            //Se entry não existe, cria uma entry vazia
            if (entry == null) {
                entry = new Entry();
                this.armazem.put(eKey,entry);
            }

            entries.put(eKey,entry);
            entry.l.writeLock().lock();

        }

        this.l.writeLock().unlock();

        //Fazer os puts das entries
        for (Map.Entry<String,byte[]> e : entriesPairs) {

            String eKey = e.getKey();
            byte[] b = e.getValue();
            Entry entry = entries.get(eKey);

            entry.set(b);
            entry.l.writeLock().unlock();

        }

    }

    //Result : retorna o value que está associado à key no armazem
    public byte[] get(String key) {

        byte[] res = null;

        this.l.readLock().lock();

        //Obtem a entry
        Entry e = this.armazem.get(key);

        //Se entry existe, obtem dado
        if (e != null) {

            e.l.readLock().lock();
            this.l.readLock().unlock();
            if (e.get() != null)
                res = e.get().clone();
            e.l.readLock().unlock();
        }
        //Entry não existe
        else {
            this.l.readLock().unlock();
        }

        return res;

    }

    //Result : retorna os values que estão associados às keys no armazem
    public Map<String,byte[]> multiGet(Set<String> keys) {

        Map<String,byte[]> res = new HashMap<>();
        Map<String,Entry> entries = new HashMap<>();

        this.l.readLock().lock();

        //Coletar todos os locks das entries
        for (String e : keys) {

            Entry entry = this.armazem.get(e);
            entries.put(e,entry);

            if (entry != null)
                entry.l.readLock().lock();

        }

        this.l.readLock().unlock();

        //Coletar os values das entries
        for (Map.Entry<String,Entry> e : entries.entrySet()) {

            String eKey = e.getKey();
            byte[] b = null;
            Entry entry = e.getValue();

            //Entry existe
            if (entry != null) {

                b = entry.get().clone();
                entry.l.readLock().unlock();

            }
            
            res.put(eKey,b);

        }

        return res;

    }

    //Result : retorna o value da key se o value do keyCond for igual ao keyCond
    //Aviso : método bloqueante!
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {

        byte[] res = null;

        this.l.writeLock().lock();

        //Obtem as entries
        Entry eCond = this.armazem.get(keyCond);
        Entry e = this.armazem.get(key);

        //Entry condicional não existe
        if (eCond == null) {
            eCond = new Entry(null);
            this.armazem.put(keyCond,eCond);
        } 

        //Entry da key não existe
        if (e == null) {
            e = new Entry(null);
            this.armazem.put(key,e);                
        }

        Stream.of(key,keyCond).sorted().forEach(n -> this.armazem.get(n).l.writeLock().lock());

        res = eCond.get();
        if (Armazem.equals(res,valueCond) == true) {

            res = e.get();

            this.l.writeLock().unlock();
            Stream.of(key,keyCond).sorted().forEach(n -> this.armazem.get(n).l.writeLock().unlock());

            return res;

        }
        else {

            e.l.writeLock().unlock();
            this.l.writeLock().unlock();

            while(Armazem.equals(eCond.get(),valueCond) == false)
                eCond.cond.await();

            e.l.writeLock().lock();
            eCond.l.writeLock().unlock();

            res = e.get();
            e.l.writeLock().unlock();
        

        }

        return res;

    }

    private static boolean equals(byte[] a, byte[] b) {
        
        if (a == null && b == null)
        return true;
        
        if (a == null || b == null)
        return false;
                
        if (a.length != b.length)
            return false;

        for (int i = 0 ; i < a.length ; i++) {

            if (a[i] != b[i])
                return false;

        }

        return true;

    }


}
