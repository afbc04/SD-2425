package Client;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClienteDados {
    
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    public final Socket socket;
    public boolean running;

    public String nome;

    public int taskID;
    public Queue<String> pedidos = new LinkedList<>();
    public Condition condPED = l.writeLock().newCondition(); // para os pedidos

    public Map<Integer,Dados> dados = new TreeMap<>();

    public ClienteDados(Socket s) {

        this.running = false;
        this.socket = s;
        this.taskID = 1;

    }

}
