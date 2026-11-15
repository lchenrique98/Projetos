
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="util.Html, model.Usuario" %>
<%
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Início | Help Desk</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/Estilos.css">
</head>
<body class=" tema-escuro tela-home " >

    <header class="topo">
        <h1>Help Desk</h1>
        <p>Sistema de gerenciamento de ordens de serviço</p>
    </header>

    <main class="container">
        <h2>Olá, <%= Html.esc(logado.getNome()) %>!</h2>
        <p>Escolha uma opção para continuar o atendimento.</p>

        <% if ("1".equals(request.getParameter("usuarioCadastrado"))) { %>
            <p class="mensagem-sucesso">Usuário cadastrado com sucesso.</p>
        <% } %>

        <section class="cards">

            <article class="card">
                <h3>Nova Ordem de Serviço</h3>
                <p>Registre um novo chamado e descreva o problema
                   ou serviço solicitado.</p>
                <a class="botao"
                   href="${pageContext.request.contextPath}/nova-os">
                    Abrir Nova OS
                </a>
            </article>

            <article class="card">
                <h3>Consultar Ordens</h3>
                <p>Consulte as ordens de serviço cadastradas e
                   acompanhe os atendimentos.</p>
                <a class="botao"
                   href="${pageContext.request.contextPath}/listar-os">
                    Consultar OS
                </a>
            </article>

            <% if (logado.isTecnico()) { %>
            <article class="card">
                <h3>Usuários</h3>
                <p>Acesse a página de cadastro de usuários do
                   sistema.</p>
                <a class="botao"
                   href="${pageContext.request.contextPath}/cadastrar-usuario">
                    Cadastrar usuário
                </a>
            </article>
            <% } %>

        </section>
    </main>

    <footer class="rodape">
        <p>Help Desk &copy; 2026 — Sistema de Ordens de Serviço</p>
        <p>
            <form action="${pageContext.request.contextPath}/logout"
                  method="post" style="display: inline;">
                <button type="submit" class="botao">Sair do sistema</button>
            </form>
        </p>
    </footer>

</body>
</html>
