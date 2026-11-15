package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bd.Conexao;
import model.OrdemServico;

public class OrdemServicoDAO {

    // Busca a OS junto com o nome da categoria, do solicitante e do técnico
    private static final String SELECT_COMPLETO =
            "SELECT s.id, s.titulo, s.descricao, "
            + "s.local_ocorrencia, s.prioridade, s.status, "
            + "s.data_abertura, s.data_fechamento, "
            + "s.usuario_id, s.tecnico_id, s.categoria_id, "
            + "c.nome AS categoria_nome, "
            + "u.nome AS usuario_nome, "
            + "t.nome AS tecnico_nome "
            + "FROM solicitacoes s "
            + "INNER JOIN categorias c ON s.categoria_id = c.id "
            + "INNER JOIN usuarios u ON s.usuario_id = u.id "
            + "LEFT JOIN usuarios t ON s.tecnico_id = t.id ";

    // Ordem do painel: urgentes primeiro, depois as mais recentes
    private static final String ORDEM =
            "ORDER BY FIELD(s.prioridade, 'URGENTE', 'ALTA', 'MEDIA', 'BAIXA'), "
            + "s.data_abertura DESC";

    // Cadastrar uma nova ordem de serviço
    public boolean cadastrar(OrdemServico os) throws SQLException {

        String sql = "INSERT INTO solicitacoes "
                + "(titulo, descricao, local_ocorrencia, prioridade, "
                + "usuario_id, tecnico_id, categoria_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, os.getTitulo());
            stmt.setString(2, os.getDescricao());

            if (os.getLocalOcorrencia() == null
                    || os.getLocalOcorrencia().isBlank()) {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(3, os.getLocalOcorrencia());
            }

            stmt.setString(4, os.getPrioridade());
            stmt.setInt(5, os.getUsuarioId());

            if (os.getTecnicoId() == null) {
                stmt.setNull(6, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(6, os.getTecnicoId());
            }

            stmt.setInt(7, os.getCategoriaId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Todas as OS (painel do técnico). status == null traz todas.
    // somenteTecnicoId != null traz só as atribuídas a esse técnico.
    public List<OrdemServico> listarParaPainel(String status,
                                               Integer somenteTecnicoId)
            throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_COMPLETO + "WHERE 1 = 1 ");
        if (status != null) {
            sql.append("AND s.status = ? ");
        }
        if (somenteTecnicoId != null) {
            sql.append("AND s.tecnico_id = ? ");
        }
        sql.append(ORDEM);

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (status != null) {
                stmt.setString(i++, status);
            }
            if (somenteTecnicoId != null) {
                stmt.setInt(i, somenteTecnicoId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return montarLista(rs);
            }
        }
    }

    // Apenas as OS abertas por um usuário (tela do solicitante)
    public List<OrdemServico> listarPorUsuario(int usuarioId)
            throws SQLException {

        String sql = SELECT_COMPLETO
                + "WHERE s.usuario_id = ? ORDER BY s.data_abertura DESC";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                return montarLista(rs);
            }
        }
    }

    // Uma OS pelo número
    public OrdemServico buscarPorId(int id) throws SQLException {

        String sql = SELECT_COMPLETO + "WHERE s.id = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                List<OrdemServico> lista = montarLista(rs);
                return lista.isEmpty() ? null : lista.get(0);
            }
        }
    }

    // Quantidade de OS em cada status (para os contadores do painel)
    public Map<String, Integer> contarPorStatus() throws SQLException {

        Map<String, Integer> contagem = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS total "
                + "FROM solicitacoes GROUP BY status";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                contagem.put(rs.getString("status"), rs.getInt("total"));
            }
        }

        return contagem;
    }

    // Atualizar o status de uma ordem de serviço
    public boolean atualizarStatus(int id, String status)
            throws SQLException {

        String sql = "UPDATE solicitacoes "
                + "SET status = ?, "
                + "data_fechamento = CASE "
                + "WHEN ? IN ('RESOLVIDA', 'CANCELADA') THEN CURRENT_TIMESTAMP "
                + "ELSE NULL END "
                + "WHERE id = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, status);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        }
    }

    // Define o técnico responsável pela OS
    public boolean atribuirTecnico(int id, int tecnicoId) throws SQLException {

        String sql = "UPDATE solicitacoes SET tecnico_id = ? WHERE id = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tecnicoId);
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;
        }
    }

    private List<OrdemServico> montarLista(ResultSet rs) throws SQLException {

        List<OrdemServico> lista = new ArrayList<>();

        while (rs.next()) {
            OrdemServico os = new OrdemServico();

            os.setId(rs.getInt("id"));
            os.setTitulo(rs.getString("titulo"));
            os.setDescricao(rs.getString("descricao"));
            os.setLocalOcorrencia(rs.getString("local_ocorrencia"));
            os.setPrioridade(rs.getString("prioridade"));
            os.setStatus(rs.getString("status"));
            os.setDataAbertura(rs.getTimestamp("data_abertura"));
            os.setDataFechamento(rs.getTimestamp("data_fechamento"));
            os.setUsuarioId(rs.getInt("usuario_id"));

            int tecnicoId = rs.getInt("tecnico_id");
            os.setTecnicoId(rs.wasNull() ? null : tecnicoId);

            os.setCategoriaId(rs.getInt("categoria_id"));
            os.setCategoriaNome(rs.getString("categoria_nome"));
            os.setUsuarioNome(rs.getString("usuario_nome"));
            os.setTecnicoNome(rs.getString("tecnico_nome"));

            lista.add(os);
        }

        return lista;
    }
}
