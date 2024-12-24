import java.io.*;
import java.net.*;
import java.util.*;

import Serializacao.Mensagem;
import Serializacao.Resposta;

public class Client {

    public static void main(String[] args) {
        int porta;
        String servidor_ip;

        try {
            servidor_ip = args[0];
            porta = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Uso: java Client.java [IP SERVIDOR] [PORTA]");
            return;
        }

        try (Socket socket = new Socket(servidor_ip, porta)) {
            System.out.println("Conectado ao servidor.");

            try (DataInputStream entrada = new DataInputStream(socket.getInputStream());
                 DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                 Scanner scanner = new Scanner(System.in)) {

                System.out.println("Digite comandos no formato apropriado:");
                System.out.println("Exemplos:");
                System.out.println("REGISTER <username> <password>");
                System.out.println("LOGIN <username> <password>");
                System.out.println("SAIR");

                String input_aux = scanner.nextLine().trim();
                String[] tokens_aux = input_aux.split(" ");
                String command_aux = tokens_aux[0].toUpperCase();

                try {
                    switch (command_aux) {
                        case "REGISTER":
                            handleRegister(tokens_aux, saida, entrada);
                            break;
                        case "LOGIN":
                            handleLogin(tokens_aux, saida, entrada);
                            break;
                        default:
                            return;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Erro: " + e.getMessage());
                }

                System.out.println("Digite comandos no formato apropriado:");
                System.out.println("Exemplos:");
                System.out.println("PUT <key> <value>");
                System.out.println("GET <key>");
                System.out.println("MULTIPUT <key1> <value1> <key2> <value2> ...");
                System.out.println("MULTIGET <key1> <key2> ...");
                System.out.println("GETWHEN <key> <keyCond> <valueCond>");
                System.out.println("SAIR");

                while (true) {
                    String input = scanner.nextLine().trim();
                    String[] tokens = input.split(" ");
                    String command = tokens[0].toUpperCase();

                    try {
                        switch (command) {
                            case "PUT":
                                handlePut(tokens, saida, entrada);
                                break;
                            case "GET":
                                handleGet(tokens, saida, entrada);
                                break;
                            case "MULTIPUT":
                                handleMultiPut(tokens, saida, entrada);
                                break;
                            case "MULTIGET":
                                handleMultiGet(tokens, saida, entrada);
                                break;
                            case "GETWHEN":
                                handleGetWhen(tokens, saida, entrada);
                                break;
                            case "SAIR":
                                System.out.println("Encerrando cliente...");
                                return;
                            default:
                                System.out.println("Comando inválido.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }

    // Handlers para cada comando
    private static void handleRegister(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: REGISTER <username> <password>");
        Mensagem msg = Mensagem.registo(tokens[1], tokens[2]);
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        System.out.println("Servidor: " + (resposta.getSessaoValida() ? "Registo bem-sucedido" : "Falha no registo"));
    }

    private static void handleLogin(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: LOGIN <username> <password>");
        Mensagem msg = Mensagem.autenticacao(tokens[1], tokens[2]);
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        System.out.println("Servidor: " + (resposta.getSessaoValida() ? "Login bem-sucedido" : "Falha no login"));
    }

    private static void handlePut(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length != 3) throw new IllegalArgumentException("Uso: PUT <key> <value>");
        Mensagem msg = Mensagem.put(tokens[1], tokens[2].getBytes());
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        System.out.println("Servidor: " + (resposta.getPut() ? "PUT bem-sucedido" : "Falha no PUT"));
    }

    private static void handleGet(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length != 2) throw new IllegalArgumentException("Uso: GET <key>");
        Mensagem msg = Mensagem.get(tokens[1]);
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        try {
            System.out.println("Servidor: Valor = " + new String(resposta.getGet()));
        } catch (Exception e) {
            System.out.println("Servidor: Chave não encontrada");
        }
    }

    private static void handleMultiPut(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length < 3 || tokens.length % 2 != 1) throw new IllegalArgumentException("Uso: MULTIPUT <key1> <value1> <key2> <value2> ...");

        Map<String, byte[]> pairs = new HashMap<>();
        for (int i = 1; i < tokens.length; i += 2) {
            pairs.put(tokens[i], tokens[i + 1].getBytes());
        }

        Mensagem msg = Mensagem.multiPut(pairs);
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        System.out.println("Servidor: " + (resposta.getMultiPut() ? "MULTIPUT bem-sucedido" : "Falha no MULTIPUT"));
    }

    private static void handleMultiGet(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length < 2) throw new IllegalArgumentException("Uso: MULTIGET <key1> <key2> ...");

        Set<String> keys = new HashSet<>(Arrays.asList(tokens).subList(1, tokens.length));
        Mensagem msg = Mensagem.multiGet(keys);
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        System.out.println("Servidor: " + resposta.getMultiGet());
    }

    private static void handleGetWhen(String[] tokens, DataOutputStream saida, DataInputStream entrada) throws IOException {
        if (tokens.length != 4) throw new IllegalArgumentException("Uso: GETWHEN <key> <keyCond> <valueCond>");
        Mensagem msg = Mensagem.getWhen(tokens[1], tokens[2], tokens[3].getBytes());
        msg.serializar(saida);

        Resposta resposta = Resposta.deserializar(entrada);
        try {
            System.out.println("Servidor: Valor = " + new String(resposta.getGetWhen()));
        } catch (Exception e) {
            System.out.println("Servidor: Operação GETWHEN falhou");
        }
    }
}
