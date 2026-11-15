# HelpDesk TI

Sistema de ordens de serviço em Java (Servlets + JSP + MySQL), rodando no Tomcat 11 com Java 21.

## Como rodar

1. **Banco:** rode `src/main/sql/schema.sql` no MySQL (cria as tabelas e algumas categorias).
2. **Credenciais:** copie `src/main/resources/db.properties.exemplo` para `db.properties`
   na mesma pasta e preencha usuário e senha. Esse arquivo fica fora do Git.
   (Outra opção: definir as variáveis de ambiente `DB_URL`, `DB_USUARIO` e `DB_SENHA`.)
3. **Eclipse:** *File → Import → Maven → Existing Maven Projects* e escolha a pasta do projeto.
   Depois rode no servidor Tomcat 11 configurado no Eclipse.
4. Acesse `http://localhost:8080/helpdesk`.

## Perfis

- **Solicitante:** abre OS e acompanha só as próprias.
- **Técnico:** vê todas as OS no painel de atendimento, assume chamados, altera o status,
  comenta e cadastra outros usuários (inclusive técnicos).

O cadastro público cria solicitantes. Para criar um técnico pela tela de cadastro, preencha o
campo **Código do técnico** com o valor de `tecnico.codigo` do `db.properties`. Outra opção é
cadastrar-se e rodar:

```sql
UPDATE usuarios SET perfil = 'TECNICO' WHERE email = 'seu@email.com';
```

## Tecnologias

Java 21 • Jakarta Servlets e JSP • MySQL • Tomcat 11 • Maven • BCrypt (senhas) • HTML e CSS

## Estrutura

```
src/main/java
  bd/          conexão com o banco
  controller/  servlets (uma rota cada)
  dao/         acesso ao banco
  filtro/      AutenticacaoFilter: exige login em todas as páginas privadas
  model/       classes de domínio
  util/        Html.esc() para exibir texto com segurança (anti-XSS)
src/main/webapp
  WEB-INF/views/  JSPs (não podem ser abertos direto pela URL)
  css/Estilos.css  um arquivo de estilos para todas as telas (classe no <body>)
src/main/sql/schema.sql
```
