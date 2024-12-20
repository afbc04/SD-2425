import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

        ServerSocket ss = null;

        try {

            ss= new ServerSocket(porta);

            Socket s = ss.accept();


            
        }
        catch (IOException e) {
            System.err.println("Erro no socket: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            ss.close();
        }
        catch (Exception e) {}
        
    }


}
