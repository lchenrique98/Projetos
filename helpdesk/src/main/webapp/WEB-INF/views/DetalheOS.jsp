<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map, util.Html, util.Rotulos, model.OrdemServico, model.Interacoes, model.Usuario" %>
<%
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    boolean tecnico = logado.isTecnico();

    OrdemServico os = (OrdemServico) request.getAttribute("os");

    @SuppressWarnings("unchecked")
    List<Interacoes> comentarios = (List<Interacoes>) request.getAttribute("comentarios");

    String ctx = request.getContextPath();
    String voltarUrl = ctx + (tecnico ? "/painel" : "/listar-os");
    boolean souResponsavel = os.getTecnicoId() != null && os.getTecnicoId() == logado.getId();
    String erro = request.getParameter("erro");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OS #<%= os.getId() %> | HelpDesk TI</title>

    <link rel="stylesheet" href="<%= ctx %>/css/Estilos.css">

</head>
<body class="tema-escuro tela-consulta "  >

    <header class="topo">
        <div class="topo-linha">
            <div>
                <h1><i class="fa-solid fa-ticket"></i> HelpDesk TI</h1>
                <p><%= tecnico ? "Painel de atendimento do técnico" : "Sistema de Ordens de Serviço" %></p>
            </div>
            <div class="topo-usuario">
                <span><i class="fa-solid fa-user"></i> <%= Html.esc(logado.getNome()) %></span>
                <form action="<%= ctx %>/logout" method="post">
                    <button type="submit" class="botao-sair">Sair</button>
                </form>
            </div>
        </div>
    </header>

    <main class="container">

        <a class="voltar" href="<%= voltarUrl %>">
            <i class="fa-solid fa-arrow-left"></i>
            <%= tecnico ? "Voltar ao painel" : "Voltar às minhas OS" %>
        </a>

        <div class="titulo-pagina">
            <h2>
                <i class="fa-solid fa-clipboard-list"></i>
                OS #<%= os.getId() %> — <%= Html.esc(os.getTitulo()) %>
            </h2>
            <p>
                <span class="selo <%= Rotulos.css("status", os.getStatus()) %>">
                    <%= Html.esc(Rotulos.status(os.getStatus())) %>
                </span>
                &nbsp;
                <span class="selo <%= Rotulos.css("prioridade", os.getPrioridade()) %>">
                    Prioridade <%= Html.esc(Rotulos.prioridade(os.getPrioridade())) %>
                </span>
            </p>
        </div>

        <% if ("1".equals(request.getParameter("atualizado"))) { %>
            <p class="mensagem-sucesso">Status atualizado.</p>
        <% } else if ("1".equals(request.getParameter("assumida"))) { %>
            <p class="mensagem-sucesso">Você agora é o técnico responsável por esta OS.</p>
        <% } else if ("1".equals(request.getParameter("comentado"))) { %>
            <p class="mensagem-sucesso">Comentário enviado.</p>
        <% } else if ("vazio".equals(erro)) { %>
            <p class="mensagem-erro">Escreva um comentário antes de enviar.</p>
        <% } else if ("grande".equals(erro)) { %>
            <p class="mensagem-erro">O comentário pode ter no máximo 2000 caracteres.</p>
        <% } %>

        <div class="detalhe-grade">

            <%-- ===== COLUNA PRINCIPAL ===== --%>
            <div>
                <section class="bloco">
                    <h3><i class="fa-solid fa-align-left"></i> Descrição do problema</h3>
                    <p class="descricao"><%= Html.esc(os.getDescricao()) %></p>
                </section>

                <section class="bloco" id="comentarios">
                    <h3><i class="fa-solid fa-comments"></i> Comentários</h3>

                    <% if (comentarios == null || comentarios.isEmpty()) { %>
                        <p class="sem-comentarios">
                            Nenhum comentário ainda.
                            <%= tecnico ? "Deixe uma mensagem para o solicitante." : "Se quiser, acrescente informações para o técnico." %>
                        </p>
                    <% } else { %>
                        <div class="comentarios">
                        <% for (Interacoes c : comentarios) { %>
                            <article class="comentario <%= c.isDoTecnico() ? "tecnico" : "" %>">
                                <div class="comentario-cabecalho">
                                    <span>
                                        <strong><%= Html.esc(c.getUsuarioNome()) %></strong>
                                        <% if (c.isDoTecnico()) { %><span class="etiqueta-tecnico">Técnico</span><% } %>
                                    </span>
                                    <span><%= Rotulos.dataHora(c.getData_interacao()) %></span>
                                </div>
                                <p><%= Html.esc(c.getMensagem()) %></p>
                            </article>
                        <% } %>
                        </div>
                    <% } %>

                    <form action="<%= ctx %>/comentar" method="post">
                        <input type="hidden" name="id" value="<%= os.getId() %>">
                        <label for="mensagem">Novo comentário</label>
                        <textarea id="mensagem" name="mensagem" maxlength="2000" required
                                  placeholder="<%= tecnico ? "Ex.: Troquei o cabo de rede, pode testar?" : "Ex.: O problema voltou a acontecer hoje de manhã." %>"></textarea>
                        <button type="submit">
                            <i class="fa-solid fa-paper-plane"></i> Enviar comentário
                        </button>
                    </form>
                </section>
            </div>

            <%-- ===== COLUNA LATERAL ===== --%>
            <aside>
                <section class="bloco">
                    <h3><i class="fa-solid fa-circle-info"></i> Dados do chamado</h3>
                    <dl class="dados">
                        <dt>Solicitante</dt>
                        <dd><%= Html.esc(os.getUsuarioNome()) %></dd>

                        <dt>Categoria</dt>
                        <dd><%= Html.esc(os.getCategoriaNome()) %></dd>

                        <dt>Local</dt>
                        <dd><%= os.getLocalOcorrencia() == null || os.getLocalOcorrencia().isBlank()
                                ? "—" : Html.esc(os.getLocalOcorrencia()) %></dd>

                        <dt>Técnico</dt>
                        <dd>
                            <% if (os.getTecnicoNome() != null) { %>
                                <%= Html.esc(os.getTecnicoNome()) %>
                            <% } else { %>
                                <span class="sem-tecnico">Ainda sem técnico</span>
                            <% } %>
                        </dd>

                        <dt>Aberta em</dt>
                        <dd><%= Rotulos.dataHora(os.getDataAbertura()) %></dd>

                        <% if (os.getDataFechamento() != null) { %>
                        <dt>Encerrada em</dt>
                        <dd><%= Rotulos.dataHora(os.getDataFechamento()) %></dd>
                        <% } %>
                    </dl>
                </section>

                <% if (tecnico) { %>
                <section class="bloco">
                    <h3><i class="fa-solid fa-screwdriver-wrench"></i> Atendimento</h3>

                    <% if (!souResponsavel) { %>
                    <form action="<%= ctx %>/assumir" method="post">
                        <input type="hidden" name="id" value="<%= os.getId() %>">
                        <button type="submit" class="secundario">
                            <i class="fa-solid fa-hand"></i>
                            <%= os.getTecnicoId() == null ? "Assumir este chamado" : "Assumir no lugar de " + Html.esc(os.getTecnicoNome()) %>
                        </button>
                    </form>
                    <% } else { %>
                        <p class="aviso"><i class="fa-solid fa-check"></i> Você é o responsável por este chamado.</p>
                    <% } %>

                    <form action="<%= ctx %>/atualizar-status" method="post">
                        <input type="hidden" name="id" value="<%= os.getId() %>">

                        <label for="status">Status</label>
                        <select id="status" name="status" required>
                            <% for (Map.Entry<String, String> st : Rotulos.STATUS.entrySet()) { %>
                            <option value="<%= st.getKey() %>"
                                <%= st.getKey().equals(os.getStatus()) ? "selected" : "" %>>
                                <%= st.getValue() %>
                            </option>
                            <% } %>
                        </select>

                        <label for="comentario">Comentário (opcional)</label>
                        <textarea id="comentario" name="comentario" maxlength="2000"
                                  placeholder="Ex.: Aguardando peça chegar."></textarea>

                        <button type="submit">
                            <i class="fa-solid fa-rotate"></i> Atualizar status
                        </button>
                    </form>
                    <p class="aviso">A mudança de status fica registrada nos comentários.</p>
                </section>
                <% } %>
            </aside>

        </div>
    </main>

    <footer class="rodape">
        <p>HelpDesk TI — Sistema de suporte técnico</p>
    </footer>

</body>
</html>
