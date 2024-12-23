package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Client.Cliente;
import Client.ClienteCredenciais;

public class GestorClientes {
    private Lock clientes_l = new ReentrantLock();
    private Map<ClienteCredenciais,Cliente> clientes = new HashMap<>();

    
    public void adicionarCliente(ClienteCredenciais credenciais, Cliente cliente){
        clientes_l.lock();
        try{
            clientes.put(credenciais, cliente);
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

    public Cliente getCliente(ClienteCredenciais credenciais){
        clientes_l.lock();
        try{
            return this.clientes.get(credenciais);
        } finally{
            clientes_l.unlock();
        }
    }

    public boolean existeCliente(ClienteCredenciais credenciais){
        clientes_l.lock();
        try{
            return this.clientes.containsKey(credenciais);
        } finally{
            clientes_l.unlock();
        }
    }
}
