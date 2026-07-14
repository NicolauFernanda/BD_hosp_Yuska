# Roteiro de Apresentação (10 Minutos) - Etapa 1: Fundamentos e JDBC

Este arquivo serve como um roteiro de apresentação em slides e script de fala para a defesa da **Etapa 1** do projeto prático de banco de dados.

---

## Slide 1: Introdução e Contexto (Tempo sugerido: 1:00)
**Título**: Sistema de Gestão Hospitalar - Hospital Universitário Dra. Yuska Maritan Brito  
**Objetivo**: Desenvolver o modelo relacional e a base de persistência JDBC (SQL puro) para controle de atendimentos, escalas e procedimentos.

* **Foco da Apresentação**:
  - Modelagem conceitual e lógica do domínio de saúde.
  - Implementação física das tabelas no PostgreSQL 17.
  - Desenvolvimento da interface CLI e operações CRUD em SQL puro.

**Dicas de Fala**:
> *"Olá a todos. Nesta apresentação, vou demonstrar a Etapa 1 do nosso projeto de banco de dados para o Hospital Universitário Dra. Yuska Maritan Brito. O objetivo principal desta etapa foi definir o modelo relacional correto do hospital, criar a base física usando o PostgreSQL, e implementar operações JDBC diretas em Java, garantindo as constraints e a normalização dos dados."*

---

## Slide 2: Modelagem Conceitual, Lógica e Normalização (Tempo sugerido: 2:30)
**Título**: Arquitetura do Esquema e Normalização  
**Destaques**:
- **Herança/Especialização de Pessoas**:
  - `PESSOA` contendo atributos comuns (nome, CPF, data de nascimento, is_flamengo, telefone).
  - Especialização em `PACIENTE` (com atributos próprios de saúde) e `PROFISSIONAL`.
  - Sub-especialização de `PROFISSIONAL` em `PRECEPTOR` (titulação) e `RESIDENTE` (ano de residência).
- **Relações de Negócio**:
  - `ATENDIMENTO` que associa um único paciente, um residente e um preceptor supervisor.
  - `PROCEDIMENTO_REALIZADO` como tabela de junção contendo quantidade e tempo real.
  - `ESCALA` de plantão mapeando residente, preceptor, unidade, dia e turno.

**Evidência de Normalização (3FN)**:
- Todas as tabelas estão na Terceira Forma Normal. Cada atributo não chave depende única e diretamente da chave primária (ex: em `PACIENTE`, o número do convênio depende do ID; em `PRECEPTOR`, a titulação depende do profissional, não havendo dependências transitivas).

**Dicas de Fala**:
> *"Para a modelagem, adotamos a estratégia de herança relacional com tabelas separadas ligadas por chaves estrangeiras. A entidade Pessoa armazena os dados comuns, enquanto Paciente e Profissional guardam dados específicos. Os profissionais ainda se dividem em Residentes e Preceptores. Todas as tabelas foram revisadas e normalizadas até a 3FN para evitar qualquer anomalia de inserção ou atualização."*

---

## Slide 3: Implementação Física e Sementes de Teste (Tempo sugerido: 1:30)
**Título**: Criação do Banco e Carga Inicial  
**Scripts Criados**:
- [schema_etapa1.sql](./src/main/resources/database/schema_etapa1.sql):
  - Declaração de `CREATE TABLE` com constraints explícitas (`PRIMARY KEY`, `FOREIGN KEY`, `NOT NULL`, `CHECK`, `UNIQUE`).
  - Restrição única importante: `UNIQUE (id_unidade, dia_semana, turno, id_residente)` na tabela `ESCALA` para impedir choque de escalas na mesma unidade.
- [data_etapa1.sql](./src/main/resources/database/data_etapa1.sql):
  - Script DML para inserção de massa de testes cobrindo os requisitos mínimos (5 pacientes, 5 residentes, 5 preceptores, 3 unidades hospitalares, 10 atendimentos e 10 procedimentos).

