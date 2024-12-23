package Server;

public class ClienteCredenciais {
    private String name;
    private String password;

    public ClienteCredenciais(String name, String password){
        if(name==null || password==null) throw new IllegalArgumentException("Campos não podem ser nulos");

        this.name = name;
        this.password = password;
    }


    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public int hashCode() {
        int res = 17;

        if(name == null || password == null) throw new IllegalArgumentException("Campos não podem ser nulos");

        res = 31 * res + name.hashCode();
        res = 31 * res + password.hashCode();

        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if((obj == null) || (this.getClass() != obj.getClass())) return false;

        ClienteCredenciais cc = (ClienteCredenciais) obj;
        return (this.name.equals(cc.getName()) && this.password.equals(cc.getPassword()));
    }
}

/*
    Lock clientes_l;
    private Map<ClienteCredenciais,Cliente> clientes;

    private class ClienteCredenciais {
        String username;
        String password;
    }

    private class Cliente {

        Lock l;
        Integer id;
        Queue<Task> respostas;


        void insert_Resposta(Task t);
        Task remove_Resposta();
        boolean has_Respostas();


    }
*/