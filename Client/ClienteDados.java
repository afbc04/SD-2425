package Client;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClienteDados {
    
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    public boolean running;

    public String nome;

    public int taskID;
    public Queue<String> pedidos = new LinkedList<>();
    public Condition condPED = l.writeLock().newCondition(); // para os pedidos

    public Map<Integer,Dados> dados = new TreeMap<>();

    public ClienteDados() {

        this.running = false;
        this.taskID = 1;

    }

}
