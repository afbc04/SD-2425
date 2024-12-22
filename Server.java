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


public class Server {

    private static final int S = 3;
    static int sessoesAtivas = 0;

    static final Queue<Socket> clientesWaiting = new LinkedList<>();
    static final Lock l = new ReentrantLock(); // lock global
    static final Condition cond = l.newCondition();
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
                    while(sessoesAtivas >= S) {
                        cond.await(); // fica em espera até algum socket ser libertado
                    }
                    sessoesAtivas++;

                    clientesWaiting.add(socket); // adicionar o socket à fila
                    cond.signalAll(); // acordar alguma thread indicando que foi adicionado um socket
                } catch (InterruptedException e){ 
                    e.printStackTrace();
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
                    Server.cond.await(); // Aguardar até que haja um cliente na fila
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
                    Scanner obj = new Scanner(System.in);
                    String mensagemRecebida;
    
                    // Criar uma thread para enviar mensagens ao cliente
                    Thread enviarMensagens = new Thread(() -> {
                        try {
                            String mensagem;
                            while ((mensagem = obj.nextLine()) != null) {
                                saida.writeUTF(mensagem);
                                saida.flush();
    
                                if (mensagem.equalsIgnoreCase("sair")) {
                                    System.out.println("Encerrando servidor...");
                                    obj.close();
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                        }
                    });
    
                    enviarMensagens.start();
    
                    // Ler mensagens do cliente
                    while ((mensagemRecebida = entrada.readUTF()) != null) {
                        System.out.println("Cliente: " + mensagemRecebida);
    
                        if (mensagemRecebida.equalsIgnoreCase("sair")) {
                            System.out.println("A encerrar conexão...");
                            break;
                        }
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
                    Server.l.lock();
                    try {
                        Server.sessoesAtivas--;
                        Server.cond.signalAll(); // acorda outras threads indicando que algum socket ficou livre
                    } finally {
                        Server.l.unlock();
                    }
                }
                System.out.println("Conexão com o cliente encerrada.");
            }
        }
    }
}    