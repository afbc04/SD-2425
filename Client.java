import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    
    public static void main(String args[]) {

        int porta;
        String servidor_ip;

        try {
            servidor_ip = args[0];
            porta = Integer.parseInt(args[1]);
        }
        catch (Exception e) {
            System.err.println("Uso: java Client.java [IP SERVIDOR] [PORTA]");
            return;
        }

        try (Socket socket = new Socket(servidor_ip, porta)) {
            System.out.println("Conectado ao servidor.");

            try (DataInputStream entrada = new DataInputStream(socket.getInputStream());
                 DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                 Scanner obj = new Scanner(System.in)) {
                 
                 System.out.println("Digite mensagens para enviar ao servidor (digite 'sair' para encerrar):");

                // Thread de leitura para ler dados enviados do servidor
                Thread leituraServidor = new Thread(() -> {
                    try {
                        String mensagemRecebida;
                        while ((mensagemRecebida = entrada.readUTF()) != null) {
                            System.out.println("Servidor: " + mensagemRecebida);

                            if (mensagemRecebida.equalsIgnoreCase("sair")) {
                                System.out.println("Conexão encerrada pelo servidor.");
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Conexão encerrada: " + e.getMessage());
                    }
                });

                leituraServidor.start();

                // Thread principal (de escrita) para enviar dados para o servidor
                String mensagem;
                while ((mensagem = obj.nextLine()) != null) {
                    saida.writeUTF(mensagem);
                    saida.flush();

                    if (mensagem.equalsIgnoreCase("sair")) {
                        System.out.println("Encerrando cliente...");
                        break;
                    }
                }

            leituraServidor.join(); // Aguarda pela thread de leitura finalizar
            } catch (InterruptedException e) {
                System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
            }

            System.out.println("Conexão com o servidor encerrada.");
        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }
}