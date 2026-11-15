package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bd.Conexao;
import model.Interacoes;

/**
 * Comentários (interações) das ordens de serviço.
 */
public class InteracoesDAO {

    // Salva um comentário com a data/hora atual do banco
    public void salvar(Interacoes interacao) throws SQLException {

        String sql = "INSERT INTO interacoes "
                + "(mensagem, data_interacao, solicitacao_id, usuario_id) "
                + "VALUES (?, NOW(), ?, ?)";

        try (Connection conexao = Conexao.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, interacao.getMensagem());
            comando.setInt(2, interacao.getSolicitacao_id());
            comando.setInt(3, interacao.getUsuario_id());

            comando.executeUpdate();
        }
    }

    // Lista os comentários de uma OS, do mais antigo para o mais novo
    public List<Interacoes> listarPorSolicitacao(int solicitacaoId)
            throws SQLException {

        String sql = "SELECT i.id, i.mensagem, i.data_interacao, "
                + "i.solicitacao_id, i.usuario_id, "
                + "u.nome AS usuario_nome, u.perfil AS usuario_perfil "
                + "FROM interacoes i "
                + "INNER JOIN usuarios u ON i.usuario_id = u.id "
                + "WHERE i.solicitacao_id = ? "
                + "ORDER BY i.data_interacao, i.id";

        List<Interacoes> lista = new ArrayList<>();

        try (Connection conexao = Conexao.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, solicitacaoId);

            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    Interacoes i = new Interacoes();
                    i.setId(rs.getInt("id"));
                    i.setMensagem(rs.getString("mensagem"));
                    i.setData_interacao(rs.getTimestamp("data_interacao"));
                    i.setSolicitacao_id(rs.getInt("solicitacao_id"));
                    i.setUsuario_id(rs.getInt("usuario_id"));
                    i.setUsuarioNome(rs.getString("usuario_nome"));
                    i.setUsuarioPerfil(rs.getString("usuario_perfil"));
                    lista.add(i);
                }
            }
        }

        return lista;
    }
}
