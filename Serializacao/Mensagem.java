package Serialização;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mensagem {

    private byte tipo;
    /*
     * r = 1
     * a = 2
     * p = 3
     * g = 4
     * mp = 5
     * mg = 6
     * gw = 7
     */
    private List<Object> lista;

    private Mensagem(int tipo) {
        this.tipo = (byte) tipo;
        this.lista = new ArrayList<>();
    } 

    //Método que cria uma mensagem de registo
    public static Mensagem registo(String nome, String password) {

        Mensagem s = new Mensagem(1);
        s.lista.add(nome);
        s.lista.add(password);

        return s;

    }

    //Método que cria uma mensagem de registo
    public static Mensagem autenticacao(String nome, String password) {

        Mensagem s = new Mensagem(2);
        s.lista.add(nome);
        s.lista.add(password);

        return s;

    }

    //Método que cria uma mensagem de put
    public static Mensagem put(String key, byte[] value) {

        Mensagem s = new Mensagem(3);
        s.lista.add(key);
        s.lista.add(value.clone());

        return s;

    }

    //Método que cria uma mensagem de get
    public static Mensagem get(String key) {

        Mensagem s = new Mensagem(4);
        s.lista.add(key);

        return s;

    }

    //Método que cria uma mensagem de multi put
    public static Mensagem multiPut(Map<String,byte[]> pairs) {

        Mensagem s = new Mensagem(5);

        Map<String,byte[]> copia = new HashMap<>();

        for (Map.Entry<String,byte[]> e : pairs.entrySet()) {

            String eKey = e.getKey();
            byte[] eValue = e.getValue();
            if (eValue != null)
                eValue = e.getValue().clone();

            copia.put(eKey,eValue);

        }

        s.lista.add(copia);

        return s;

    }

    //Método que cria uma mensagem de multi get
    public static Mensagem multiGet(Set<String> keys) {

        Mensagem s = new Mensagem(6);
        s.lista.add(new HashSet<String>(keys));

        return s;

    }

    //Método que cria uma mensagem de get when
    public static Mensagem getWhen(String key, String keyCond, byte[] valueCond) {

        Mensagem s = new Mensagem(7);
        s.lista.add(key);
        s.lista.add(keyCond);
        
        if (valueCond != null)
            s.lista.add(valueCond.clone());
        else
            s.lista.add(valueCond);

        return s;

    }



}