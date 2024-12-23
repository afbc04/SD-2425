package Server;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Serializacao.Resposta;

public class Cliente {
    public Lock l;
    private final Integer id;
    private Queue<Resposta> respostas;
    private ClienteCredenciais credenciais;


    public Cliente(ClienteCredenciais credenciais, Integer id){
        if(credenciais == null) throw new IllegalArgumentException("As credenciais do cliente não podem ser nulas");

        this.id = id;
        this.l = new ReentrantLock();
        this.respostas = new LinkedList<>();
        this.credenciais = credenciais;
    }

    public ClienteCredenciais getCredenciais() {
        return this.credenciais;
    }


    public void insert_Resposta(Resposta resposta){
        l.lock();
        try{
            respostas.add(resposta);
        } finally{
            l.unlock();
        }
    }

    public Resposta remove_Resposta(){
        l.lock();
        try{
            return respostas.poll(); //poll() - retorna e remove a cabeça da queue
        } finally{
            l.unlock();
        }
    }

    public boolean has_Respostas(){
        l.lock();
        try{
            return !respostas.isEmpty();
        } finally{
            l.unlock();
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if((obj == null) || (this.getClass() != obj.getClass())) return false;

        Cliente cliente = (Cliente) obj;
        return this.credenciais.equals(cliente.getCredenciais());
    }

    @Override
    public int hashCode() {
        return 17*31*this.credenciais.hashCode();
    }
}
