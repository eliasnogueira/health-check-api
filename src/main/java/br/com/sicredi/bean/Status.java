package br.com.sicredi.bean;

public class Status {

    public String nome;
    public String url;
    public boolean status;

    public Status(String nome, String url, boolean status) {
        this.nome = nome;
        this.url = url;
        this.status = status;
    }

    public String getNome() {

        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