**Dicas de Fala**:
> *"Traduzimos o modelo lógico para scripts SQL PostgreSQL. No script 'schema_etapa1', declaramos chaves primárias, estrangeiras e constraints de integridade, incluindo regras para garantir unicidade de plantões. No script 'data_etapa1', populamos o banco com o mínimo de dados exigido pelo projeto para que todas as funcionalidades possam ser testadas imediatamente."*

---

## Slide 4: Arquitetura do Software e Camada JDBC (Tempo sugerido: 2:00)
**Título**: Conectividade JDBC e Padrão DAO  
**Elementos Chave**:
- [DbConfig.java](./src/main/java/com/hospital/config/DbConfig.java): Gerencia a conexão nativa com a URL JDBC do PostgreSQL.
- [HospitalDao.java](./src/main/java/com/hospital/dao/HospitalDao.java): Interface abstrata de operações do hospital.
- [HospitalJdbcDao.java](./src/main/java/com/hospital/dao/jdbc/HospitalJdbcDao.java):
  - Execução de Queries em SQL nativo puro usando `PreparedStatement`.
  - Tratamento manual das transações quando necessário.

**Operações Principais**:
- Inserção transacional de atendimento com validações de IDs e vínculos de procedimentos.
- Consultas Analíticas (SQL puro com `JOIN`, `GROUP BY` e `HAVING`):
  - Ranking de residentes mais ativos.
  - Lista de preceptores mais demandados.
  - Relatório de plantões por residente por unidade.
  - Filtro por risco de procedimentos.

**Dicas de Fala**:
> *"No código Java, estruturamos a aplicação utilizando o padrão DAO. A classe HospitalJdbcDao executa consultas e instruções DML diretamente, sem uso de ORMs, mapeando os resultados ResultSet em objetos manualmente. Todas as validações exigidas pela Etapa 1 são executadas com validações SQL rápidas."*

---

## Slide 5: Demonstração ao Vivo da CLI (Tempo sugerido: 2:30)
**Título**: Demonstração de Funcionalidades  

**Passo a passo a ser executado**:
1. Iniciar o container Docker (PostgreSQL 17):
   ```bash
   docker compose up -d
   ```
2. Compilar e rodar a aplicação:
   ```bash
   mvn clean package
   java -jar target/hospital-management-cli-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
3. No menu principal, acessar a opção **1** (JDBC - Etapa 1).
4. Demonstrar a **Opção 1 (Inicializar Banco)** para recriar o schema e popular os dados da Etapa 1.
5. Executar um **CRUD** (Opção 2 - Inserir um Atendimento e listar atendimentos por paciente).
6. Executar as **Consultas Analíticas** (Opção 3) mostrando os dados retornados no console (ex: Ranking de atendimentos por residente).

**Dicas de Fala**:
> *"Vou demonstrar agora o sistema CLI funcionando. Ao iniciarmos na opção 1 (JDBC), temos as opções de inicialização. Vou resetar o banco de dados. Vejam no console que os scripts SQL são executados com sucesso. Agora, vou realizar a inserção de um atendimento e listar os dados analíticos do banco, comprovando o correto funcionamento dos relatórios e a execução de nossas queries SQL robustas."*

---

## Slide 6: Encerramento e Próximos Passos (Tempo sugerido: 0:30)
**Título**: Conclusão da Etapa 1  
**Resultados**:
- Esquema de banco de dados robusto, normalizado e íntegro.
- Camada Java robusta integrada via JDBC nativo.
- Interface simples e de fácil validação via CLI.

**Dicas de Fala**:
> *"Com isso, finalizamos a apresentação da Etapa 1, comprovando a integridade física e conceitual do banco de dados do Hospital. Estamos prontos para seguir para a Etapa 2, onde migraremos para ORM e adicionaremos automações avançadas no banco. Obrigado!"*
