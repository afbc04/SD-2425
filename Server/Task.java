package Server;

import Serializacao.Mensagem;

public class Task {
    
    public final Cliente cliente;
    public final Mensagem mensagem;

    public Task(Cliente c, Mensagem m) {
        this.cliente = c;
        this.mensagem = m;
    }

}
