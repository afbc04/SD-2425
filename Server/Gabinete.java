package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Serializacao.InvalidMessageException;
import Serializacao.Mensagem;
import Serializacao.Resposta;


public class Gabinete extends Thread {
  
    private Servidor server;
    private Cliente cliente;

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
                            if (cliente != null) {
                                cliente.l.lock();
                                try {
                                    while(!cliente.has_Respostas()) {
                                        cliente.cond.await();
                                    }
                                    Resposta resposta = cliente.remove_Resposta();
                                    resposta.serializar(saida);
                                /*
                                    if (resposta.equalsIgnoreCase("sair")) {
                                    System.out.println("Encerrando servidor...");
                                    obj.close();
                                    break;
                                } */
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    cliente.l.unlock();
                                }
                            }
                            else {
                                System.err.println("Erro.");
                                return;
                            }
                        } catch (IOException e) {
                            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                        }
                    });
                    
                    enviarMensagens.start();

                    //Ler as mensagens recebidas
                    Mensagem mRecebida;
                    while ((mRecebida = Mensagem.deserializar(entrada)) != null) {

                        if (mRecebida.getTipo() == 2) {
                            try{
                                String nome = mRecebida.getNome();
                                String passe = mRecebida.getPassword();

                                Cliente cli = null; 
                                cli = server.clientes.getCliente(nome, passe);

                                if (cli != null) {
                                    cliente = cli;
                                }
                                else {
                                    System.err.println("Falha na autenticação para: " + nome);
                                    s.close();
                                    return;
                                }

                            } catch(InvalidMessageException i) {
                                System.err.println("Mensagem inválida recebida. A encerrar conexão...");
                                i.printStackTrace();
                                try {
                                    s.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                return;
                            }
                        }

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
