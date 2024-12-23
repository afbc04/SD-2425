package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GestorClientes {
    private Lock clientes_l = new ReentrantLock();
    private Map<ClienteCredenciais,Cliente> clientes = new HashMap<>();
    private Integer ids;

    public GestorClientes() {
        this.ids = 1;
    }
    
    public Cliente adicionarCliente(String nome, String password){
        this.clientes_l.lock();
        try{

            ClienteCredenciais cc = new ClienteCredenciais(nome, password);
            Cliente c = this.clientes.get(cc);
            
            //Cliente não existe
            if (c == null) {
                c = new Cliente(cc, ids);
                this.ids++;
                this.clientes.put(cc,c);
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

            ClienteCredenciais cc = new ClienteCredenciais(nome, password);
            return this.clientes.get(cc);
            
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
