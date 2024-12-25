package Server;

import java.util.Map;

import Serializacao.InvalidMessageException;
import Serializacao.Mensagem;
import Serializacao.Resposta;


public class TaskHandler extends Thread{
    private Servidor server;

    public TaskHandler(Servidor server){
        this.server = server;
    }

    public void run(){
        
        while(true){

            server.l.lock();
            try{
                //aguarda até que hajam mensagens (tarefas)
                while(server.mensagens.isEmpty()) server.condTask.await();
                Task task = server.mensagens.poll(); //Retira uma mensagem da fila

                if(task!=null){

                    Resposta resposta = processaMensagem(task.mensagem);

                    //verifica qual é o cliente e insere lhe a resposta
                    Cliente c = task.cliente;

                    c.l.lock();
                    c.insert_Resposta(resposta);
                    c.l.unlock();
                        
                }


            } catch(InterruptedException e){
                e.printStackTrace();
            } finally{
                server.l.unlock();
            }

        }
    }


    private Resposta processaMensagem(Mensagem mensagem){
        Resposta resposta = null;
        
        int id = mensagem.getID();

        try{
            switch(mensagem.getTipo()){
            case 3: //tipo = put
                server.armazem.put(mensagem.getKey(), mensagem.getValue());
                resposta = Resposta.put(id,true);
            break;
            case 4: //tipo = get
                byte[] answerGet = server.armazem.get(mensagem.getKey());
                resposta = Resposta.get(id,answerGet);
            break;
            case 5: //tipo = multiPut
                server.armazem.multiPut(mensagem.getValues());
                resposta = Resposta.multiPut(id,true);
            break;
            case 6: //tipo = multiGet
                Map<String, byte[]> answerMultiGet = server.armazem.multiGet(mensagem.getKeys());
                resposta = Resposta.multiGet(id,answerMultiGet);
            break;
            case 7: //tipo = getWhen
                byte[] answerGetWhen = server.armazem.getWhen(mensagem.getKey(), mensagem.getKeyCond(), mensagem.getValueCond());
                resposta = Resposta.getWhen(id,answerGetWhen);
            break;
            default:
                throw new RuntimeException("Tipo de mensagem inválida.");
            }
        } catch(InvalidMessageException e){
            e.getStackTrace();
        } catch(InterruptedException e){
            e.getStackTrace();
        }
        
        return resposta;
    }

}
