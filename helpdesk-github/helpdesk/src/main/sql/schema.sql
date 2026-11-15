CREATE DATABASE IF NOT EXISTS helpdesk
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE helpdesk;

CREATE TABLE IF NOT EXISTS usuarios (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nome           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    senha          VARCHAR(60)  NOT NULL,          -- hash BCrypt
    perfil         ENUM('SOLICITANTE', 'TECNICO') NOT NULL DEFAULT 'SOLICITANTE',
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    data_cadastro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categorias (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    descricao  VARCHAR(255),
    ativo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS solicitacoes (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    descricao         TEXT         NOT NULL,
    local_ocorrencia  VARCHAR(150),
    prioridade        ENUM('BAIXA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL,
    status            ENUM('ABERTA', 'EM_ANALISE', 'EM_ATENDIMENTO',
                           'AGUARDANDO_USUARIO', 'RESOLVIDA', 'CANCELADA')
                      NOT NULL DEFAULT 'ABERTA',
    data_abertura     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento   TIMESTAMP NULL,
    usuario_id        INT NOT NULL,
    tecnico_id        INT NULL,
    categoria_id      INT NOT NULL,

    CONSTRAINT fk_solicitacao_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuarios (id),
    CONSTRAINT fk_solicitacao_tecnico   FOREIGN KEY (tecnico_id)   REFERENCES usuarios (id),
    CONSTRAINT fk_solicitacao_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
);

CREATE TABLE IF NOT EXISTS interacoes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    mensagem        TEXT NOT NULL,
    data_interacao  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    solicitacao_id  INT NOT NULL,
    usuario_id      INT NOT NULL,

    CONSTRAINT fk_interacao_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes (id),
    CONSTRAINT fk_interacao_usuario     FOREIGN KEY (usuario_id)     REFERENCES usuarios (id)
);

-- Categorias iniciais
INSERT INTO categorias (nome, descricao) VALUES
    ('Hardware', 'Computadores, impressoras e periféricos'),
    ('Software', 'Instalação e erros de programas'),
    ('Rede',     'Internet, Wi-Fi e cabeamento'),
    ('Acesso',   'Senhas, contas e permissões');


