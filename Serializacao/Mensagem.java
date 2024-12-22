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

    private int tipo;
    private static String[] tipoString = {"register","login","put","get","multiPut","multiGet","getWhen"};

    //Construtor básico
    private Mensagem(int tipo) {
        this.tipo = tipo;
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

    public String getNome() {
        return this.Nome;
    }

    public String getPassword() {
        return this.Password;
    }

    public String getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }

    public Map<String,byte[]> getValues() {
        return this.values;
    }

    public Set<String> getKeys() {
        return this.keys;
    }

    public String getKeyCond() {
        return this.keyCond;
    }

    public byte[] getValueCond() {
        return this.valueCond;
    }

    // #########################
    //         CONSTRUTORS
    // #########################

    //Método que cria uma mensagem de registo
    public static Mensagem registo(String nome, String password) {

        Mensagem s = new Mensagem(1);
        s.Nome = nome;
        s.Password = password;

        return s;

    }

    //Método que cria uma mensagem de registo
    public static Mensagem autenticacao(String nome, String password) {

        Mensagem s = new Mensagem(2);
        s.Nome = nome;
        s.Password = password;

        return s;

    }

    //Método que cria uma mensagem de put
    public static Mensagem put(String key, byte[] value) {

        Mensagem s = new Mensagem(3);
        s.key = key;

        if (value !=null)
            s.value = value.clone();

        return s;

    }

    //Método que cria uma mensagem de get
    public static Mensagem get(String key) {

        Mensagem s = new Mensagem(4);
        s.key = key;

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

        s.values = copia;

        return s;

    }

    //Método que cria uma mensagem de multi get
    public static Mensagem multiGet(Set<String> keys) {

        Mensagem s = new Mensagem(6);
        s.keys = new HashSet<String>(keys);

        return s;

    }

    //Método que cria uma mensagem de get when
    public static Mensagem getWhen(String key, String keyCond, byte[] valueCond) {

        Mensagem s = new Mensagem(7);
        s.key = key;
        s.keyCond = keyCond;
        
        if (valueCond != null)
            s.valueCond = valueCond.clone();

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
                m.Nome = in.readUTF();
                m.Password = in.readUTF();
            break;

            //Put
            case 3:
                m.key = in.readUTF();

                int value_length = in.readInt();

                if (value_length > 0) {
                    m.value = new byte[value_length];
                    in.read(m.value);
                }

            break;

            //Get
            case 4:
                m.key = in.readUTF();
            break;

            //MultiPut
            case 5:
                
                m.values = new HashMap<String,byte[]>();

                int values_length = in.readInt();

                for (int i=0 ; i < values_length ; i++) {

                    String s = in.readUTF();
                    int value_size = in.readInt();
                    byte[] b = null;

                    if (value_size > 0) {
                        b = new byte[value_size];
                        in.read(b);
                    }

                    m.values.put(s,b);

                }

            break;

            //MultiGet
            case 6:
                
                m.keys = new HashSet<String>();

                int keys_length = in.readInt();

                for (int i=0 ; i < keys_length ; i++) {

                    String s = in.readUTF();
                    m.keys.add(s);

                }

            break;

            //getWhen
            case 7:
                
                m.key = in.readUTF();
                m.keyCond = in.readUTF();

                int valueCond_length = in.readInt();

                if (valueCond_length > 0) {
                    m.valueCond = new byte[valueCond_length];
                    in.read(m.valueCond);
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