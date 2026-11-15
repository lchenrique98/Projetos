package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import dao.OrdemServicoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.OrdemServico;
import model.Usuario;

@WebServlet("/listar-os")
public class ConsultaOSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        // O técnico acompanha as OS pelo painel de atendimento.
        if (logado.isTecnico()) {
            response.sendRedirect(request.getContextPath() + "/painel");
            return;
        }

        try {
            // Solicitante vê só as OS que ele abriu.
            List<OrdemServico> ordens =
                    ordemServicoDAO.listarPorUsuario(logado.getId());

            request.setAttribute("ordens", ordens);

            request.getRequestDispatcher("/WEB-INF/views/ConsultaOS.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Erro ao consultar as ordens de serviço.", e);
        }
    }
}
