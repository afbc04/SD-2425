import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Server.Servidor;
import Server.TaskHandler;
import Server.Gabinete;


public class Server {

    public static void main(String args[]) {

        int porta;
        int N;
        int S;

        try {

            porta = Integer.parseInt(args[0]);
            S = Integer.parseInt(args[1]);
            N = Integer.parseInt(args[2]); 

            if (S < 1)
                S = 1;
            
            if (N < 1)
                N = 1;
            
        }
        catch (Exception e) {
            System.err.println("Uso: java Server.java [PORTA] [Nº CLIENTES CONCORRENTES] [Nº PEDIDOS CONCORRENTES]");
            return;
        }

        Servidor server = new Servidor(S,N);

        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
             System.out.println("Servidor iniciado. A aguardar conexão...");

             Thread[] gabinetes = new Thread[server.S]; // threadpool
             for(int i = 0; i < server.S; i++) {
                gabinetes[i] = new Gabinete(server); // criar o gabinete
                gabinetes[i].start(); // inciar a thread (executa o método run)
             }

             Thread[] taskHandlers = new Thread[server.N]; // threadpool
             for(int i = 0; i < server.N; i++) {
                taskHandlers[i] = new TaskHandler(server); // criar o gabinete
                taskHandlers[i].start(); // inciar a thread (executa o método run)
             }

             while(true) {
                 final Socket socket = servidorSocket.accept(); // aceitar a conexão
                 System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());

                 server.l.lock();
                 try {
                    server.clientesWaiting.add(socket); // adicionar o socket à fila
                    server.condGab.signal(); // acordar alguma thread indicando que foi adicionado um socket
                } finally {
                    server.l.unlock();
                }
           }   
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
    }
}
        
