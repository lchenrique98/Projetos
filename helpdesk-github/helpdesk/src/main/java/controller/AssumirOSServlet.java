package controller;

import java.io.IOException;
import java.sql.SQLException;

import dao.OrdemServicoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.OrdemServico;
import model.Usuario;

/**
 * O técnico logado assume a OS (vira o responsável).
 * Se a OS ainda estava "Aberta", passa para "Em atendimento".
 */
@WebServlet("/assumir")
public class AssumirOSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        if (!logado.isTecnico()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Apenas técnicos podem assumir chamados.");
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

        try {
            OrdemServico os = ordemServicoDAO.buscarPorId(id);
            if (os == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Ordem de serviço não encontrada.");
                return;
            }

            ordemServicoDAO.atribuirTecnico(id, logado.getId());

            if ("ABERTA".equals(os.getStatus())) {
                ordemServicoDAO.atualizarStatus(id, "EM_ATENDIMENTO");
            }

            response.sendRedirect(request.getContextPath()
                    + "/os?id=" + id + "&assumida=1");

        } catch (SQLException e) {
            throw new ServletException("Erro ao assumir a OS.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
