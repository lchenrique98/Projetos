package model;

import java.sql.Date;

public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private boolean ativo;
    private Date data_cadastro;

    public Usuario() {
    }

    public Usuario(int id, String nome, String email, String senha,
                   String perfil, boolean ativo, Date data_cadastro) {

        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
        this.data_cadastro = data_cadastro;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Date getDataCadastro() {
        return data_cadastro;
    }

    public void setDataCadastro(Date data_cadastro) {
        this.data_cadastro = data_cadastro;
    }

    public boolean isTecnico() {
        return "TECNICO".equals(perfil);
    }
}
