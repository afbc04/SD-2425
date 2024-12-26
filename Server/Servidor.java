package Server;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {
    
    public final int S;
    public final int N;

    public Queue<Socket> clientesWaiting = new LinkedList<>();
    public Queue<Task> mensagens = new LinkedList<>();
    public Lock l = new ReentrantLock(); // lock global
    public Condition condGab = l.newCondition(); // para os Gabinetes
    public Condition condTask = l.newCondition(); // para os TaskHandlers

    public Armazem armazem = new Armazem();
    public GestorClientes clientes = new GestorClientes();

    public Servidor(int S, int N) {

        this.S = S;
        this.N = N;

    }

}
