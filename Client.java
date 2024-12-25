import java.io.*;
import java.net.*;
import java.util.*;

import Client.ClienteDados;
import Client.Dados;
import Serializacao.InvalidMessageException;
import Serializacao.Mensagem;
import Serializacao.Resposta;

public class Client {

    public static void main(String args[]) {

        int porta;
        String servidor_ip;

        try {
            servidor_ip = args[0];
            porta = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Uso: java Client.java [IP SERVIDOR] [PORTA]");
            return;
        }

        try (final Socket socket = new Socket(servidor_ip, porta)) {

            System.out.println("Conectado ao servidor.");
            ClienteDados cliente = new ClienteDados();

            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            boolean sessaoValida = Client.autenticar(scanner, entrada, saida, cliente);

            //Autenticação válida
            if (sessaoValida == true) {

                cliente.running = true;
                String nome = cliente.nome;

                //Thread que lê as respostas do servidor
                Thread leitorCliente = new Thread(() -> {
    
                    while(socket.isConnected() && !socket.isClosed()) {
    
                        try {
    
                            Resposta r = Resposta.deserializar(entrada);
                            int respostaID = r.getID();

                            cliente.l.writeLock().lock();

                            Dados dado = cliente.dados.get(respostaID);

                            //Existe dado
                            if (dado != null) {

                                dado.l.writeLock().lock();
                                cliente.l.writeLock().unlock();

                                dado.addResposta(r);

                                dado.l.writeLock().unlock();

                            }
                            //Não existe dado
                            else {

                                cliente.l.writeLock().unlock();

                            }

                        }
                        catch (IOException e) {
                            break;
                        }
    
                    }
    
                });
                leitorCliente.start();

                //Thread que lê pedidos e envia mensagens ao servidor
                Thread escritorCliente = new Thread(() -> {

                    while (socket.isConnected() && !socket.isClosed()) {

                        String pedido = null;
                        int pedidoID = 0;
            
                        cliente.l.writeLock().lock();
                        try{

                            //aguarda até que hajam pedidos
                            while(cliente.pedidos.isEmpty()) 
                                cliente.condPED.await();

                            pedido = cliente.pedidos.poll(); //Retira um pedido da fila
            
                            //Pedido válido
                            if (pedido != null) {
                            
                                String input = pedido.trim();
                                String[] tokens = input.split(" ");
                                String command = tokens[0].toUpperCase();
                            
                                Mensagem m = null;
                                pedidoID = cliente.taskID;
                                cliente.taskID++;
                            
                                try {

                                    switch (command) {
                                        case "PUT":
                                            m = handlePut(pedidoID,tokens);
                                            break;
                                        case "GET":
                                            m = handleGet(pedidoID,tokens);
                                            break;
                                        case "MULTIPUT":
                                            m = handleMultiPut(pedidoID,tokens);
                                            break;
                                        case "MULTIGET":
                                            m = handleMultiGet(pedidoID,tokens);
                                            break;
                                        case "GETWHEN":
                                            m = handleGetWhen(pedidoID,tokens);
                                            break;
                                        case "OPEN":

                                            List<String> lista = handleOpenFile(tokens);

                                            for (String s : lista)
                                                cliente.pedidos.add(s);

                                            break;

                                        default:
                                            System.err.println("Comando inválido.");
                                            break;
                                    }

                                } catch (IllegalArgumentException e) {
                                    System.err.println("Erro: " + e.getMessage());
                                }
                            
                                //Mandar a mensagem
                                if (m != null && socket.isConnected() && !socket.isClosed()) {

                                    try {
                                      
                                        m.serializar(saida);

                                        cliente.dados.put(m.getID(),new Dados(m));

                                    }
                                    catch (IOException e) {
                                        System.err.println("Erro na serialização da mensagem: " + e.getMessage());
                                    }
                                
                                }            

                            }
                            
                        } catch(InterruptedException e){
                            break;
                        } finally{
                            cliente.l.writeLock().unlock();
                        }
            
                        

                    }
    
                });
                escritorCliente.start();

                while (socket.isConnected() && !socket.isClosed()) {

                    menu(scanner,nome);
                    String input = scanner.nextLine();

                    if (input.equalsIgnoreCase("SAIR"))
                        break;

                    if (input.equalsIgnoreCase("EXPORT")) {
                            
                        StringBuilder sb = new StringBuilder("Resultados/");
                        sb.append(nome);
                        sb.append(".txt");

                        FileWriter f = new FileWriter(sb.toString(),false);

                        f.write("[");
                        boolean flag = false;

                        cliente.l.readLock().lock();
                        List<Dados> lista = new ArrayList<>();
                        
                        for (Dados d : cliente.dados.values()) {

                            d.l.readLock().lock();
                            lista.add(d);

                        }

                        cliente.l.readLock().unlock();

                        for (Dados d : lista) {

                            if (flag == false) {
                                f.write("\n");
                                f.write(d.toString());
                                flag = true;
                            }
                            else {
                                f.write(",\n");
                                f.write(d.toString());
                            }

                            d.l.readLock().unlock();

                        }
                        
                        if (flag == true)
                            f.write("\n");

                        f.write("]");

                        f.close();
                            
                    }
                    else {

                        cliente.l.writeLock().lock();
                        cliente.pedidos.add(input);
                        cliente.condPED.signal();
                        cliente.l.writeLock().unlock();

                    }

                            
                }

                escritorCliente.interrupt();
                leitorCliente.interrupt();
                //Esperar as threads terminarem
                //leitorCliente.join();
                //escritorCliente.join();

            }

            //cliente.condPED.signalAll();
            scanner.close();
            socket.close();
            

        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }

    }

