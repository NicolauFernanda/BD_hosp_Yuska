-- Schema definition for Hospital Management System (Dra. Yuska Maritan Brito) - Etapa 1

-- Clean up existing structures
DROP TABLE IF EXISTS ESCALA CASCADE;
DROP TABLE IF EXISTS PROCEDIMENTO_REALIZADO CASCADE;
DROP TABLE IF EXISTS PROCEDIMENTO CASCADE;
DROP TABLE IF EXISTS ATENDIMENTO CASCADE;
DROP TABLE IF EXISTS UNIDADE CASCADE;
DROP TABLE IF EXISTS RESIDENTE CASCADE;
DROP TABLE IF EXISTS PRECEPTOR CASCADE;
DROP TABLE IF EXISTS PROFISSIONAL CASCADE;
DROP TABLE IF EXISTS PACIENTE CASCADE;
DROP TABLE IF EXISTS PESSOA CASCADE;

-- 1. Table PESSOA (Base)
CREATE TABLE PESSOA (
    id_pessoa SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    data_nascimento DATE NOT NULL,
    is_flamengo BOOLEAN NOT NULL DEFAULT FALSE,
    telefone VARCHAR(20),
    endereco VARCHAR(200)
);

-- 2. Table PACIENTE (extends PESSOA)
CREATE TABLE PACIENTE (
    id_pessoa INT PRIMARY KEY,
    num_convenio VARCHAR(50),
    alergias TEXT,
    grupo_sanguineo VARCHAR(5),
    CONSTRAINT fk_paciente_pessoa FOREIGN KEY (id_pessoa) REFERENCES PESSOA(id_pessoa) ON DELETE CASCADE
);

-- 3. Table PROFISSIONAL (extends PESSOA)
CREATE TABLE PROFISSIONAL (
    id_pessoa INT PRIMARY KEY,
    crm VARCHAR(20) UNIQUE NOT NULL,
    data_admissao DATE NOT NULL,
    especialidade VARCHAR(100),
    CONSTRAINT fk_profissional_pessoa FOREIGN KEY (id_pessoa) REFERENCES PESSOA(id_pessoa) ON DELETE CASCADE
);

-- 4. Table PRECEPTOR (extends PROFISSIONAL)
CREATE TABLE PRECEPTOR (
    id_profissional INT PRIMARY KEY,
    titulacao VARCHAR(50),
    CONSTRAINT fk_preceptor_profissional FOREIGN KEY (id_profissional) REFERENCES PROFISSIONAL(id_pessoa) ON DELETE CASCADE
);

-- 5. Table RESIDENTE (extends PROFISSIONAL)
CREATE TABLE RESIDENTE (
    id_profissional INT PRIMARY KEY,
    ano_residencia VARCHAR(2) CHECK (ano_residencia IN ('R1', 'R2', 'R3')),
    CONSTRAINT fk_residente_profissional FOREIGN KEY (id_profissional) REFERENCES PROFISSIONAL(id_pessoa) ON DELETE CASCADE
);

-- 6. Table UNIDADE
CREATE TABLE UNIDADE (
    id_unidade SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) CHECK (tipo IN ('Enfermaria', 'UTI', 'Pronto-Socorro', 'Ambulatorio')),
    capacidade_leitos INT NOT NULL CHECK (capacidade_leitos >= 0)
);

-- 7. Table ATENDIMENTO
CREATE TABLE ATENDIMENTO (
    id_atendimento SERIAL PRIMARY KEY,
    data_hora TIMESTAMP NOT NULL,
    duracao_minutos INT NOT NULL CHECK (duracao_minutos >= 0),
    id_paciente INT NOT NULL,
    id_residente INT NOT NULL,
    id_preceptor INT NOT NULL,
    id_unidade INT NOT NULL,
    CONSTRAINT fk_atendimento_paciente FOREIGN KEY (id_paciente) REFERENCES PACIENTE(id_pessoa),
    CONSTRAINT fk_atendimento_residente FOREIGN KEY (id_residente) REFERENCES RESIDENTE(id_profissional),
    CONSTRAINT fk_atendimento_preceptor FOREIGN KEY (id_preceptor) REFERENCES PRECEPTOR(id_profissional),
    CONSTRAINT fk_atendimento_unidade FOREIGN KEY (id_unidade) REFERENCES UNIDADE(id_unidade)
);

-- 8. Table PROCEDIMENTO
CREATE TABLE PROCEDIMENTO (
    id_procedimento SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    tempo_medio_minutos INT NOT NULL CHECK (tempo_medio_minutos >= 0),
    nivel_risco VARCHAR(10) CHECK (nivel_risco IN ('BAIXO', 'MEDIO', 'ALTO'))
);

-- 9. Table PROCEDIMENTO_REALIZADO (Composite PK)
CREATE TABLE PROCEDIMENTO_REALIZADO (
    id_atendimento INT,
    id_procedimento INT,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    tempo_real_minutos INT NOT NULL CHECK (tempo_real_minutos >= 0),
    observacao TEXT,
    data_hora_inicio TIMESTAMP NOT NULL,
    faturado BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_atendimento, id_procedimento),
    CONSTRAINT fk_realizado_atendimento FOREIGN KEY (id_atendimento) REFERENCES ATENDIMENTO(id_atendimento) ON DELETE CASCADE,
    CONSTRAINT fk_realizado_procedimento FOREIGN KEY (id_procedimento) REFERENCES PROCEDIMENTO(id_procedimento)
);

-- 10. Table ESCALA
CREATE TABLE ESCALA (
    id_escala SERIAL PRIMARY KEY,
    id_unidade INT NOT NULL,
    dia_semana VARCHAR(15) CHECK (dia_semana IN ('Segunda', 'Terca', 'Quarta', 'Quinta', 'Sexta', 'Sabado', 'Domingo')),
    turno VARCHAR(10) CHECK (turno IN ('Manha', 'Tarde', 'Noite')),
    id_residente INT NOT NULL,
    id_preceptor INT NOT NULL,
    CONSTRAINT fk_escala_unidade FOREIGN KEY (id_unidade) REFERENCES UNIDADE(id_unidade),
    CONSTRAINT fk_escala_residente FOREIGN KEY (id_residente) REFERENCES RESIDENTE(id_profissional),
    CONSTRAINT fk_escala_preceptor FOREIGN KEY (id_preceptor) REFERENCES PRECEPTOR(id_profissional),
    CONSTRAINT uq_escala_unidade_dia_turno_residente UNIQUE (id_unidade, dia_semana, turno, id_residente)
);
