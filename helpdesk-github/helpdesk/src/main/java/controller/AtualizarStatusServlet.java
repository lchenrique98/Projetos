package controller;

import java.io.IOException;
import java.sql.SQLException;

import dao.InteracoesDAO;
import dao.OrdemServicoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Interacoes;
import model.OrdemServico;
import model.Usuario;
import util.Rotulos;

/**
 * O técnico muda o status de uma OS.
 * - Se a OS ainda não tinha técnico, quem mudou o status vira o responsável.
 * - Se vier um comentário junto, ele é salvo no histórico.
 * - A mudança de status também fica registrada no histórico.
 */
@WebServlet("/atualizar-status")
public class AtualizarStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();
    private final InteracoesDAO interacoesDAO = new InteracoesDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Só técnicos podem alterar o status de uma OS.
        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");
        if (!logado.isTecnico()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Apenas técnicos podem alterar o status.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Número da OS inválido.");
            return;
        }

        String status = request.getParameter("status");
        if (!Rotulos.STATUS.containsKey(status)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Status inválido.");
            return;
        }

        String comentario = request.getParameter("comentario");

        try {
            OrdemServico os = ordemServicoDAO.buscarPorId(id);
            if (os == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Ordem de serviço não encontrada.");
                return;
            }

            boolean mudou = !status.equals(os.getStatus());

            if (mudou) {
                ordemServicoDAO.atualizarStatus(id, status);
            }

            if (os.getTecnicoId() == null) {
                ordemServicoDAO.atribuirTecnico(id, logado.getId());
            }

            // Registra no histórico: a mudança de status e/ou o comentário.
            StringBuilder texto = new StringBuilder();
            if (mudou) {
                texto.append("Status alterado para \"")
                     .append(Rotulos.status(status)).append("\".");
            }
            if (comentario != null && !comentario.isBlank()) {
                if (texto.length() > 0) {
                    texto.append("\n\n");
                }
                texto.append(comentario.trim());
            }
            if (texto.length() > 0) {
                Interacoes registro = new Interacoes();
                registro.setMensagem(texto.toString());
                registro.setSolicitacao_id(id);
                registro.setUsuario_id(logado.getId());
                interacoesDAO.salvar(registro);
            }

            response.sendRedirect(request.getContextPath()
                    + "/os?id=" + id + "&atualizado=1");

        } catch (SQLException e) {
            throw new ServletException(
                    "Erro ao atualizar o status da OS.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Utilize o formulário para atualizar o status.");
    }
}
