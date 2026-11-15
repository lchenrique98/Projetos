
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="util.Html" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HelpDesk TI | Login</title>

    <link rel="stylesheet"
    href="${pageContext.request.contextPath}/css/Estilos.css">
</head>
<body class="tema-claro " >

    <main class="login-container">
        <h1>HelpDesk TI</h1>
        <p class="subtitulo">Sistema de Ordens de Serviço</p>

        <% if ("sucesso".equals(request.getParameter("cadastro"))) { %>
            <p class="mensagem-sucesso">
                Cadastro realizado! Agora é só fazer login.
            </p>
        <% } %>

        <% if (request.getAttribute("erro") != null) { %>
            <p class="mensagem-erro">
                <%= Html.esc(request.getAttribute("erro")) %>
            </p>
        <% } %>

        <form action="${pageContext.request.contextPath}/login"
              method="post">

            <label for="txtEmail">E-mail</label>
            <input type="email"
                   id="txtEmail"
                   name="txtEmail"
                   placeholder="Digite seu e-mail"
                   required>

            <label for="txtSenha">Senha</label>
            <input type="password"
                   id="txtSenha"
                   name="txtSenha"
                   placeholder="Digite sua senha"
                   required>

            <button type="submit">Entrar no sistema</button>
        </form>

        <p class="subtitulo">
            Gerenciamento de chamados e suporte técnico
        </p>

        <div class="cadastro-link">
            <p>Não tem uma conta?</p>
            <a href="${pageContext.request.contextPath}/cadastrar-usuario">Cadastre-se</a>
        </div>

    </main>

</body>
</html>
