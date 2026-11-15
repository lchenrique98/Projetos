package model;

import java.sql.Timestamp;

/**
 * Um comentário feito em uma ordem de serviço
 * (pelo solicitante ou pelo técnico).
 */
public class Interacoes {

    private int id;
    private String mensagem;
    private Timestamp data_interacao;
    private int solicitacao_id;
    private int usuario_id;

    // Preenchidos na listagem (JOIN com usuarios)
    private String usuarioNome;
    private String usuarioPerfil;

    public Interacoes() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Timestamp getData_interacao() {
        return data_interacao;
    }

    public void setData_interacao(Timestamp data_interacao) {
        this.data_interacao = data_interacao;
    }

    public int getSolicitacao_id() {
        return solicitacao_id;
    }

    public void setSolicitacao_id(int solicitacao_id) {
        this.solicitacao_id = solicitacao_id;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioPerfil() {
        return usuarioPerfil;
    }

    public void setUsuarioPerfil(String usuarioPerfil) {
        this.usuarioPerfil = usuarioPerfil;
    }

    public boolean isDoTecnico() {
        return "TECNICO".equals(usuarioPerfil);
    }
}
