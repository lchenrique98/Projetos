
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, util.Html, model.Categorias" %>
<!DOCTYPE html>
<html lang="pt-br">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Nova OS | HelpDesk TI</title>

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/Estilos.css">

   
</head>

<body class=" tema-escuro tela-nova-os " >

    <header class="topo">
        <h1>
            <i class="fa-solid fa-ticket"></i>
            HelpDesk TI
        </h1>
        <p>Sistema de Ordens de Serviço</p>
    </header>

    <main class="container">

        <p>
            <a href="${pageContext.request.contextPath}/home">
                <i class="fa-solid fa-arrow-left"></i>
                Voltar ao painel
            </a>
        </p>

        <h2>
            <i class="fa-solid fa-file-circle-plus"></i>
            Abrir nova OS
        </h2>

        <p>Preencha os dados para registrar uma solicitação de suporte.</p>

        <% if (request.getAttribute("erro") != null) { %>
            <p class="mensagem-erro">
                <%= Html.esc(request.getAttribute("erro")) %>
            </p>
        <% } %>

        <section class="card">

            <form action="${pageContext.request.contextPath}/nova-os"
                  method="post">

                <label for="titulo">Título do problema</label>
                <input type="text"
                       id="titulo"
                       name="titulo"
                       maxlength="150"
                       placeholder="Ex.: Computador não liga"
                       required>

                <label for="descricao">Descrição do problema</label>
                <textarea id="descricao"
                          name="descricao"
                          placeholder="Descreva o problema encontrado..."
                          required></textarea>

                <label for="local_ocorrencia">Local da ocorrência</label>
                <input type="text"
                       id="local_ocorrencia"
                       name="local_ocorrencia"
                       maxlength="150"
                       placeholder="Ex.: Laboratório de informática">

                <label for="categoria">Categoria</label>
                <select id="categoria" name="categoria" required>
                    <option value="">Selecione uma categoria</option>

                    <%
                        @SuppressWarnings("unchecked")
                        List<Categorias> categorias =
                            (List<Categorias>) request.getAttribute("categorias");

                        if (categorias != null) {
                            for (Categorias categoria : categorias) {
                    %>
                    <option value="<%= categoria.getId() %>">
                        <%= Html.esc(categoria.getNome()) %>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>

                <label for="prioridade">Prioridade</label>
                <select id="prioridade" name="prioridade" required>
                    <option value="">Selecione a prioridade</option>
                    <option value="BAIXA">Baixa</option>
                    <option value="MEDIA">Média</option>
                    <option value="ALTA">Alta</option>
                    <option value="URGENTE">Urgente</option>
                </select>

                <div class="acoes-formulario">
                    <button type="submit">
                        <i class="fa-solid fa-paper-plane"></i>
                        Cadastrar OS
                    </button>

                    <a class="botao-cancelar"
                       href="${pageContext.request.contextPath}/home">
                        Cancelar
                    </a>
                </div>

            </form>

        </section>

    </main>

    <footer class="rodape">
        <p>HelpDesk TI — Sistema de suporte técnico</p>
    </footer>

</body>
</html>
