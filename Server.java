import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    
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

             Socket socket = servidorSocket.accept();
             System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());

              try (
                    DataInputStream entrada = new DataInputStream(socket.getInputStream());
                    DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                    Scanner obj = new Scanner(System.in)
                ) {
                    String mensagemRecebida;

                    // Criar uma thread para o servidor enviar mensagens ao cliente (thread de escrita)
                    Thread enviarMensagens = new Thread(() -> {
                        try {
                            String mensagem;
                            while ((mensagem = obj.nextLine()) != null) {
                                saida.writeUTF(mensagem);
                                saida.flush();

                                if (mensagem.equalsIgnoreCase("sair")) {
                                    System.out.println("Encerrando servidor...");
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                        }
                    });
                    enviarMensagens.start();

                    while ((mensagemRecebida = entrada.readUTF()) != null) {
                        System.out.println("Cliente: " + mensagemRecebida);

                        if (mensagemRecebida.equalsIgnoreCase("sair")) {
                            System.out.println("A encerrar conexão...");
                            break;
                        }
                    }

                    enviarMensagens.join(); // Espera pela thread de escrita
               
                } catch (InterruptedException e) {
                    System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
                }
            
        }
        catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
        
        
