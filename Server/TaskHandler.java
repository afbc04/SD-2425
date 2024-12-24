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
            Mensagem task = null;

            server.l.lock();
            try{
                //aguarda até que hajam mensagens (tarefas)
                while(server.mensagens.isEmpty()) server.condTask.await();
                task = server.mensagens.poll(); //Retira uma mensagem da fila
            } catch(InterruptedException e){
                e.printStackTrace();
            } finally{
                server.l.unlock();
            }

            if(task!=null){
                Resposta resposta = processaMensagem(task);
                
                server.l.lock();
                try{
                    //verifica qual é o cliente e insere lhe a resposta
                    server.clientes.getCliente(task.getNome(), task.getPassword()).insert_Resposta(resposta);

                } catch(InvalidMessageException e){
                    e.getStackTrace();
                } finally{
                    server.l.unlock();
                }
            }
        }
    }


    private Resposta processaMensagem(Mensagem mensagem){
        Resposta resposta = null;
        
        try{
            switch(mensagem.getTipo()){
            case 3: //tipo = put
                server.armazem.put(mensagem.getKey(), mensagem.getValue());
                resposta = Resposta.put(true);
            break;
            case 4: //tipo = get
                byte[] answerGet = server.armazem.get(mensagem.getKey());
                resposta = Resposta.get(answerGet);
            break;
            case 5: //tipo = multiPut
                server.armazem.multiPut(mensagem.getValues());
                resposta = Resposta.multiPut(true);
            break;
            case 6: //tipo = multiGet
                Map<String, byte[]> answerMultiGet = server.armazem.multiGet(mensagem.getKeys());
                resposta = Resposta.multiGet(answerMultiGet);
            break;
            case 7: //tipo = getWhen
                byte[] answerGetWhen = server.armazem.getWhen(mensagem.getKey(), mensagem.getKeyCond(), mensagem.getValueCond());
                resposta = Resposta.getWhen(answerGetWhen);
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
