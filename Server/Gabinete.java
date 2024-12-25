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

            //final Socket s = s_gab;

            if (s != null) {

                int teste = 0;

                try (
                    DataInputStream entrada = new DataInputStream(s.getInputStream());
                    DataOutputStream saida = new DataOutputStream(s.getOutputStream())) {
                   // Scanner obj = new Scanner(System.in);
                    
                    //Resetar o cliente
                    this.cliente = null;
                    
                    //1º Fase -> Autenticação/Registo do Cliente
                    Mensagem authCliente = Mensagem.deserializar(entrada);
                    //Autenticação
                    if (authCliente.getTipo() == 2) {

                        try{
                            String nome = authCliente.getNome();
                            String passe = authCliente.getPassword();

                            this.cliente = server.clientes.getCliente(nome, passe);

                            if (this.cliente == null) {
                                System.out.println("Falha na autenticação para: " + nome);
                            }

                        } catch(InvalidMessageException i) {
                            System.err.println("Mensagem inválida recebida. A encerrar conexão...");
                            i.printStackTrace();
                        }

                    }

                    //Registo
                    if (authCliente.getTipo() == 1) {

                        try{
                            String nome = authCliente.getNome();
                            String passe = authCliente.getPassword();
                                
                            this.cliente = server.clientes.adicionarCliente(nome, passe);

                            if (this.cliente == null) {
                                System.out.println("Falha no registo para: " + nome);
                            }

                        } catch (InvalidMessageException i) {
                            System.err.println("Mensagem inválida recebida. A encerrar conexão...");
                            i.printStackTrace();
                        }
                          
                    }

                    //Verifica se a autenticação é válida
                    Resposta sessaoValida = Resposta.sessaoValida(authCliente.getID(),this.cliente != null);
                    sessaoValida.serializar(saida);

                    //Autenticação é válida
                    if (this.cliente != null) {

                        // Criar uma thread para enviar mensagens ao cliente
                        /*
                        Substituir pelo objeto resposta e depos serializar
                        */
                        Thread enviarMensagens = new Thread(() -> {

                            try {
                                while (true) {
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
                            }
                            catch (IOException e) {
                                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                            }
                            
                        });
                        enviarMensagens.start();

                        //Ler as mensagens recebidas
                        Mensagem mRecebida = null;
                        while (s.isConnected()) {
                            mRecebida = Mensagem.deserializar(entrada);
                            teste++;
                            System.out.println("Cliente: " + mRecebida);
                            server.l.lock();
                            try {
                                server.mensagens.add(new Task(this.cliente, mRecebida));
                                server.condTask.signal();
                            } finally {
                                server.l.unlock();
                            }
                      /* if (mRecebida.equalsIgnoreCase("sair")) {
                            System.out.println("A encerrar conexão...");
                            break;
                        } */
                        }
                        System.out.println("C");

    
                        enviarMensagens.interrupt();

                    }

                } catch (IOException e) {
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
