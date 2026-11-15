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
import model.OrdemServico;
import model.Usuario;
import util.Acesso;

/**
 * Detalhe de uma OS: dados do chamado, histórico de comentários,
 * campo para comentar e (para o técnico) assumir / mudar status.
 *
 * URL: /os?id=12
 */
@WebServlet("/os")
public class DetalheOSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();
    private final InteracoesDAO interacoesDAO = new InteracoesDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Número da OS inválido.");
            return;
        }

        try {
            OrdemServico os = ordemServicoDAO.buscarPorId(id);

            // Mesmo erro para "não existe" e "não é sua", para não revelar OS de outros.
            if (!Acesso.podeVer(logado, os)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Ordem de serviço não encontrada.");
                return;
            }

            request.setAttribute("os", os);
            request.setAttribute("comentarios",
                    interacoesDAO.listarPorSolicitacao(id));

            request.getRequestDispatcher("/WEB-INF/views/DetalheOS.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Erro ao carregar a OS.", e);
        }
    }
}
