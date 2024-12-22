package Serializacao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Mensagem {

    //Tipos possiveis da mensagem
    private String Nome = null;
    private String Password = null;

    private String key = null;
    private byte[] value = null;

    private Map<String,byte[]> values = null;
    private Set<String> keys = null;

    private String keyCond = null;
    private byte[] valueCond = null;

    private boolean[] camposValidos;
    private int tipo;
    private static String[] tipoString = {"register","login","put","get","multiPut","multiGet","getWhen"};

    //Construtor básico
    private Mensagem(int tipo) {
        this.tipo = tipo;
        
        int N = 8;
        this.camposValidos = new boolean[N];

        for (int i=0 ; i < N ; i++)
            this.camposValidos[i] = false;

    } 

    // #########################
    //         GETTERS
    // #########################

    public int getTipo() {
        return this.tipo;
    }

    public String getTipoString() {
        return tipoString[this.tipo-1];
    }

    public String getNome() throws InvalidMessageException {

        if (this.camposValidos[0] == false)
            throw new InvalidMessageException();

        return this.Nome;

    }

    public String getPassword() throws InvalidMessageException {

        if (this.camposValidos[1] == false)
            throw new InvalidMessageException();

        return this.Password;

    }

    public String getKey() throws InvalidMessageException {

        if (this.camposValidos[2] == false)
            throw new InvalidMessageException();
            
        return this.key;

    }

    public byte[] getValue() throws InvalidMessageException {

        if (this.camposValidos[3] == false)
            throw new InvalidMessageException();

        return this.value;

    }

    public Map<String,byte[]> getValues() throws InvalidMessageException {

        if (this.camposValidos[4] == false)
            throw new InvalidMessageException();

        return this.values;

    }

    public Set<String> getKeys() throws InvalidMessageException {

        if (this.camposValidos[5] == false)
            throw new InvalidMessageException();

        return this.keys;

    }

    public String getKeyCond() throws InvalidMessageException {

        if (this.camposValidos[6] == false)
            throw new InvalidMessageException();

        return this.keyCond;

    }

    public byte[] getValueCond() throws InvalidMessageException {

        if (this.camposValidos[7] == false)
            throw new InvalidMessageException();

        return this.valueCond;

    }

    // #########################
    //         SETTERS
    // #########################

    private void setNome(String nome) {
        this.Nome = nome;
        this.camposValidos[0] = true;
    }

    private void setPassword(String password) {
        this.Password = password;
        this.camposValidos[1] = true;
    }

    private void setKey(String key) {
        this.key = key;
        this.camposValidos[2] = true;
    }

    private void setValue(byte[] value) {
        this.value = value != null ? value.clone() : null;
        this.camposValidos[3] = true;
    }

    private void setValues(Map<String,byte[]> values) {
        this.values = values;
        this.camposValidos[4] = true;
    }

    private void setKeys(Set<String> keys) {
        this.keys = keys;
        this.camposValidos[5] = true;
    }

    private void setKeyCond(String keyCond) {
        this.keyCond = keyCond;
        this.camposValidos[6] = true;
    }

    private void setValueCond(byte[] valueCond) {
        this.valueCond = valueCond != null ? valueCond.clone() : null;
        this.camposValidos[7] = true;
    }

    // #########################
    //         CONSTRUTORS
    // #########################

    //Método que cria uma mensagem de registo
    public static Mensagem registo(String nome, String password) {

        Mensagem s = new Mensagem(1);
        s.setNome(nome);
        s.setPassword(password);

        return s;

    }

    //Método que cria uma mensagem de registo
    public static Mensagem autenticacao(String nome, String password) {

        Mensagem s = new Mensagem(2);
        s.setNome(nome);
        s.setPassword(password);

        return s;

    }

    //Método que cria uma mensagem de put
    public static Mensagem put(String key, byte[] value) {

        Mensagem s = new Mensagem(3);
        s.setKey(key);
        s.setValue(value);

        return s;

    }

    //Método que cria uma mensagem de get
    public static Mensagem get(String key) {

        Mensagem s = new Mensagem(4);
        s.setKey(key);

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

        s.setValues(copia);

        return s;

    }

    //Método que cria uma mensagem de multi get
    public static Mensagem multiGet(Set<String> keys) {

        Mensagem s = new Mensagem(6);
        s.setKeys(new HashSet<String>(keys));

        return s;

    }

    //Método que cria uma mensagem de get when
    public static Mensagem getWhen(String key, String keyCond, byte[] valueCond) {

        Mensagem s = new Mensagem(7);
        s.setKey(keyCond);
        s.setKeyCond(keyCond);
        s.setValueCond(valueCond);

        return s;

    }

    // #########################
    //         SERIALIZAR
    // #########################

    //Método que serializa uma mensagem
    public void serializar(DataOutputStream out) throws IOException {

        boolean sucess = true;
        out.writeInt(this.tipo);

        switch (this.tipo) {

            //Registo | Autenticação
            case 1:
            case 2:
                out.writeUTF(this.Nome);
                out.writeUTF(this.Password);
            break;

            //Put
            case 3:
                out.writeUTF(this.key);

                if (this.value == null) {
                    out.writeInt(0);
                }
                else {
                    out.writeInt(this.value.length);
                    out.write(this.value);
                }

            break;

            //Get
            case 4:
                out.writeUTF(this.key);
            break;

            //MultiPut
            case 5:
                
                out.writeInt(this.values.size());

                for (Map.Entry<String,byte[]> e : this.values.entrySet()) {

                    out.writeUTF(e.getKey());
                    byte[] b = e.getValue();

                    if (b != null) {
                        out.writeInt(b.length);
                        out.write(b);
                    }
                    else {
                        out.writeInt(0);
                    }

                }

            break;

            //MultiGet
            case 6:
                
                out.writeInt(this.keys.size());

                for (String s : this.keys) {

                    out.writeUTF(s);

                }

            break;

            //getWhen
            case 7:
                
                out.writeUTF(this.key);
                out.writeUTF(this.keyCond);

                if (this.valueCond != null) {
                    out.writeInt(this.valueCond.length);
                    out.write(this.valueCond);
                }
                else {
                    out.writeInt(0);
                }

            break;

            default:
                sucess = false;
            break;
        
        }
        
        if (sucess == false)
            throw new IOException();

    }

    // #########################
    //         DESERIALIZAR
    // #########################

    //Método que deserializa uma mensagem
    public static Mensagem deserializar(DataInputStream in) throws IOException {

        int tipo = in.readInt();
        Mensagem m = new Mensagem(tipo);
        boolean sucess = true;

        switch (tipo) {

            //Registo | Autenticação
            case 1:
            case 2:
                m.setNome(in.readUTF());
                m.setPassword(in.readUTF());
            break;

            //Put
            case 3:
                m.setKey(in.readUTF());

                int value_length = in.readInt();

                if (value_length > 0) {
                    byte[] b = new byte[value_length];
                    in.read(b);
                    m.setValue(b);
                }
                else {
                    m.setValue(null);
                }

            break;

            //Get
            case 4:
                m.setKey(in.readUTF());
            break;

            //MultiPut
            case 5:
                
                Map<String,byte[]> values = new HashMap<String,byte[]>();

                int values_length = in.readInt();

                for (int i=0 ; i < values_length ; i++) {

                    String s = in.readUTF();
                    int value_size = in.readInt();
                    byte[] b = null;

                    if (value_size > 0) {
                        b = new byte[value_size];
                        in.read(b);
                    }

                    values.put(s,b);

                }

                m.setValues(values);

            break;

            //MultiGet
            case 6:
                
                Set<String> keys = new HashSet<String>();

                int keys_length = in.readInt();

                for (int i=0 ; i < keys_length ; i++) {
                    keys.add(in.readUTF());
                }

                m.setKeys(keys);

            break;

            //getWhen
            case 7:
                
                m.setKey(in.readUTF());
                m.setKeyCond(in.readUTF());

                int valueCond_length = in.readInt();

                if (valueCond_length > 0) {
                    byte[] b = new byte[valueCond_length];
                    in.read(b);
                    m.setValueCond(b);
                }
                else {
                    m.setValueCond(null);
                }

            break;

            default:
                sucess = false;
            break;
        
        }
        
        if (sucess == true)
            return m;
        else
            throw new IOException();

    }


}