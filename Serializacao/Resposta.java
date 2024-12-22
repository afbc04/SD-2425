package Serializacao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Resposta {

    //Tipos possiveis da mensagem
    private boolean sessaoValida;
    private boolean put;
    private byte[] get;
    private boolean multiPut;
    private Map<String, byte[]> multiGet;
    private byte[] getWhen;

    private boolean[] camposValidos;
    private int tipo;
    private static String[] tipoString = {"sessao","put","get","multiPut","multiGet","getWhen"};

    //Construtor básico
    private Resposta(int tipo) {
        this.tipo = tipo;
        
        int N = 6;
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

    public boolean getSessaoValida() throws InvalidMessageException {

        if (this.camposValidos[0] == false)
            throw new InvalidMessageException();

        return this.sessaoValida;

    }

    public boolean getPut() throws InvalidMessageException {

        if (this.camposValidos[1] == false)
            throw new InvalidMessageException();

        return this.put;

    }

    public byte[] getGet() throws InvalidMessageException {

        if (this.camposValidos[2] == false)
            throw new InvalidMessageException();
            
        return this.get != null ? this.get.clone() : null;

    }

    public boolean getMultiPut() throws InvalidMessageException {

        if (this.camposValidos[3] == false)
            throw new InvalidMessageException();

        return this.multiPut;

    }

    public Map<String,byte[]> getMultiGet() throws InvalidMessageException {

        if (this.camposValidos[4] == false)
            throw new InvalidMessageException();

        return this.multiGet;

    }

    public byte[] getGetWhen() throws InvalidMessageException {

        if (this.camposValidos[5] == false)
            throw new InvalidMessageException();

        return this.getWhen != null ? this.getWhen.clone() : null;

    }

    // #########################
    //         SETTERS
    // #########################

    private void setSessao(boolean sessao) {
        this.sessaoValida = sessao;
        this.camposValidos[0] = true;
    }

    private void setPut(boolean put) {
        this.put = put;
        this.camposValidos[1] = true;
    }

    private void setGet(byte[] get) {
        this.get = get != null ? get.clone() : null;
        this.camposValidos[2] = true;
    }

    private void setMultiPut(boolean multiPut) {
        this.multiPut = multiPut;
        this.camposValidos[3] = true;
    }

    private void setMultiGet(Map<String,byte[]> multiGet) {
        this.multiGet = multiGet;
        this.camposValidos[4] = true;
    }

    private void setGetWhen(byte[] getWhen) {
        this.getWhen = getWhen != null ? getWhen.clone() : null;
        this.camposValidos[5] = true;
    }

    // #########################
    //         CONSTRUTORS
    // #########################

    //Método que cria uma resposta de inicio de sessão
    public static Resposta sessaoValida(boolean sessao) {

        Resposta r = new Resposta(1);
        r.setSessao(sessao);

        return r;

    }

    //Método que cria uma resposta de put
    public static Resposta put(boolean sucesso) {

        Resposta r = new Resposta(2);
        r.setPut(sucesso);

        return r;

    }

    //Método que cria uma resposta de get
    public static Resposta get(byte[] objeto) {

        Resposta r = new Resposta(3);
        r.setGet(objeto);

        return r;

    }

    //Método que cria uma resposta de multi put
    public static Resposta multiPut(boolean sucesso) {

        Resposta r = new Resposta(4);
        r.setMultiPut(sucesso);

        return r;

    }

    //Método que cria uma resposta de multi get
    public static Resposta multiGet(Map<String,byte[]> pairs) {

        Resposta r = new Resposta(5);

        Map<String,byte[]> copia = new HashMap<>();

        for (Map.Entry<String,byte[]> e : pairs.entrySet()) {

            String eKey = e.getKey();
            byte[] eValue = e.getValue();
            if (eValue != null)
                eValue = e.getValue().clone();

            copia.put(eKey,eValue);

        }

        r.setMultiGet(copia);

        return r;

    }

    //Método que cria uma resposta de get when
    public static Resposta getWhen(byte[] objeto) {

        Resposta r = new Resposta(6);
        r.setGetWhen(objeto);

        return r;

    }

    // #########################
    //         SERIALIZAR
    // #########################

    //Método que serializa uma resposta
    public void serializar(DataOutputStream out) throws IOException {

        boolean sucess = true;
        out.writeInt(this.tipo);

        switch (this.tipo) {

            //Sessão Válida
            case 1:
                out.writeBoolean(this.sessaoValida);
            break;

            //Put
            case 2:
                out.writeBoolean(this.put);
            break;

            //Get
            case 3:

                if (this.get == null) {
                    out.writeInt(0);
                }
                else {
                    out.writeInt(this.get.length);
                    out.write(this.get);
                }

            break;

            //MultiPut
            case 4:
                out.writeBoolean(this.multiPut);
            break;

            //MultiGet
            case 5:
                
                out.writeInt(this.multiGet.size());

                for (Map.Entry<String,byte[]> e : this.multiGet.entrySet()) {

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

            //GetWhen
            case 6:

                if (this.getWhen == null) {
                    out.writeInt(0);
                }
                else {
                    out.writeInt(this.getWhen.length);
                    out.write(this.getWhen);
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

    //Método que deserializa uma resposta
    public static Resposta deserializar(DataInputStream in) throws IOException {

        int tipo = in.readInt();
        Resposta r = new Resposta(tipo);
        boolean sucess = true;

        switch (tipo) {

            //Sessão
            case 1:
                r.setSessao(in.readBoolean());
            break;

            //Put
            case 2:
                r.setPut(in.readBoolean());
            break;

            //Get
            case 3:

                int value_length = in.readInt();

                if (value_length > 0) {
                    byte[] b = new byte[value_length];
                    in.read(b);
                    r.setGet(b);
                }
                else {
                    r.setGet(null);
                }

            break;

            //MultiPut
            case 4:
                r.setMultiPut(in.readBoolean());
            break;

            //MultiGet
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

                r.setMultiGet(values);

            break;

            //GetWhen
            case 6:

                int getWhen_length = in.readInt();

                if (getWhen_length > 0) {
                    byte[] b = new byte[getWhen_length];
                    in.read(b);
                    r.setGetWhen(b);
                }
                else {
                    r.setGetWhen(null);
                }

            break;

            default:
                sucess = false;
            break;
        
        }
        
        if (sucess == true)
            return r;
        else
            throw new IOException();

    }


}