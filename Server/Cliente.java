package Server;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Serializacao.Resposta;

public class Cliente {
    public Lock l;
    public Condition cond;
    private final Integer id;
    private String password;
    private Queue<Resposta> respostas;
    private ClienteCredenciais credenciais;


    public Cliente(String password, Integer id){
        //if(credenciais == null) throw new IllegalArgumentException("As credenciais do cliente não podem ser nulas");

        this.id = id;
        this.password = password;
        this.l = new ReentrantLock();
        this.cond = l.newCondition();
        this.respostas = new LinkedList<>();
        //this.credenciais = credenciais;
    }

    public boolean samePassword(String password) {
        return this.password.equals(password);
    }

    public ClienteCredenciais getCredenciais() {
        return this.credenciais;
    }


    public void insert_Resposta(Resposta resposta){
        respostas.add(resposta);
        this.cond.signal();
    }

    public Resposta remove_Resposta(){
        return respostas.poll(); //poll() - retorna e remove a cabeça da queue
    }

    public boolean has_Respostas(){
        return !respostas.isEmpty();
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
