package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GestorClientes {
    private Lock clientes_l = new ReentrantLock();
    private Map<String,Cliente> clientes = new HashMap<>();

    public GestorClientes() {
    }
    
    public Cliente adicionarCliente(String nome, String password){
        this.clientes_l.lock();
        try{

            //ClienteCredenciais cc = new ClienteCredenciais(nome, password);
            Cliente c = this.clientes.get(nome);
            
            //Cliente não existe
            if (c == null) {
                c = new Cliente(password);
                this.clientes.put(nome,c);
                return c;
            }
            //Cliente já existia
            else {
                return null;
            }

        } finally{
            this.clientes_l.unlock();
        }
    }

    /*
    public void removerCliente(ClienteCredenciais cliente){
        clientes_l.lock();
        try{
            this.clientes.remove(cliente);
        } finally{
            clientes_l.unlock();
        }
    }
    */

    public Cliente getCliente(String nome, String password){
        clientes_l.lock();
        try{

            Cliente c = this.clientes.get(nome);

            if (c != null) {

                c.l.lock();
                if (c.samePassword(password) == false) {
                    c.l.unlock();
                    c = null;
                }
                else {

                    c.l.unlock();
                }
                
            }

            return c;
            
        } finally{
            clientes_l.unlock();
        }
    }

    /*
    public boolean existeCliente(ClienteCredenciais credenciais){
        clientes_l.lock();
        try{
            return this.clientes.containsKey(credenciais);
        } finally{
            clientes_l.unlock();
        }
    }*/
}
