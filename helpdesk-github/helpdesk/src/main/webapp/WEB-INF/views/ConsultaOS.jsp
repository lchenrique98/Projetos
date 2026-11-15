
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, util.Html, util.Rotulos, model.OrdemServico" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Consultar OS | HelpDesk TI</title>

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/Estilos.css">

    

</head>

<body class=" tema-escuro tela-consulta ">

    <header class="topo">
        <h1>
            <i class="fa-solid fa-ticket"></i>
            HelpDesk TI
        </h1>
        <p>Sistema de Ordens de Serviço</p>
    </header>

    <main class="container">

        <a class="voltar"
           href="${pageContext.request.contextPath}/home">
            <i class="fa-solid fa-arrow-left"></i>
            Voltar ao painel
        </a>

        <div class="titulo-pagina">
            <div>
                <h2>
                    <i class="fa-solid fa-clipboard-list"></i>
                    Consultar ordens de serviço
                </h2>
                <p>Visualize e acompanhe as solicitações de suporte.</p>
            </div>
        </div>

        <% if ("1".equals(request.getParameter("sucesso"))) { %>
            <p class="mensagem-sucesso">Ordem de serviço cadastrada com sucesso.</p>
        <% } %>

        <section class="painel">

            <div class="painel-topo">
                <h3>Minhas ordens de serviço</h3>
                <span class="contador">
                    <i class="fa-solid fa-list-check"></i>
                    Lista de OS
                </span>
            </div>

            <div class="tabela-responsiva">
                <table>
                    <thead>
                        <tr>
                            <th>Nº OS</th>
                            <th>Título</th>
                            <th>Categoria</th>
                            <th>Prioridade</th>
                            <th>Status</th>
                            <th>Técnico</th>
                            <th>Aberta em</th>
                            <th></th>
                        </tr>
                    </thead>

                    <tbody>
                        <%
                            @SuppressWarnings("unchecked")
                            List<OrdemServico> ordens =
                                (List<OrdemServico>) request.getAttribute("ordens");

                            if (ordens != null && !ordens.isEmpty()) {
                                for (OrdemServico os : ordens) {
                        %>
                        <tr>
                            <td>#<%= os.getId() %></td>

                            <td><%= Html.esc(os.getTitulo()) %></td>

                            <td><%= Html.esc(os.getCategoriaNome()) %></td>

                            <td>
                                <span class="selo <%= Rotulos.css("prioridade", os.getPrioridade()) %>">
                                    <%= Html.esc(Rotulos.prioridade(os.getPrioridade())) %>
                                </span>
                            </td>

                            <td>
                                <span class="selo <%= Rotulos.css("status", os.getStatus()) %>">
                                    <%= Html.esc(Rotulos.status(os.getStatus())) %>
                                </span>
                            </td>

                            <td>
                                <% if (os.getTecnicoNome() != null) { %>
                                    <%= Html.esc(os.getTecnicoNome()) %>
                                <% } else { %>
                                    <span class="sem-tecnico">Aguardando</span>
                                <% } %>
                            </td>

                            <td><%= Rotulos.dataHora(os.getDataAbertura()) %></td>

                            <td>
                                <a class="link-os"
                                   href="${pageContext.request.contextPath}/os?id=<%= os.getId() %>">
                                    Ver <i class="fa-solid fa-arrow-right"></i>
                                </a>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="8" class="vazio">
                                <i class="fa-solid fa-folder-open"></i>
                                <p>Nenhuma ordem de serviço para exibir.</p>
                                <span>
                                    As ordens cadastradas aparecerão aqui.
                                </span>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>

            <div class="painel-rodape">
                <a class="botao"
                   href="${pageContext.request.contextPath}/nova-os">
                    <i class="fa-solid fa-plus"></i>
                    Abrir nova OS
                </a>
            </div>

        </section>

    </main>

    <footer class="rodape">
        <p>HelpDesk TI — Sistema de suporte técnico</p>
    </footer>

</body>
</html>