    private static boolean autenticar(Scanner sc, DataInputStream entrada, DataOutputStream saida, ClienteDados cliente) {

        Client.limpaEcra();
        System.out.println("Inicie a conexão com o servidor fazendo:");
        System.out.println("REGISTER <username> <password>");
        System.out.println("LOGIN <username> <password>");
        System.out.println("SAIR");

        String input = sc.nextLine().trim();
        String[] tokens = input.split(" ");
        String command = tokens[0].toUpperCase();

        Mensagem auth = null;
        boolean sessaoValida = false;

        try {
            switch (command) {
                case "REGISTER":
                    auth = handleRegister(tokens);
                    break;
                case "LOGIN":
                    auth = handleLogin(tokens);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        //Aconteceu um erro
        if (auth == null) 
            return false;

        //Enviar a mensagem
        else {

            try {

                auth.serializar(saida);
                Resposta authResponse = Resposta.deserializar(entrada);
                if (authResponse.getSessaoValida() == true) {
                    sessaoValida = true;
                }

            }
            catch (IOException | InvalidMessageException e) {
                System.err.println("Erro na autenticação: " + e.getMessage());
            }

        }

        //Sessão inválida
        if (sessaoValida == false) {

            System.err.println("Autenticação falhou...");
            return false;

        }

        cliente.nome = tokens[1];
        return true;

    }

    // ##############
    //    HANDLERS
    // ##############

    // Handlers para cada comando
    private static Mensagem handleRegister(String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: REGISTER <username> <password>");
        return Mensagem.registo(0,tokens[1], tokens[2]);
    }

    private static Mensagem handleLogin(String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: LOGIN <username> <password>");
        return Mensagem.autenticacao(0,tokens[1], tokens[2]);
    }

    private static Mensagem handlePut(int ID, String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: PUT <key> <value>");
        return Mensagem.put(ID,tokens[1], tokens[2].getBytes());
    }

    private static Mensagem handleGet(int ID, String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 2) throw new IllegalArgumentException("Uso: GET <key>");
        return Mensagem.get(ID,tokens[1]);
    }

    private static Mensagem handleMultiPut(int ID, String[] tokens) throws IllegalArgumentException {
        if (tokens.length < 3 || tokens.length % 2 != 1) throw new IllegalArgumentException("Uso: MULTIPUT <key1> <value1> <key2> <value2> ...");

        Map<String, byte[]> pairs = new HashMap<>();
        for (int i = 1; i < tokens.length; i += 2) {
            pairs.put(tokens[i], tokens[i + 1].getBytes());
        }

        return Mensagem.multiPut(ID,pairs);
    }

    private static Mensagem handleMultiGet(int ID, String[] tokens) throws IllegalArgumentException {
        if (tokens.length < 2) throw new IllegalArgumentException("Uso: MULTIGET <key1> <key2> ...");

        Set<String> keys = new HashSet<>(Arrays.asList(tokens).subList(1, tokens.length));
        return Mensagem.multiGet(ID,keys);
    }

    private static Mensagem handleGetWhen(int ID, String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 4) throw new IllegalArgumentException("Uso: GETWHEN <key> <keyCond> <valueCond>");
        return Mensagem.getWhen(ID,tokens[1], tokens[2], tokens[3].getBytes());
    }

    private static List<String> handleOpenFile(String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 2) throw new IllegalArgumentException("Uso: OPEN <file path>");

        List<String> lista = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(tokens[1]));

            String linha = null;
            while ((linha = br.readLine()) != null) 
                lista.add(linha);

            br.close();
        }
        catch (Exception e) {}

        return lista;
    }

    // ##############
    //    MENU
    // ##############

    //Metodo que limpa o Ecra, ajudando na visualização do menu
    private static void limpaEcra() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void menu(Scanner sc, String nome) {

        limpaEcra();
        System.out.println("--------------");
        System.out.println("Cliente: " + nome + "\n");

        System.out.println("Digite comandos no formato apropriado:");
        System.out.println("Exemplos:");
        System.out.println("PUT <key> <value>");
        System.out.println("GET <key>");
        System.out.println("MULTIPUT <key1> <value1> <key2> <value2> ...");
        System.out.println("MULTIGET <key1> <key2> ...");
        System.out.println("GETWHEN <key> <keyCond> <valueCond>");
        System.out.println("");
        System.out.println("OPEN <path file>");
        System.out.println("EXPORT");
        System.out.println("SAIR\n");

    }

}
