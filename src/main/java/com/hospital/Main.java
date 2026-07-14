package com.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.config.DbConfig;
import com.hospital.dao.HospitalDao;
import com.hospital.dao.jdbc.HospitalJdbcDao;
import com.hospital.model.Procedimento;
import com.hospital.model.ProcedimentoRealizado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static HospitalDao currentDao;
    private static String currentModeLabel = "Nenhum (Selecione um modo)";

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  SISTEMA DE GESTÃO HOSPITALAR DRA. YUSKA MARITAN BRITO (CLI)   ");
        System.out.println("=================================================================");

        boolean exitSystem = false;
        while (!exitSystem) {
            System.out.println("\n--- MENU DE SELEÇÃO DE ETAPA ---");
            System.out.println("1. Etapa 1 (Fundamentos - JDBC / SQL Puro)");
            System.out.println("0. Sair do Sistema");
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runEtapa1();
                    break;
                case "0":
                    exitSystem = true;
                    DbConfig.shutdown();
                    System.out.println("Encerrando o sistema. Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private static void runEtapa1() {
        currentDao = new HospitalJdbcDao();
        currentModeLabel = "JDBC (Etapa 1 - SQL Puro)";

        boolean back = false;
        while (!back) {
            System.out.println("\n--- MENU ETAPA 1 (JDBC / SQL Puro) ---");
            System.out.println("1. Inicializar Banco de Dados (Schema + Seeds da Etapa 1)");
            System.out.println("2. Operações CRUD (Etapa 1)");
            System.out.println("3. Consultas Analíticas (Etapa 1)");
            System.out.println("4. Listagens Básicas de Entidades");
            System.out.println("0. Voltar ao Menu de Seleção de Etapa");
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        initDatabase();
                        break;
                    case "2":
                        handleCrudMenu();
                        break;
                    case "3":
                        handleAnalyticalMenu();
                        break;
                    case "4":
                        handleListingsMenu();
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n[ERRO] Ocorreu um problema na Etapa 1: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void initDatabase() throws Exception {
        System.out.println("\nInicializando banco de dados...");
        currentDao.initDatabase();
        System.out.println("Banco de dados pronto para uso.");
    }

    private static void handleListingsMenu() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- LISTAGENS BÁSICAS DE ENTIDADES --- (" + currentModeLabel + ")");
            System.out.println("1. Listar Pacientes (Especialização de Pessoa)");
            System.out.println("2. Listar Preceptores (Especialização de Profissional)");
            System.out.println("3. Listar Residentes (Especialização de Profissional)");
            System.out.println("4. Listar Unidades");
            System.out.println("5. Listar Procedimentos");
            System.out.println("6. Listar Escalas de Plantão");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    printResultsTable(currentDao.listPacientes());
                    break;
                case "2":
                    printResultsTable(currentDao.listPreceptores());
                    break;
                case "3":
                    printResultsTable(currentDao.listResidentes());
                    break;
                case "4":
                    printResultsTable(currentDao.listUnidades());
                    break;
                case "5":
                    printResultsTable(currentDao.listProcedimentos());
                    break;
                case "6":
                    printResultsTable(currentDao.listEscalas());
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    // ==========================================
    // CRUD MENU
    // ==========================================
    private static void handleCrudMenu() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- OPERAÇÕES CRUD --- (" + currentModeLabel + ")");
            System.out.println("1. Inserir Novo Atendimento (com múltiplos procedimentos)");
            System.out.println("2. Listar Atendimentos de um Paciente");
            System.out.println("3. Listar Procedimentos Realizados em um Atendimento");
            System.out.println("4. Atualizar Dados do Paciente (Endereço e Convênio)");
            System.out.println("5. Remover Procedimento Realizado (Apenas se não faturado)");
            System.out.println("6. Calcular Duração Média dos Atendimentos por Residente");
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    doInsertAttendance();
                    break;
                case "2":
                    doListAttendancesByPatient();
                    break;
                case "3":
                    doListProceduresByAttendance();
                    break;
                case "4":
                    doUpdatePatient();
                    break;
                case "5":
                    doDeleteProcedure();
                    break;
                case "6":
                    doAverageDurationPerResident();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private static void doInsertAttendance() throws Exception {
        System.out.println("\n--- Inserir Atendimento ---");
        System.out.print("Data e Horário (yyyy-MM-dd HH:mm): ");
        String dateStr = scanner.nextLine().trim();
        LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);

        System.out.print("Duração (minutos): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("ID do Paciente: ");
        int patId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("ID do Residente: ");
        int resId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("ID do Preceptor: ");
        int precId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("ID da Unidade: ");
        int unitId = Integer.parseInt(scanner.nextLine().trim());

        List<ProcedimentoRealizado> list = new ArrayList<>();
        boolean adding = true;
        while (adding) {
            System.out.println("\nAdicionar Procedimento Realizado:");
            System.out.print("ID do Procedimento: ");
            int procId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Quantidade: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Tempo Real Gasto (minutos): ");
            int realTime = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Observação: ");
            String obs = scanner.nextLine().trim();

            System.out.print("Data/Hora Início do Procedimento (yyyy-MM-dd HH:mm): ");
            String procStartStr = scanner.nextLine().trim();
            LocalDateTime procStart = LocalDateTime.parse(procStartStr, formatter);

            Procedimento proc = new Procedimento();
            proc.setIdProcedimento(procId);
            ProcedimentoRealizado pr = new ProcedimentoRealizado(null, proc, qty, realTime, obs, procStart);
            list.add(pr);

            System.out.print("Adicionar mais um procedimento? (S/N): ");
            String resp = scanner.nextLine().trim();
            if (resp.equalsIgnoreCase("N")) {
                adding = false;
            }
        }

        currentDao.insertAttendance(dt, duration, patId, resId, precId, unitId, list);
        System.out.println("Atendimento gravado com sucesso.");
    }

    private static void doListAttendancesByPatient() throws Exception {
        System.out.print("\nID do Paciente: ");
        int patId = Integer.parseInt(scanner.nextLine().trim());
        List<Map<String, Object>> list = currentDao.listAttendancesByPatient(patId);
        printResultsTable(list);
    }

    private static void doListProceduresByAttendance() throws Exception {
        System.out.print("\nID do Atendimento: ");
        int attId = Integer.parseInt(scanner.nextLine().trim());
        List<Map<String, Object>> list = currentDao.listProceduresByAttendance(attId);
        printResultsTable(list);
    }

    private static void doUpdatePatient() throws Exception {
        System.out.print("\nID do Paciente: ");
        int patId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Novo Número de Convênio: ");
        String conv = scanner.nextLine().trim();
        System.out.print("Novas Alergias (separe por vírgula ou em branco): ");
        String allergies = scanner.nextLine().trim();

        currentDao.updatePatient(patId, conv, allergies.isEmpty() ? null : allergies);
        System.out.println("Paciente atualizado com sucesso.");
    }

    private static void doDeleteProcedure() throws Exception {
        System.out.print("\nID do Atendimento: ");
        int attId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("ID do Procedimento: ");
        int procId = Integer.parseInt(scanner.nextLine().trim());

        boolean success = currentDao.deleteProcedureRealized(attId, procId);
        if (success) {
            System.out.println("Procedimento realizado deletado com sucesso.");
        } else {
            System.out
                    .println("[BLOQUEADO] Não foi possível remover. O procedimento já foi faturado (faturado = TRUE).");
        }
    }

    private static void doAverageDurationPerResident() throws Exception {
        System.out.print("\nID do Residente: ");
        int resId = Integer.parseInt(scanner.nextLine().trim());
        double avg = currentDao.calculateAverageDurationPerResident(resId);
        System.out.printf("Duração média dos atendimentos do residente %d: %.2f minutos\n", resId, avg);
    }

    // ==========================================
    // ANALYTICAL MENU
    // ==========================================
    private static void handleAnalyticalMenu() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CONSULTAS ANALÍTICAS --- (" + currentModeLabel + ")");
            System.out.println("1. Ranking dos Residentes por Número de Atendimentos");
            System.out.println("2. Preceptores com mais de 5 atendimentos em um Mês/Ano");
            System.out.println("3. Plantões Escalados por Residente no Mês Corrente (Calendário)");
            System.out.println("4. Pacientes que nunca realizaram nenhum procedimento de risco 'ALTO'");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    printResultsTable(currentDao.getResidentRanking());
                    break;
                case "2":
                    System.out.print("Ano (ex: 2026): ");
                    int year = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Mês (1 a 12): ");
                    int month = Integer.parseInt(scanner.nextLine().trim());
                    printResultsTable(currentDao.getPreceptorsWithSupervisionLimit(year, month));
                    break;
                case "3":
                    printResultsTable(currentDao.getShiftsCountPerResidentInCurrentMonth());
                    break;
                case "4":
                    printResultsTable(currentDao.getPatientsWithoutHighRiskProcedures());
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }



    // ==========================================
    // PRINT TABLE HELPER
    // ==========================================
    private static void printResultsTable(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("\n[Resultado] Nenhum registro encontrado.");
            return;
        }

        System.out.println();
        // Print column headers
        Map<String, Object> firstRow = list.get(0);
        for (String colName : firstRow.keySet()) {
            System.out.print(colName.toUpperCase() + "\t| ");
        }
        System.out.println("\n" + "-".repeat(80));

        // Print rows
        for (Map<String, Object> row : list) {
            for (Object value : row.values()) {
                System.out.print(value + "\t| ");
            }
            System.out.println();
        }
        System.out.println("-".repeat(80));
        System.out.println("Total de linhas: " + list.size() + "\n");
    }
}
