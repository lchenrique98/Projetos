package filtro;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Bloqueia qualquer página para quem não está logado,
 * exceto login, cadastro, CSS e a página inicial.
 */
@WebFilter("/*")
public class AutenticacaoFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
            "/", "/Index.jsp", "/login", "/cadastrar-usuario"
    );

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

        String caminho = request.getRequestURI()
                .substring(request.getContextPath().length());

        boolean publica = ROTAS_PUBLICAS.contains(caminho)
                || caminho.startsWith("/css/");

        HttpSession sessao = request.getSession(false);
        boolean logado = sessao != null
                && sessao.getAttribute("usuarioLogado") != null;

        if (publica || logado) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
