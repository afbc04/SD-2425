package Server;

import Serializacao.Mensagem;

public class Task {
    
    public Cliente cliente;
    public Mensagem mensagem;

    public Task(Cliente c, Mensagem m) {
        this.cliente = c;
        this.mensagem = m;
    }

}
