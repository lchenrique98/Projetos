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
import util.Acesso;

/**
 * Adiciona um comentário a uma OS.
 * Técnico pode comentar em qualquer OS; solicitante, só nas dele.
 */
@WebServlet("/comentar")
public class ComentarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int TAMANHO_MAXIMO = 2000;

    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();
    private final InteracoesDAO interacoesDAO = new InteracoesDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

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

        String mensagem = request.getParameter("mensagem");
        String voltar = request.getContextPath() + "/os?id=" + id;

        if (mensagem == null || mensagem.isBlank()) {
            response.sendRedirect(voltar + "&erro=vazio#comentarios");
            return;
        }
        if (mensagem.length() > TAMANHO_MAXIMO) {
            response.sendRedirect(voltar + "&erro=grande#comentarios");
            return;
        }

        try {
            OrdemServico os = ordemServicoDAO.buscarPorId(id);

            if (!Acesso.podeVer(logado, os)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Ordem de serviço não encontrada.");
                return;
            }

            Interacoes comentario = new Interacoes();
            comentario.setMensagem(mensagem.trim());
            comentario.setSolicitacao_id(id);
            comentario.setUsuario_id(logado.getId());
            interacoesDAO.salvar(comentario);

            response.sendRedirect(voltar + "&comentado=1#comentarios");

        } catch (SQLException e) {
            throw new ServletException("Erro ao salvar o comentário.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
