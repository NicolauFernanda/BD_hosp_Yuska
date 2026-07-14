# Sistema de Gestão Hospitalar - Etapa 1 (JDBC & SQL Puro)

Para ver a documentação e instruções da **Etapa 2 (Hibernate ORM & Recursos Avançados)**, acesse o arquivo [README_ETAPA2.md](./README_ETAPA2.md).

---

Este projeto consiste em um **Sistema de Gestão Hospitalar** completo desenvolvido em **Java 17**, utilizando **JDBC (SQL puro)** e **PostgreSQL 17** (via Docker ou outro servidor PostgreSQL de sua escolha), com uma interface amigável via **CLI (Linha de Comando)**.

O sistema gerencia atendimentos, profissionais (residentes e preceptores), pacientes, procedimentos realizados, internações e escalas de plantão, cobrindo todos os requisitos da **Etapa 1** do projeto prático de banco de dados.

---

## 🏗️ Arquitetura e Estrutura da Etapa 1

O projeto é estruturado de forma limpa seguindo padrões de desenvolvimento Java robustos:

- [pom.xml](./pom.xml): Gerenciador de dependências Maven (PostgreSQL Driver, Jackson Databind, JUnit 5).
- [docker-compose.yml](./docker-compose.yml): Infraestrutura do banco de dados PostgreSQL 17 configurado com a base `hospitaldb`.
- [/src/main/resources/database/](./src/main/resources/database/):
  - [schema_etapa1.sql](./src/main/resources/database/schema_etapa1.sql): Script DDL contendo a criação das tabelas e constraints de chaves primárias/estrangeiras da **Etapa 1**.
  - [data_etapa1.sql](./src/main/resources/database/data_etapa1.sql): Carga de dados inicial para testes da **Etapa 1** (mínimo de 5 pacientes, 5 residentes, 5 preceptores, 3 unidades, 10 atendimentos e 10 procedimentos).
- [/src/main/java/com/hospital/](./src/main/java/com/hospital/):
  - [Main.java](./src/main/java/com/hospital/Main.java): Ponto de entrada do sistema contendo a interface em linha de comando (CLI) interativa.
  - [config/DbConfig.java](./src/main/java/com/hospital/config/DbConfig.java): Configurações de conexões JDBC.
  - [dao/](./src/main/java/com/hospital/dao/): Camada de acesso a dados:
    - [HospitalDao.java](./src/main/java/com/hospital/dao/HospitalDao.java): Interface comum de acesso ao banco.
    - [jdbc/HospitalJdbcDao.java](./src/main/java/com/hospital/dao/jdbc/HospitalJdbcDao.java): Operações executadas via SQL puro e JDBC (Etapa 1).

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Banco de Dados PostgreSQL 17** (pode ser executado localmente, em nuvem ou via Docker - o **Docker é opcional**, sendo necessário *apenas* se você optar por rodar o container PostgreSQL configurado no projeto).
- **Java JDK 17+** instalado.
- **Maven** instalado e configurado nas variáveis de ambiente.
  *(O Maven é a ferramenta que gerencia as dependências do Java definidas no `pom.xml`, compila o código e empacota o projeto em um executável).*

  <details>
  <summary><b>🛠️ Como Instalar o Maven (Windows)</b></summary>

  **Opção A: Via Terminal (Mais Rápido e Simples)**
  1. Abra o Prompt de Comando ou PowerShell.
  2. Execute o comando do gerenciador de pacotes padrão do Windows:
     ```powershell
     winget install Apache.Maven
     ```
  3. Feche e abra o terminal novamente para que as alterações entrem em vigor.

  **Opção B: Instalação Manual**
  1. Acesse o site oficial do [Apache Maven Downloads](https://maven.apache.org/download.cgi) e baixe o arquivo **Binary zip archive** (extensão `.zip`).
  2. Extraia o conteúdo do arquivo ZIP para uma pasta permanente em seu computador (ex: `C:\Program Files\apache-maven`).
  3. Adicione a pasta `bin` contida no diretório extraído (ex: `C:\Program Files\apache-maven\bin`) às variáveis de ambiente:
     - Pesquise por **Variáveis de Ambiente** na barra de pesquisa do Windows e clique em "Editar as variáveis de ambiente do sistema".
     - Clique no botão **Variáveis de Ambiente...** no canto inferior direito.
     - Em **Variáveis do Sistema**, selecione a variável `Path` e clique em **Editar...**
     - Clique em **Novo** e adicione o caminho absoluto até a pasta `bin` do Maven (ex: `C:\Program Files\apache-maven\bin`).
     - Clique em **OK** para fechar e salvar todas as janelas abertas.

  **Verificação:**
  Para validar se a instalação foi bem-sucedida, abra um novo terminal e execute:
  ```bash
  mvn -version
  ```
  Isso deverá retornar a versão instalada do Maven e do Java instalados.
  </details>


### 1. Iniciar o Banco de Dados
> [!NOTE]
> O uso do Docker/Docker Compose é opcional. Ele só é necessário caso você precise de um servidor PostgreSQL local pré-configurado. Se você já possui um servidor PostgreSQL ativo (local ou em nuvem), pode utilizá-lo diretamente, bastando atualizar as credenciais de conexão em [DbConfig.java](./src/main/java/com/hospital/config/DbConfig.java) (para JDBC).

Caso opte por utilizar o Docker para subir o banco de dados:
No diretório raiz do projeto, garanta que a rede externa `tools` esteja criada e inicie o container:
```bash
docker network create tools
docker compose up -d
```
O PostgreSQL será iniciado na porta `5432` com o banco `hospitaldb` e usuário/senha padrão (`postgres`/`a`).

### 2. Compilar e Gerar o Executável
Compile e empacote a aplicação gerando o JAR com todas as dependências embutidas:
```bash
mvn clean package
```

### 3. Executar o Sistema CLI
Rode o JAR gerado na pasta target:
```bash
java -jar target/hospital-management-cli-1.0-SNAPSHOT-jar-with-dependencies.jar
```

No menu inicial da aplicação, selecione **1** para entrar no modo **JDBC (Etapa 1)**.

---

## ⚙️ Testando Cenários Específicos do Menu CLI

1. **Inicialização do Banco (Opção 1)**: Executa os scripts da Etapa 1 (`schema_etapa1.sql` e `data_etapa1.sql`) para restaurar a estrutura básica e dados iniciais.
2. **Operações CRUD (Opção 2)**: Permite executar inclusão de novos atendimentos, listagem por paciente, atualização de paciente e exclusão lógica de procedimentos (com flag).
3. **Consultas Analíticas (Opção 3)**: Executa consultas agregadas complexas em SQL puro (ranking de residentes, preceptores ocupados, plantões e consultas de risco).
