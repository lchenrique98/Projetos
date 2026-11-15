package controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

import bd.Conexao;

import dao.UsuarioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

/**
 * Cadastro de usuários.
 *
 * - Visitante (não logado): cria uma conta de SOLICITANTE.
 *   Se digitar o código do técnico certo (tecnico.codigo no db.properties),
 *   a conta é criada como TECNICO.
 * - Técnico logado: pode escolher o perfil (SOLICITANTE ou TECNICO).
 * - Solicitante logado: não pode cadastrar outros usuários.
 */
@WebServlet("/cadastrar-usuario")
public class CadastrarUsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/CadastrarUsuario.jsp";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Usuario logado = usuarioLogado(request);

        if (logado != null && !logado.isTecnico()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Apenas técnicos podem cadastrar usuários.");
            return;
        }

        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Usuario logado = usuarioLogado(request);

        if (logado != null && !logado.isTecnico()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Apenas técnicos podem cadastrar usuários.");
            return;
        }

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");
        String confirmarSenha = request.getParameter("confirmarSenha");

        String perfil = "SOLICITANTE";
        String erro = null;

        if (logado != null) {
            // Técnico logado escolhe o perfil no formulário.
            if ("TECNICO".equals(request.getParameter("perfil"))) {
                perfil = "TECNICO";
            }
        } else {
            // Visitante: vira técnico só com o código certo.
            String codigo = request.getParameter("codigoTecnico");
            if (codigo != null && !codigo.isBlank()) {
                if (codigoTecnicoConfere(codigo.trim())) {
                    perfil = "TECNICO";
                } else {
                    erro = "Código do técnico inválido. Deixe em branco para "
                         + "criar uma conta de solicitante.";
                }
            }
        }

        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || senha == null || senha.isBlank()) {
            erro = "Preencha todos os campos.";
        } else if (senha.length() < 6) {
            erro = "A senha precisa ter pelo menos 6 caracteres.";
        } else if (senha.length() > 72) {
            erro = "A senha pode ter no máximo 72 caracteres.";
        } else if (!senha.equals(confirmarSenha)) {
            erro = "As senhas não conferem.";
        }

        if (erro != null) {
            voltarComErro(request, response, erro);
            return;
        }

        try {
            // O UsuarioDAO gera o hash BCrypt da senha.
            boolean cadastrado = usuarioDAO.cadastrar(
                    nome.trim(), email.trim(), senha, perfil);

            if (!cadastrado) {
                voltarComErro(request, response,
                        "Não foi possível realizar o cadastro.");
                return;
            }

            if (logado != null) {
                response.sendRedirect(request.getContextPath()
                        + "/painel?usuarioCadastrado=1");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/login?cadastro=sucesso");
            }

        } catch (SQLException e) {
            // 23000 = violação de chave única (e-mail já cadastrado).
            if ("23000".equals(e.getSQLState())) {
                voltarComErro(request, response,
                        "Este e-mail já está cadastrado.");
                return;
            }
            throw new ServletException("Erro ao cadastrar usuário.", e);
        }
    }

    private void voltarComErro(HttpServletRequest request,
                               HttpServletResponse response,
                               String mensagem)
            throws ServletException, IOException {

        request.setAttribute("erro", mensagem);
        mostrarFormulario(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request,
                                   HttpServletResponse response)
            throws ServletException, IOException {

        // O campo "Código do técnico" só aparece se houver código configurado.
        request.setAttribute("codigoTecnicoAtivo", codigoConfigurado() != null);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String codigoConfigurado() {
        String codigo = Conexao.configuracao("TECNICO_CODIGO", "tecnico.codigo");
        // O valor de exemplo não vale como código.
        return "TROQUE_ESTE_CODIGO".equals(codigo) ? null : codigo;
    }

    private static boolean codigoTecnicoConfere(String digitado) {
        String esperado = codigoConfigurado();
        if (esperado == null) {
            return false;
        }
        // Comparação em tempo constante (não revela por tentativa quantos
        // caracteres estão certos).
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                digitado.getBytes(StandardCharsets.UTF_8));
    }

    private Usuario usuarioLogado(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        return sessao == null ? null
                : (Usuario) sessao.getAttribute("usuarioLogado");
    }
}
