package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import dao.CategoriasDAO;
import dao.OrdemServicoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Categorias;
import model.OrdemServico;
import model.Usuario;

@WebServlet("/nova-os")
public class NovaOSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> PRIORIDADES_VALIDAS =
            Set.of("BAIXA", "MEDIA", "ALTA", "URGENTE");

    private final CategoriasDAO categoriasDAO = new CategoriasDAO();
    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        String titulo = request.getParameter("titulo");
        String descricao = request.getParameter("descricao");
        String local = request.getParameter("local_ocorrencia");
        String prioridade = request.getParameter("prioridade");

        int categoriaId;
        try {
            categoriaId = Integer.parseInt(request.getParameter("categoria"));
        } catch (NumberFormatException e) {
            categoriaId = 0;
        }

        if (titulo == null || titulo.isBlank()
                || descricao == null || descricao.isBlank()
                || !PRIORIDADES_VALIDAS.contains(prioridade)
                || categoriaId <= 0) {
            request.setAttribute("erro",
                    "Preencha título, descrição, categoria e prioridade.");
            mostrarFormulario(request, response);
            return;
        }

        try {
            OrdemServico os = new OrdemServico();
            os.setTitulo(titulo.trim());
            os.setDescricao(descricao.trim());
            os.setLocalOcorrencia(local == null ? null : local.trim());
            os.setPrioridade(prioridade);
            os.setCategoriaId(categoriaId);
            // A OS fica no nome de quem está logado.
            os.setUsuarioId(logado.getId());

            if (ordemServicoDAO.cadastrar(os)) {
                response.sendRedirect(request.getContextPath()
                        + "/listar-os?sucesso=1");
            } else {
                request.setAttribute("erro",
                        "Não foi possível cadastrar a OS.");
                mostrarFormulario(request, response);
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Erro ao cadastrar a ordem de serviço.", e);
        }
    }

    private void mostrarFormulario(HttpServletRequest request,
                                   HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Categorias> categorias = categoriasDAO.listarCategorias();
            request.setAttribute("categorias", categorias);

            request.getRequestDispatcher("/WEB-INF/views/NovaOS.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Erro ao carregar as categorias.", e);
        }
    }
}
