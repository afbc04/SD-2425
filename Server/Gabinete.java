package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Serializacao.Mensagem;
import Serializacao.Resposta;

public class Gabinete extends Thread {
  
    private Servidor server;

    public Gabinete(Servidor server) {
        this.server = server;
    }

    public void run() {
        Socket s = null;
    
        while (true) {
            server.l.lock(); 
            try {
                // Aguarda até que haja um cliente na fila
                while (server.clientesWaiting.isEmpty()) {
                    server.condGab.await(); // Aguardar até que haja um cliente na fila
                }
                s = server.clientesWaiting.poll(); // Retira um socket da fila
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                server.l.unlock(); 
            }
    
            if (s != null) {
                try (
                    DataInputStream entrada = new DataInputStream(s.getInputStream());
                    DataOutputStream saida = new DataOutputStream(s.getOutputStream())) {
                   // Scanner obj = new Scanner(System.in);
                    
                        
                    // Criar uma thread para enviar mensagens ao cliente
                    /*
                     Substituir pelo objeto resposta e depos serializar
                     */
                    Thread enviarMensagens = new Thread(() -> {
                        try {
                            Resposta resposta = null;
                            resposta.serializar(saida);
                                /*
                                    if (resposta.equalsIgnoreCase("sair")) {
                                    System.out.println("Encerrando servidor...");
                                    obj.close();
                                    break;
                                } */
                        } catch (IOException e) {
                            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                        }
                    });
                    
                    enviarMensagens.start();

                    //Ler as mensagens recebidas
                    Mensagem mRecebida;
                    while ((mRecebida = Mensagem.deserializar(entrada)) != null) {
                        System.out.println("Cliente: " + mRecebida);
                        server.l.lock();
                        try {
                        server.mensagens.add(mRecebida);
                        server.condTask.signal();
                        } finally {
                            server.l.unlock();
                        }
                      /* if (mRecebida.equalsIgnoreCase("sair")) {
                            System.out.println("A encerrar conexão...");
                            break;
                        } */
                    }
    
                    enviarMensagens.join(); // Aguarda a thread de envio finalizar
    
                } catch (IOException | InterruptedException e) {
                    System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
                } finally { // serve para fechar arquivos/sockets ou libertar recursos
                    try {
                        s.close(); // Fechar o socket após a comunicação
                    } catch (IOException e) {
                        System.err.println("Erro ao fechar socket: " + e.getMessage());
                    }
                }
                System.out.println("Conexão com o cliente encerrada.");
            }
        }
    }
}    
