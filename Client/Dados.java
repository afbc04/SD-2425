package Client;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Serializacao.Mensagem;
import Serializacao.Resposta;

public class Dados {
    
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();

    private Mensagem mensagem;
    private LocalDateTime mensagemTime;

    private Resposta resposta;
    private LocalDateTime respostaTime;

    public Dados(Mensagem m) {
        
        this.mensagem = m;
        this.mensagemTime = LocalDateTime.now();

        this.resposta = null;

    }

    public void addResposta(Resposta r) {
        this.resposta = r;
        this.respostaTime = LocalDateTime.now();
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("   {\n");
        sb.append("      \"query_id\" : ");
        sb.append(this.mensagem.getID());
        sb.append(",\n");
        sb.append("      \"message\" : \"");
        sb.append(this.mensagem.getTipoString().toUpperCase());
        sb.append(toString_msg_aux(this.mensagem.getTipo()));
        sb.append("\",\n      \"time\" : \"" + this.mensagemTime.toString() + "\",\n\n");

        sb.append("      \"resposta_existe\" : ");
        if (this.resposta == null) {

            sb.append("false");

        }
        else {

            sb.append("true,\n");
            sb.append("      \"resposta\" : \"");
            sb.append(toString_res_aux(this.resposta.getTipo()));
            sb.append("\",\n      \"time\" : \"" + this.respostaTime.toString() + "\"");

        }

        sb.append("\n   }");

        return sb.toString();

    }

    private String toString_msg_aux(int tipo) {

        try {

            StringBuilder sb = new StringBuilder();

            switch (tipo) {

                case 1:
                case 2:

                sb.append(" " + this.mensagem.getNome());
                sb.append(" ***");
                    
                break;

                case 3:

                sb.append(" " + this.mensagem.getKey());
                sb.append(" " + new String(this.mensagem.getValue()));

                break;

                case 4:

                sb.append(" " + this.mensagem.getKey());

                break;

                case 5:

                Map<String,byte[]> values = this.mensagem.getValues();

                for (Map.Entry<String,byte[]> e : values.entrySet()) {

                    sb.append(" " + e.getKey());
                    sb.append(" " + new String(e.getValue()));

                }

                break;

                case 6:

                Set<String> keys = this.mensagem.getKeys();

                for (String key : keys) {

                    sb.append(" " + key);

                }

                break;

                case 7:

                sb.append(" " + this.mensagem.getKey());
                sb.append(" " + this.mensagem.getKeyCond());
                sb.append(" " + new String(this.mensagem.getValueCond()));

                break;
            
                default:
                break;

            }

            return sb.toString();

        }
        catch (Exception e) {
            return "*";
        }

    }

    private String toString_res_aux(int tipo) {

        try {

            StringBuilder sb = new StringBuilder();

            switch (tipo) {

                case 1:

                sb.append(this.resposta.getSessaoValida());
                    
                break;

                case 2:

                sb.append(this.resposta.getPut());
                    
                break;

                case 3:

                if (this.resposta.getGet() == null)
                    sb.append("null");
                else
                    sb.append(new String(this.resposta.getGet()));

                break;

                case 4:

                sb.append(this.resposta.getMultiPut());

                break;

                case 5:

                Map<String,byte[]> values = this.resposta.getMultiGet();

                sb.append("[");

                for (Map.Entry<String,byte[]> e : values.entrySet()) {

                    sb.append(" (" + e.getKey());

                    if (e.getValue() == null)
                        sb.append(" , null)");
                    else
                        sb.append(" , " + new String(e.getValue()) + ")");

                }

                sb.append("]");

                break;

                case 6:

                if (this.resposta.getGetWhen() == null)
                    sb.append("null");
                else
                    sb.append(new String(this.resposta.getGetWhen()));

                break;
            
                default:
                break;

            }

            return sb.toString();

        }
        catch (Exception e) {
            return "*";
        }

    }

}
