-- Seed data for Hospital Management System (Dra. Yuska Maritan Brito) - Etapa 1

-- Clean up data to avoid constraint violations during seed execution
TRUNCATE ESCALA, PROCEDIMENTO_REALIZADO, PROCEDIMENTO, ATENDIMENTO, UNIDADE, RESIDENTE, PRECEPTOR, PROFISSIONAL, PACIENTE, PESSOA RESTART IDENTITY;

-- 1. Insert PESSOAS (1 to 15)
-- Patients (1-5)
INSERT INTO PESSOA (id_pessoa, nome, cpf, data_nascimento, is_flamengo, telefone) VALUES 
(1, 'Fernanda Nicolau', '12345678901', '1998-05-15', TRUE, '81999991111'),
(2, 'Jose Teixeira', '23456789012', '1985-10-22', FALSE, '81999992222'),
(3, 'Maria Oliveira', '34567890123', '2000-01-05', TRUE, '81999993333'),
(4, 'Pedro Santos', '45678901234', '1960-07-12', FALSE, '81999994444'),
(5, 'Lucas Souza', '56789012345', '1992-12-30', TRUE, '81999995555');

-- Preceptors (6-10)
INSERT INTO PESSOA (id_pessoa, nome, cpf, data_nascimento, is_flamengo, telefone) VALUES 
(6, 'Dr. Arnaldo Azevedo', '67890123456', '1970-03-25', TRUE, '81988881111'),
(7, 'Dra. Beatrice Lima', '78901234567', '1975-08-14', FALSE, '81988882222'),
(8, 'Carlos Mendes', '89012345678', '1980-11-02', TRUE, '81988883333'),
(9, 'Dr. Daniel Rocha', '90123456789', '1968-09-19', FALSE, '81988884444'),
(10, 'Elisa Pereira', '01234567890', '1982-04-11', TRUE, '81988885555');

-- Residents (11-15)
INSERT INTO PESSOA (id_pessoa, nome, cpf, data_nascimento, is_flamengo, telefone) VALUES 
(11, 'Dr. Gabriel Costa', '11122233344', '1995-02-18', FALSE, '81977771111'),
(12, 'Dra. Helena Souza', '22233344455', '1997-06-25', TRUE, '81977772222'),
(13, 'Igor Fernandes', '33344455566', '1994-09-01', TRUE, '81977773333'),
(14, 'Juliana Castro', '44455566677', '1996-03-10', FALSE, '81977774444'),
(15, 'Kleber Machado', '55566677788', '1995-11-20', TRUE, '81977775555');

-- Synchronize PESSOA sequence
SELECT setval('pessoa_id_pessoa_seq', (SELECT MAX(id_pessoa) FROM PESSOA));


-- 2. Insert PACIENTES (1 to 5)
INSERT INTO PACIENTE (id_pessoa, num_convenio, alergias, grupo_sanguineo) VALUES 
(1, 'CONV-1001', 'Dipirona, Poeira', 'O+'),
(2, 'CONV-1002', NULL, 'A-'),
(3, 'CONV-1003', 'Penicilina', 'AB+'),
(4, NULL, 'Corante Amarelo', 'B+'),
(5, 'CONV-1005', NULL, 'O-');


-- 3. Insert PROFISSIONAIS (6 to 15)
-- Preceptors (6-10)
INSERT INTO PROFISSIONAL (id_pessoa, crm, data_admissao, especialidade) VALUES 
(6, 'CRM-6001', '2010-02-01', 'Cardiologia'),
(7, 'CRM-7002', '2012-05-15', 'Pediatria'),
(8, 'CRM-8003', '2015-08-01', 'Cirurgia Geral'),
(9, 'CRM-9004', '2008-11-10', 'Clinica Medica'),
(10, 'CRM-10005', '2018-03-20', 'Neurologia');

-- Residents (11-15)
INSERT INTO PROFISSIONAL (id_pessoa, crm, data_admissao, especialidade) VALUES 
(11, 'CRM-11001', '2024-03-01', 'Cardiologia'),
(12, 'CRM-12002', '2024-03-01', 'Pediatria'),
(13, 'CRM-13003', '2025-03-01', 'Cirurgia Geral'),
(14, 'CRM-14004', '2023-03-01', 'Clinica Medica'),
(15, 'CRM-15005', '2025-03-01', 'Neurologia');


-- 4. Insert PRECEPTORES (6 to 10)
-- 6: Doutor, 7: Doutora, 8: Mestre, 9: Doutor, 10: Especialista
INSERT INTO PRECEPTOR (id_profissional, titulacao) VALUES 
(6, 'Doutor em Cardiologia'),
(7, 'Doutora em Pediatria'),
(8, 'Mestre em Cirurgia'),
(9, 'Doutor em Clinica Medica'),
(10, 'Especialista em Neurologia');


-- 5. Insert RESIDENTES (11 to 15)
INSERT INTO RESIDENTE (id_profissional, ano_residencia) VALUES 
(11, 'R2'),
(12, 'R2'),
(13, 'R1'),
(14, 'R3'),
(15, 'R1');


