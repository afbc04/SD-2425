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

        try {

            Socket s= new Socket(InetAddress.getByName(servidor_ip),porta);



        }
        catch (IOException e) {
            System.err.println("Erro no socket: " + e.getMessage());
            e.printStackTrace();
        }

    }


}
