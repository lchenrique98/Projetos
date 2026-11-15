package controller;

import java.io.IOException;
import java.sql.SQLException;

import dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;
import util.Acesso;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Quem já está logado vai direto para o painel.
        HttpSession sessao = request.getSession(false);
        if (sessao != null && sessao.getAttribute("usuarioLogado") != null) {
            Usuario logado = (Usuario) sessao.getAttribute("usuarioLogado");
            response.sendRedirect(request.getContextPath()
                    + Acesso.paginaInicial(logado));
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/Login.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("txtEmail");
        String senha = request.getParameter("txtSenha");

        if (email == null || email.isBlank()
                || senha == null || senha.isEmpty()) {
            mostrarErro(request, response);
            return;
        }

        try {
            Usuario usuario = usuarioDAO.logar(email.trim(), senha);

            if (usuario == null) {
                mostrarErro(request, response);
                return;
            }

            HttpSession sessao = request.getSession();
            // Gera um novo ID de sessão após o login (evita session fixation).
            request.changeSessionId();
            sessao.setAttribute("usuarioLogado", usuario);

            // Técnico vai para o painel de atendimento; solicitante, para o início.
            response.sendRedirect(request.getContextPath()
                    + Acesso.paginaInicial(usuario));

        } catch (SQLException e) {
            throw new ServletException("Erro ao autenticar usuário.", e);
        }
    }

    private void mostrarErro(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("erro", "E-mail ou senha inválidos.");
        request.getRequestDispatcher("/WEB-INF/views/Login.jsp")
               .forward(request, response);
    }
}
