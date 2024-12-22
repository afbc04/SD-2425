import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Serializacao.Mensagem;
import Serializacao.Resposta;


public class Server {

    private static final int S = 3;

    static final Queue<Socket> clientesWaiting = new LinkedList<>();
    static final Queue<Mensagem> mensagens = new LinkedList<>();
    static final Lock l = new ReentrantLock(); // lock global
    static final Condition condGab = l.newCondition(); // para os Gabinetes
    static final Condition condTask = l.newCondition(); // para os TaskHandlers
    public static void main(String args[]) {

        int porta;

        try {

            porta = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            System.err.println("Uso: java Server.java [PORTA]");
            return;
        }

        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
             System.out.println("Servidor iniciado. A aguardar conexão...");

             Thread[] gabinetes = new Thread[S]; // threadpool
             for(int i = 0; i < S; i++) {
                gabinetes[i] = new Gabinete(); // criar o gabinete
                gabinetes[i].start(); // inciar a thread (executa o método run)
             }

             while(true) {
                 Socket socket = servidorSocket.accept(); // aceitar a conexão
                 System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());

                 l.lock();
                 try {
                    clientesWaiting.add(socket); // adicionar o socket à fila
                    condGab.signal(); // acordar alguma thread indicando que foi adicionado um socket
                } finally {
                    l.unlock();
                }
           }   
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
    }
}
        
class Gabinete extends Thread {
  
    public Gabinete() {}

    public void run() {
        Socket s = null;
    
        while (true) {
            Server.l.lock(); 
            try {
                // Aguarda até que haja um cliente na fila
                while (Server.clientesWaiting.isEmpty()) {
                    Server.condGab.await(); // Aguardar até que haja um cliente na fila
                }
                s = Server.clientesWaiting.poll(); // Retira um socket da fila
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Server.l.unlock(); 
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
                        Server.l.lock();
                        try {
                        Server.mensagens.add(mRecebida);
                        Server.condTask.signal();
                        } finally {
                            Server.l.unlock();
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