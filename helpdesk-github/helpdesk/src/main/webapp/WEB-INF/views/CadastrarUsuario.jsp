
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="util.Html, model.Usuario" %>
<%
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    boolean tecnicoLogado = logado != null && logado.isTecnico();
    boolean codigoTecnicoAtivo = Boolean.TRUE.equals(request.getAttribute("codigoTecnicoAtivo"));
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro de usuário | HelpDesk TI</title>
    
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/Estilos.css">

    
</head>
<body class=" tela-cadastro " >

    <main class="container">
        <section class="formulario">
            <h1>HelpDesk TI</h1>
            <h2>Criar conta</h2>
            <p>Preencha os dados para se cadastrar no sistema.</p>

            <% if (request.getAttribute("erro") != null) { %>
                <p class="mensagem-erro">
                    <%= Html.esc(request.getAttribute("erro")) %>
                </p>
            <% } %>

            <form action="${pageContext.request.contextPath}/cadastrar-usuario"
                  method="post">

                <label for="nome">Nome completo</label>
                <input type="text" id="nome" name="nome"
                       maxlength="100" required
                       value="<%= Html.esc(request.getParameter("nome")) %>">

                <label for="email">E-mail</label>
                <input type="email" id="email" name="email"
                       maxlength="150" required
                       value="<%= Html.esc(request.getParameter("email")) %>">

                <label for="senha">Senha</label>
                <input type="password" id="senha" name="senha"
                       minlength="6" maxlength="72" required>

                <label for="confirmarSenha">Confirmar senha</label>
                <input type="password" id="confirmarSenha"
                       name="confirmarSenha" minlength="6" maxlength="72" required>

                <%-- Só um técnico logado pode criar outros técnicos. --%>
                <% if (tecnicoLogado) { %>
                <label for="perfil">Tipo de usuário</label>
                <select id="perfil" name="perfil" required>
                    <option value="SOLICITANTE">Solicitante</option>
                    <option value="TECNICO">Técnico</option>
                </select>
                <% } else if (logado == null && codigoTecnicoAtivo) { %>
                <label for="codigoTecnico">Código do técnico <span class="opcional">(opcional)</span></label>
                <input type="password" id="codigoTecnico" name="codigoTecnico"
                       maxlength="100" autocomplete="off"
                       placeholder="Só para a equipe de TI">
                <p class="dica">Deixe em branco para criar uma conta de solicitante.</p>
                <% } %>

                <button type="submit">Cadastrar</button>
            </form>

            <p class="rodape">
                <% if (logado == null) { %>
                    Já possui uma conta?
                    <a href="${pageContext.request.contextPath}/login">
                        Fazer login
                    </a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/home">
                        Voltar ao painel
                    </a>
                <% } %>
            </p>
        </section>
    </main>

</body>
</html>
