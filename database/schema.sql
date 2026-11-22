DROP TABLE IF EXISTS resposta CASCADE;
DROP TABLE IF EXISTS submissao_anonima CASCADE;
DROP TABLE IF EXISTS avaliacao_destinatario CASCADE;
DROP TABLE IF EXISTS avaliacao_questao CASCADE;
DROP TABLE IF EXISTS avaliacao CASCADE;
DROP TABLE IF EXISTS alternativa CASCADE;
DROP TABLE IF EXISTS questao CASCADE;
DROP TABLE IF EXISTS disciplina CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- 1. Usuários (Alunos, Docentes e Coordenadores)
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    matricula VARCHAR(30) NOT NULL UNIQUE,
    papel VARCHAR(20) NOT NULL CHECK (papel IN ('ALUNO', 'DOCENTE', 'COORDENADOR')),
    senha VARCHAR(100) NOT NULL
);

-- 2. Disciplinas vinculadas a um docente responsável
CREATE TABLE disciplina (
    id_disciplina SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    id_docente INT NOT NULL REFERENCES usuario(id_usuario)
);

-- 3. Banco de questões reutilizáveis (RF1)
CREATE TABLE questao (
    id_questao SERIAL PRIMARY KEY,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('MULTIPLA_ESCOLHA', 'TEXTO_LIVRE')),
    categoria VARCHAR(60) NOT NULL
);

-- 4. Alternativas das questões de múltipla escolha (RF1)
CREATE TABLE alternativa (
    id_alternativa SERIAL PRIMARY KEY,
    id_questao INT NOT NULL REFERENCES questao(id_questao) ON DELETE CASCADE,
    texto VARCHAR(255) NOT NULL,
    valor_escala INT CHECK (valor_escala BETWEEN 1 AND 5),
    correta BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Avaliações de disciplinas por ano/semestre (RF2 e RF6)
-- A restrição UNIQUE (id_disciplina, ano, semestre) garante no máximo uma
-- avaliação institucional por disciplina em cada semestre letivo.
CREATE TABLE avaliacao (
    id_avaliacao SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    id_disciplina INT NOT NULL REFERENCES disciplina(id_disciplina),
    ano INT NOT NULL CHECK (ano >= 2020),
    semestre INT NOT NULL CHECK (semestre IN (1, 2)),
    data_avaliacao DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    CHECK (hora_fim > hora_inicio),
    UNIQUE (id_disciplina, ano, semestre)
);

-- 6. Associação N:M entre Avaliações e Questões (RF3)
CREATE TABLE avaliacao_questao (
    id_avaliacao INT NOT NULL REFERENCES avaliacao(id_avaliacao) ON DELETE CASCADE,
    id_questao INT NOT NULL REFERENCES questao(id_questao),
    ordem INT NOT NULL CHECK (ordem > 0),
    PRIMARY KEY (id_avaliacao, id_questao),
    UNIQUE (id_avaliacao, ordem)
);

-- 7. Controle de destinatários e participação única (RF4 e RF6)
-- Registra QUEM deve responder e SE já respondeu, sem qualquer ligação com as respostas.
CREATE TABLE avaliacao_destinatario (
    id_avaliacao INT NOT NULL REFERENCES avaliacao(id_avaliacao) ON DELETE CASCADE,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
    respondido BOOLEAN NOT NULL DEFAULT FALSE,
    data_participacao DATE,
    PRIMARY KEY (id_avaliacao, id_usuario)
);

-- 8. Submissão anônima (RF5 e RF6)
-- NÃO possui id_usuario: impossibilita rastrear o autor das respostas.
CREATE TABLE submissao_anonima (
    id_submissao SERIAL PRIMARY KEY,
    id_avaliacao INT NOT NULL REFERENCES avaliacao(id_avaliacao),
    data_submissao DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 9. Respostas individuais vinculadas apenas à submissão anônima (RF5 e RF6)
CREATE TABLE resposta (
    id_resposta SERIAL PRIMARY KEY,
    id_submissao INT NOT NULL REFERENCES submissao_anonima(id_submissao) ON DELETE CASCADE,
    id_questao INT NOT NULL REFERENCES questao(id_questao),
    id_alternativa INT REFERENCES alternativa(id_alternativa),
    texto_resposta TEXT,
    UNIQUE (id_submissao, id_questao),
    CHECK (
        id_alternativa IS NOT NULL
        OR (texto_resposta IS NOT NULL AND LENGTH(TRIM(texto_resposta)) > 0)
    )
);
