package Server;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Serializacao.Mensagem;

public class Servidor {
    
    public  final int S = 3;

    public final Queue<Socket> clientesWaiting = new LinkedList<>();
    public final Queue<Mensagem> mensagens = new LinkedList<>();
    public final Lock l = new ReentrantLock(); // lock global
    public final Condition condGab = l.newCondition(); // para os Gabinetes
    public final Condition condTask = l.newCondition(); // para os TaskHandlers

    public Armazem armazem = new Armazem();
    public GestorClientes clientes = new GestorClientes();

    public Servidor() {

    }

}
