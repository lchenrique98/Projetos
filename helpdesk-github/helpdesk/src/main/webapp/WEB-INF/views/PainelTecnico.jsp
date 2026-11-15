<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map, util.Html, util.Rotulos, model.OrdemServico, model.Usuario" %>
<%
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");

    @SuppressWarnings("unchecked")
    List<OrdemServico> ordens = (List<OrdemServico>) request.getAttribute("ordens");

    @SuppressWarnings("unchecked")
    Map<String, Integer> contagem = (Map<String, Integer>) request.getAttribute("contagem");

    String filtroStatus = (String) request.getAttribute("filtroStatus");
    boolean filtroMinhas = Boolean.TRUE.equals(request.getAttribute("filtroMinhas"));
    boolean semFiltro = filtroStatus == null && !filtroMinhas;

    String ctx = request.getContextPath();

    // Cartões de resumo exibidos no topo
    String[] cartoes = {"ABERTA", "EM_ATENDIMENTO", "AGUARDANDO_USUARIO", "RESOLVIDA"};
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel do técnico | HelpDesk TI</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Estilos.css">
	<link rel="stylesheet"
    	  href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
 
</head>
<body class="tema-escuro tela-consulta ">

    <header class="topo">
        <div class="topo-linha">
            <div>
                <h1><i class="fa-solid fa-screwdriver-wrench"></i> HelpDesk TI</h1>
                <p>Painel de atendimento do técnico</p>
            </div>

            <div class="topo-usuario">
                <a class="link-topo" href="<%= ctx %>/cadastrar-usuario">
                    <i class="fa-solid fa-user-plus"></i> Cadastrar usuário
                </a>
                <span><i class="fa-solid fa-user-gear"></i> <%= Html.esc(logado.getNome()) %></span>
                <form action="<%= ctx %>/logout" method="post">
                    <button type="submit" class="botao-sair">Sair</button>
                </form>
            </div>
        </div>
    </header>

    <main class="container">

        <% if ("1".equals(request.getParameter("usuarioCadastrado"))) { %>
            <p class="mensagem-sucesso">Usuário cadastrado com sucesso.</p>
        <% } %>

        <section class="resumo">
            <% for (String codigo : cartoes) {
                   int total = contagem == null ? 0 : contagem.getOrDefault(codigo, 0);
            %>
            <a class="resumo-card <%= codigo.equals(filtroStatus) ? "ativo" : "" %>"
               href="<%= ctx %>/painel?status=<%= codigo %>">
                <strong><%= total %></strong>
                <span><%= Rotulos.status(codigo) %></span>
            </a>
            <% } %>
        </section>

        <section class="painel">

            <div class="painel-topo">
                <h3>Chamados</h3>

                <nav class="filtros">
                    <a class="filtro <%= semFiltro ? "ativo" : "" %>"
                       href="<%= ctx %>/painel">Todos</a>
                    <a class="filtro <%= filtroMinhas ? "ativo" : "" %>"
                       href="<%= ctx %>/painel?minhas=1">Atribuídos a mim</a>
                    <% for (Map.Entry<String, String> st : Rotulos.STATUS.entrySet()) { %>
                    <a class="filtro <%= st.getKey().equals(filtroStatus) ? "ativo" : "" %>"
                       href="<%= ctx %>/painel?status=<%= st.getKey() %>"><%= st.getValue() %></a>
                    <% } %>
                </nav>
            </div>

            <div class="tabela-responsiva">
                <table>
                    <thead>
                        <tr>
                            <th>Nº</th>
                            <th>Título</th>
                            <th>Solicitante</th>
                            <th>Categoria</th>
                            <th>Prioridade</th>
                            <th>Status</th>
                            <th>Técnico</th>
                            <th>Aberta em</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (ordens != null && !ordens.isEmpty()) {
                           for (OrdemServico os : ordens) { %>
                        <tr>
                            <td>#<%= os.getId() %></td>
                            <td><%= Html.esc(os.getTitulo()) %></td>
                            <td><%= Html.esc(os.getUsuarioNome()) %></td>
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
                                    <span class="sem-tecnico">Sem técnico</span>
                                <% } %>
                            </td>
                            <td><%= Rotulos.dataHora(os.getDataAbertura()) %></td>
                            <td>
                                <a class="link-os" href="<%= ctx %>/os?id=<%= os.getId() %>">
                                    Atender <i class="fa-solid fa-arrow-right"></i>
                                </a>
                            </td>
                        </tr>
                    <%     }
                       } else { %>
                        <tr>
                            <td colspan="9" class="vazio">
                                <i class="fa-solid fa-mug-hot"></i>
                                <p>Nenhum chamado neste filtro.</p>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <footer class="rodape">
        <p>HelpDesk TI — Sistema de suporte técnico</p>
    </footer>

</body>
</html>