-- 6. Insert UNIDADES (1 to 3)
INSERT INTO UNIDADE (id_unidade, nome, tipo, capacidade_leitos) VALUES 
(1, 'Enfermaria Central', 'Enfermaria', 50),
(2, 'UTI Adulto Coracao', 'UTI', 10),
(3, 'Pronto-Socorro Adulto', 'Pronto-Socorro', 20);

-- Synchronize UNIDADE sequence
SELECT setval('unidade_id_unidade_seq', (SELECT MAX(id_unidade) FROM UNIDADE));


-- 7. Insert PROCEDIMENTOS (1 to 5)
INSERT INTO PROCEDIMENTO (id_procedimento, codigo, nome, tempo_medio_minutos, nivel_risco) VALUES 
(1, 'PROC-001', 'Sutura Simples', 20, 'BAIXO'),
(2, 'PROC-002', 'Coleta de Sangue Venoso', 10, 'BAIXO'),
(3, 'PROC-003', 'Acesso Venoso Central', 45, 'MEDIO'),
(4, 'PROC-004', 'Drenagem de Torax', 60, 'ALTO'),
(5, 'PROC-005', 'Intubacao Orotraqueal', 30, 'ALTO');

-- Synchronize PROCEDIMENTO sequence
SELECT setval('procedimento_id_procedimento_seq', (SELECT MAX(id_procedimento) FROM PROCEDIMENTO));


-- 8. Insert ATENDIMENTOS (1 to 10)
-- Format: (id, data_hora, duracao, paciente, residente, preceptor, unidade)
INSERT INTO ATENDIMENTO (id_atendimento, data_hora, duracao_minutos, id_paciente, id_residente, id_preceptor, id_unidade) VALUES 
(1, '2026-07-01 08:30:00', 45, 1, 11, 6, 2),
(2, '2026-07-01 10:15:00', 30, 2, 12, 7, 1),
(3, '2026-07-02 14:00:00', 90, 3, 13, 8, 3),
(4, '2026-07-02 21:30:00', 25, 4, 14, 9, 3),
(5, '2026-07-03 09:00:00', 50, 5, 15, 10, 1),
(6, '2026-07-03 16:30:00', 40, 1, 11, 6, 2),
(7, '2026-07-04 11:00:00', 120, 2, 13, 8, 3),
(8, '2026-07-04 22:45:00', 35, 3, 14, 9, 3),
(9, '2026-07-05 08:00:00', 60, 4, 12, 7, 1),
(10, '2026-07-05 13:30:00', 70, 5, 15, 10, 1);

-- Synchronize ATENDIMENTO sequence
SELECT setval('atendimento_id_atendimento_seq', (SELECT MAX(id_atendimento) FROM ATENDIMENTO));


-- 9. Insert PROCEDIMENTOS REALIZADOS (10 rows)
-- Format: (atendimento, procedimento, quantidade, tempo_real, observacao, data_hora_inicio, faturado)
INSERT INTO PROCEDIMENTO_REALIZADO (id_atendimento, id_procedimento, quantidade, tempo_real_minutos, observacao, data_hora_inicio, faturado) VALUES 
(1, 3, 1, 50, 'Acesso central sem intercorrencias', '2026-07-01 08:40:00', FALSE),
(1, 2, 1, 15, 'Dificuldade para achar veia', '2026-07-01 08:32:00', TRUE),
(2, 1, 1, 25, 'Sutura no antebraco esquerdo', '2026-07-01 10:20:00', FALSE),
(3, 4, 1, 75, 'Paciente estavel apos drenagem', '2026-07-02 14:15:00', FALSE),
(4, 2, 2, 12, 'Coletas de rotina realizadas', '2026-07-02 21:35:00', FALSE),
(5, 1, 1, 20, 'Retirada de pontos em consulta', '2026-07-03 09:10:00', FALSE),
(6, 5, 1, 35, 'Intubacao de emergencia realizada', '2026-07-03 16:35:00', FALSE),
(7, 4, 1, 60, 'Drenagem bilateral', '2026-07-04 11:20:00', FALSE),
(8, 2, 1, 8, 'Coleta para gasometria', '2026-07-04 22:50:00', FALSE),
(9, 2, 1, 10, 'Coleta de sangue de rotina', '2026-07-05 08:05:00', FALSE),
(10, 3, 1, 40, 'Procedimento realizado no leito', '2026-07-05 13:45:00', FALSE);


-- 10. Insert ESCALAS DE PLANTÃO
-- Format: (unidade, dia, turno, residente, preceptor)
INSERT INTO ESCALA (id_unidade, dia_semana, turno, id_residente, id_preceptor) VALUES 
(2, 'Segunda', 'Manha', 11, 6),
(1, 'Segunda', 'Tarde', 12, 7),
(3, 'Terca', 'Tarde', 13, 8),
(3, 'Terca', 'Noite', 14, 9),
(1, 'Quarta', 'Manha', 15, 10),
(2, 'Quinta', 'Manha', 11, 6),
(3, 'Sexta', 'Tarde', 13, 8);

-- Synchronize ESCALA sequence
SELECT setval('escala_id_escala_seq', (SELECT MAX(id_escala) FROM ESCALA));
