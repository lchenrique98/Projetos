package controller;

import java.io.IOException;
import java.sql.SQLException;

import dao.OrdemServicoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Usuario;
import util.Rotulos;

/**
 * Painel de atendimento do técnico: contadores por status,
 * filtros e a lista de todas as ordens de serviço.
 *
 * Filtros (pela URL):
 *   /painel?status=EM_ATENDIMENTO  -> só um status
 *   /painel?minhas=1               -> só as atribuídas ao técnico logado
 */
@WebServlet("/painel")
public class PainelTecnicoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        if (!logado.isTecnico()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        String status = request.getParameter("status");
        if (!Rotulos.STATUS.containsKey(status)) {
            status = null;
        }

        boolean minhas = "1".equals(request.getParameter("minhas"));

        try {
            request.setAttribute("ordens", ordemServicoDAO.listarParaPainel(
                    status, minhas ? logado.getId() : null));
            request.setAttribute("contagem", ordemServicoDAO.contarPorStatus());
            request.setAttribute("filtroStatus", status);
            request.setAttribute("filtroMinhas", minhas);

            request.getRequestDispatcher("/WEB-INF/views/PainelTecnico.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Erro ao carregar o painel.", e);
        }
    }
}
