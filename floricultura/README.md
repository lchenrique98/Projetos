# Floricultura — Sistema de Gestão

Sistema web para gestão de uma floricultura, feito em Python com Django e MySQL.

## Funcionalidades

- **Produtos:** cadastro com foto, categoria, preço de custo e de venda, fornecedor e estoque mínimo.
- **Estoque:** entradas e baixas de estoque direto na lista de produtos.
- **Vendas:** registro de vendas com vários itens, forma de pagamento e histórico de compras por cliente.
- **Encomendas:** pedidos com data e hora de entrega, retirada ou entrega no endereço, sinal pago e controle de status.
- **Clientes:** cadastro com busca de endereço pelo CEP, edição, desativação e reativação.
- **Fornecedores:** cadastro de fornecedores ligados aos produtos.
- **Painel:** visão geral do sistema.

## Tecnologias

Python • Django 5.2 • MySQL • HTML • CSS • JavaScript

## Como rodar

1. Crie e ative um ambiente virtual:
   ```bash
   python -m venv ambiente
   ambiente\Scripts\activate
   ```
2. Instale as dependências:
   ```bash
   pip install -r requirements.txt
   ```
3. Crie o banco no MySQL:
   ```sql
   CREATE DATABASE GestaoFloricultura CHARACTER SET utf8mb4;
   ```
4. Copie `config/local_settings.exemplo.py` para `config/local_settings.py` e preencha a senha do MySQL.
   Esse arquivo fica fora do Git.
5. Crie as tabelas e um usuário administrador:
   ```bash
   python manage.py migrate
   python manage.py createsuperuser
   ```
6. Rode o servidor e acesse `http://127.0.0.1:8000`:
   ```bash
   python manage.py runserver
   ```

## Estrutura

```
config/         configurações do projeto (settings, urls)
core/           página inicial
floricultura/   produtos, estoque, vendas, encomendas e painel
clientes/       cadastro de clientes
fornecedores/   cadastro de fornecedores
usuarios/ financeiro/   apps reservados para próximas etapas
```
