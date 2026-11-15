package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Usuario;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // O AutenticacaoFilter já garante que há um usuário logado.
        Usuario logado = (Usuario) request.getSession()
                .getAttribute("usuarioLogado");

        // O técnico tem uma página própria.
        if (logado.isTecnico()) {
            response.sendRedirect(request.getContextPath() + "/painel");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/Home.jsp")
               .forward(request, response);
    }
}
