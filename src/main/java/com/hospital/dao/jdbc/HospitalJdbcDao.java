package com.hospital.dao.jdbc;

import com.hospital.config.DbConfig;
import com.hospital.dao.HospitalDao;
import com.hospital.model.ProcedimentoRealizado;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HospitalJdbcDao implements HospitalDao {

    // Helper to read resources
    private String readResourceFile(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public void initDatabase() throws Exception {
        String schemaSql = readResourceFile("database/schema_etapa1.sql");
        String dataSql = readResourceFile("database/data_etapa1.sql");

        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Executing schema_etapa1.sql...");
            stmt.execute(schemaSql);
            
            System.out.println("Executing data_etapa1.sql...");
            stmt.execute(dataSql);
            
            System.out.println("Database initialized successfully for Etapa 1.");
        }
    }

    @Override
    public void insertAttendance(LocalDateTime dataHora, int duracao, int pacienteId, int residenteId, int preceptorId, int unidadeId, List<ProcedimentoRealizado> procedimentos) throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            // 1. Validation checks
            try (PreparedStatement psPat = conn.prepareStatement("SELECT 1 FROM PACIENTE WHERE id_pessoa = ?");
                 PreparedStatement psRes = conn.prepareStatement("SELECT 1 FROM RESIDENTE WHERE id_profissional = ?");
                 PreparedStatement psPrec = conn.prepareStatement("SELECT 1 FROM PRECEPTOR WHERE id_profissional = ?");
                 PreparedStatement psUnit = conn.prepareStatement("SELECT 1 FROM UNIDADE WHERE id_unidade = ?")) {
                
                psPat.setInt(1, pacienteId);
                try (ResultSet rs = psPat.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Paciente com ID " + pacienteId + " não existe.");
                }
                
                psRes.setInt(1, residenteId);
                try (ResultSet rs = psRes.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Residente com ID " + residenteId + " não existe.");
                }
                
                psPrec.setInt(1, preceptorId);
                try (ResultSet rs = psPrec.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Preceptor com ID " + preceptorId + " não existe.");
                }

                psUnit.setInt(1, unidadeId);
                try (ResultSet rs = psUnit.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Unidade com ID " + unidadeId + " não existe.");
                }
            }

            // 2. Insert attendance and procedures in a transaction
            conn.setAutoCommit(false);
            try {
                int attendanceId;
                String insertAtendimento = "INSERT INTO ATENDIMENTO (data_hora, duracao_minutos, id_paciente, id_residente, id_preceptor, id_unidade) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAtendimento, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(dataHora));
                    ps.setInt(2, duracao);
                    ps.setInt(3, pacienteId);
                    ps.setInt(4, residenteId);
                    ps.setInt(5, preceptorId);
                    ps.setInt(6, unidadeId);
                    ps.executeUpdate();
                    
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            attendanceId = rs.getInt(1);
                        } else {
                            throw new SQLException("Falha ao obter ID do Atendimento inserido.");
                        }
                    }
                }

                String insertProc = "INSERT INTO PROCEDIMENTO_REALIZADO (id_atendimento, id_procedimento, quantidade, tempo_real_minutos, observacao, data_hora_inicio) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertProc)) {
                    for (ProcedimentoRealizado proc : procedimentos) {
                        ps.setInt(1, attendanceId);
                        ps.setInt(2, proc.getProcedimento().getIdProcedimento());
                        ps.setInt(3, proc.getQuantidade());
                        ps.setInt(4, proc.getTempoRealMinutos());
                        ps.setString(5, proc.getObservacao());
                        ps.setTimestamp(6, Timestamp.valueOf(proc.getDataHoraInicio()));
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Map<String, Object>> listAttendancesByPatient(int pacienteId) throws Exception {
        String sql = "SELECT a.id_atendimento, a.data_hora, a.duracao_minutos, p.nome AS nome_paciente, " +
                     "r_pes.nome AS nome_residente, prec_pes.nome AS nome_preceptor, u.nome AS nome_unidade " +
                     "FROM ATENDIMENTO a " +
                     "JOIN PESSOA p ON a.id_paciente = p.id_pessoa " +
                     "JOIN PESSOA r_pes ON a.id_residente = r_pes.id_pessoa " +
                     "JOIN PESSOA prec_pes ON a.id_preceptor = prec_pes.id_pessoa " +
                     "JOIN UNIDADE u ON a.id_unidade = u.id_unidade " +
                     "WHERE a.id_paciente = ? " +
                     "ORDER BY a.data_hora DESC";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id_atendimento", rs.getInt("id_atendimento"));
                    map.put("data_hora", rs.getTimestamp("data_hora").toLocalDateTime());
                    map.put("duracao_minutos", rs.getInt("duracao_minutos"));
                    map.put("nome_paciente", rs.getString("nome_paciente"));
                    map.put("nome_residente", rs.getString("nome_residente"));
                    map.put("nome_preceptor", rs.getString("nome_preceptor"));
                    map.put("nome_unidade", rs.getString("nome_unidade"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listProceduresByAttendance(int atendimentoId) throws Exception {
        String sql = "SELECT pr.id_procedimento, p.codigo, p.nome, pr.quantidade, pr.tempo_real_minutos, pr.observacao, pr.data_hora_inicio, pr.faturado " +
                     "FROM PROCEDIMENTO_REALIZADO pr " +
                     "JOIN PROCEDIMENTO p ON pr.id_procedimento = p.id_procedimento " +
                     "WHERE pr.id_atendimento = ?";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, atendimentoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id_procedimento", rs.getInt("id_procedimento"));
                    map.put("codigo", rs.getString("codigo"));
                    map.put("nome", rs.getString("nome"));
                    map.put("quantidade", rs.getInt("quantidade"));
                    map.put("tempo_real_minutos", rs.getInt("tempo_real_minutos"));
                    map.put("observacao", rs.getString("observacao"));
                    map.put("data_hora_inicio", rs.getTimestamp("data_hora_inicio").toLocalDateTime());
                    map.put("faturado", rs.getBoolean("faturado"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    @Override
    public void updatePatient(int patientId, String numConvenio, String alergias) throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update Paciente attributes
                try (PreparedStatement ps = conn.prepareStatement("UPDATE PACIENTE SET num_convenio = ?, alergias = ? WHERE id_pessoa = ?")) {
                    ps.setString(1, numConvenio);
                    ps.setString(2, alergias);
                    ps.setInt(3, patientId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean deleteProcedureRealized(int atendimentoId, int procedimentoId) throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if already billed (faturado flag)
                try (PreparedStatement psCheck = conn.prepareStatement("SELECT faturado FROM PROCEDIMENTO_REALIZADO WHERE id_atendimento = ? AND id_procedimento = ?")) {
                    psCheck.setInt(1, atendimentoId);
                    psCheck.setInt(2, procedimentoId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            boolean faturado = rs.getBoolean("faturado");
                            if (faturado) {
                                conn.rollback();
                                return false; // cannot delete since it is already billed
                            }
                        } else {
                            conn.rollback();
                            throw new IllegalArgumentException("Procedimento realizado não encontrado para este atendimento.");
                        }
                    }
                }

                // Proceed with deletion
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM PROCEDIMENTO_REALIZADO WHERE id_atendimento = ? AND id_procedimento = ?")) {
                    psDel.setInt(1, atendimentoId);
                    psDel.setInt(2, procedimentoId);
                    psDel.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public double calculateAverageDurationPerResident(int residenteId) throws Exception {
        String sql = "SELECT AVG(duracao_minutos) FROM ATENDIMENTO WHERE id_residente = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, residenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    @Override
    public List<Map<String, Object>> getResidentRanking() throws Exception {
        String sql = "SELECT p.nome, COUNT(a.id_atendimento) AS total " +
                     "FROM ATENDIMENTO a " +
                     "JOIN PESSOA p ON a.id_residente = p.id_pessoa " +
                     "GROUP BY p.nome " +
                     "ORDER BY total DESC";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("nome_residente", rs.getString("nome"));
                map.put("total_atendimentos", rs.getInt("total"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getPreceptorsWithSupervisionLimit(int year, int month) throws Exception {
        String sql = "SELECT p.nome, COUNT(a.id_atendimento) AS total " +
                     "FROM ATENDIMENTO a " +
                     "JOIN PESSOA p ON a.id_preceptor = p.id_pessoa " +
                     "WHERE EXTRACT(YEAR FROM a.data_hora) = ? AND EXTRACT(MONTH FROM a.data_hora) = ? " +
                     "GROUP BY p.nome " +
                     "HAVING COUNT(a.id_atendimento) > 5";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nome_preceptor", rs.getString("nome"));
                    map.put("total_supervisoes", rs.getInt("total"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getShiftsCountPerResidentInCurrentMonth() throws Exception {
        String sql = "WITH CurrentMonthDates AS ( " +
                     "    SELECT generate_series( " +
                     "        DATE_TRUNC('month', CURRENT_DATE), " +
                     "        (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' - INTERVAL '1 day')::date, " +
                     "        '1 day'::interval " +
                     "    )::date AS data_dia " +
                     "), " +
                     "DayOfWeekNames AS ( " +
                     "    SELECT data_dia, " +
                     "    CASE EXTRACT(ISODOW FROM data_dia) " +
                     "        WHEN 1 THEN 'Segunda' " +
                     "        WHEN 2 THEN 'Terca' " +
                     "        WHEN 3 THEN 'Quarta' " +
                     "        WHEN 4 THEN 'Quinta' " +
                     "        WHEN 5 THEN 'Sexta' " +
                     "        WHEN 6 THEN 'Sabado' " +
                     "        WHEN 7 THEN 'Domingo' " +
                     "    END AS dia_semana " +
                     "    FROM CurrentMonthDates " +
                     ") " +
                     "SELECT u.nome AS nome_unidade, r_pes.nome AS nome_residente, COUNT(d.data_dia) AS total_plantoes " +
                     "FROM ESCALA e " +
                     "JOIN UNIDADE u ON e.id_unidade = u.id_unidade " +
                     "JOIN PESSOA r_pes ON e.id_residente = r_pes.id_pessoa " +
                     "JOIN DayOfWeekNames d ON e.dia_semana = d.dia_semana " +
                     "GROUP BY u.nome, r_pes.nome " +
                     "ORDER BY u.nome, total_plantoes DESC";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("nome_unidade", rs.getString("nome_unidade"));
                map.put("nome_residente", rs.getString("nome_residente"));
                map.put("total_plantoes", rs.getInt("total_plantoes"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getPatientsWithoutHighRiskProcedures() throws Exception {
        String sql = "SELECT p.id_pessoa, p.nome " +
                     "FROM PACIENTE pac " +
                     "JOIN PESSOA p ON pac.id_pessoa = p.id_pessoa " +
                     "WHERE pac.id_pessoa NOT IN ( " +
                     "    SELECT DISTINCT a.id_paciente " +
                     "    FROM ATENDIMENTO a " +
                     "    JOIN PROCEDIMENTO_REALIZADO pr ON a.id_atendimento = pr.id_atendimento " +
                     "    JOIN PROCEDIMENTO proc ON pr.id_procedimento = proc.id_procedimento " +
                     "    WHERE proc.nivel_risco = 'ALTO' " +
                     ")";
        
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id_paciente", rs.getInt("id_pessoa"));
                map.put("nome_paciente", rs.getString("nome"));
                list.add(map);
            }
        }
        return list;
    }

    // --- End of Stage 1 operations ---

    @Override
    public List<Map<String, Object>> listPacientes() throws Exception {
        String sql = "SELECT p.id_pessoa, p.nome, p.cpf, p.data_nascimento, p.is_flamengo, p.telefone, p.endereco, pac.num_convenio, pac.alergias, pac.grupo_sanguineo " +
                     "FROM PACIENTE pac " +
                     "JOIN PESSOA p ON pac.id_pessoa = p.id_pessoa " +
                     "ORDER BY p.nome";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_pessoa", rs.getInt("id_pessoa"));
                map.put("nome", rs.getString("nome"));
                map.put("cpf", rs.getString("cpf"));
                map.put("data_nascimento", rs.getDate("data_nascimento").toLocalDate());
                map.put("is_flamengo", rs.getBoolean("is_flamengo"));
                map.put("telefone", rs.getString("telefone"));
                map.put("endereco", rs.getString("endereco"));
                map.put("num_convenio", rs.getString("num_convenio"));
                map.put("alergias", rs.getString("alergias"));
                map.put("grupo_sanguineo", rs.getString("grupo_sanguineo"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listPreceptores() throws Exception {
        String sql = "SELECT p.id_pessoa, p.nome, p.cpf, p.data_nascimento, p.is_flamengo, p.telefone, p.endereco, prof.crm, prof.data_admissao, prof.especialidade, prec.titulacao " +
                     "FROM PRECEPTOR prec " +
                     "JOIN PROFISSIONAL prof ON prec.id_profissional = prof.id_pessoa " +
                     "JOIN PESSOA p ON prof.id_pessoa = p.id_pessoa " +
                     "ORDER BY p.nome";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_pessoa", rs.getInt("id_pessoa"));
                map.put("nome", rs.getString("nome"));
                map.put("cpf", rs.getString("cpf"));
                map.put("data_nascimento", rs.getDate("data_nascimento").toLocalDate());
                map.put("is_flamengo", rs.getBoolean("is_flamengo"));
                map.put("telefone", rs.getString("telefone"));
                map.put("endereco", rs.getString("endereco"));
                map.put("crm", rs.getString("crm"));
                map.put("data_admissao", rs.getDate("data_admissao").toLocalDate());
                map.put("especialidade", rs.getString("especialidade"));
                map.put("titulacao", rs.getString("titulacao"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listResidentes() throws Exception {
        String sql = "SELECT p.id_pessoa, p.nome, p.cpf, p.data_nascimento, p.is_flamengo, p.telefone, p.endereco, prof.crm, prof.data_admissao, prof.especialidade, res.ano_residencia " +
                     "FROM RESIDENTE res " +
                     "JOIN PROFISSIONAL prof ON res.id_profissional = prof.id_pessoa " +
                     "JOIN PESSOA p ON prof.id_pessoa = p.id_pessoa " +
                     "ORDER BY p.nome";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_pessoa", rs.getInt("id_pessoa"));
                map.put("nome", rs.getString("nome"));
                map.put("cpf", rs.getString("cpf"));
                map.put("data_nascimento", rs.getDate("data_nascimento").toLocalDate());
                map.put("is_flamengo", rs.getBoolean("is_flamengo"));
                map.put("telefone", rs.getString("telefone"));
                map.put("endereco", rs.getString("endereco"));
                map.put("crm", rs.getString("crm"));
                map.put("data_admissao", rs.getDate("data_admissao").toLocalDate());
                map.put("especialidade", rs.getString("especialidade"));
                map.put("ano_residencia", rs.getString("ano_residencia"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listUnidades() throws Exception {
        String sql = "SELECT id_unidade, nome, tipo, capacidade_leitos FROM UNIDADE ORDER BY nome";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_unidade", rs.getInt("id_unidade"));
                map.put("nome", rs.getString("nome"));
                map.put("tipo", rs.getString("tipo"));
                map.put("capacidade_leitos", rs.getInt("capacidade_leitos"));
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listProcedimentos() throws Exception {
        boolean hasMediaColumn = false;
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM PROCEDIMENTO LIMIT 1")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                if (rsmd.getColumnName(i).equalsIgnoreCase("media_tempo_procedimento")) {
                    hasMediaColumn = true;
                    break;
                }
            }
        } catch (Exception e) {}

        String sql = hasMediaColumn 
            ? "SELECT id_procedimento, codigo, nome, tempo_medio_minutos, nivel_risco, media_tempo_procedimento FROM PROCEDIMENTO ORDER BY nome"
            : "SELECT id_procedimento, codigo, nome, tempo_medio_minutos, nivel_risco FROM PROCEDIMENTO ORDER BY nome";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_procedimento", rs.getInt("id_procedimento"));
                map.put("codigo", rs.getString("codigo"));
                map.put("nome", rs.getString("nome"));
                map.put("tempo_medio_minutos", rs.getInt("tempo_medio_minutos"));
                map.put("nivel_risco", rs.getString("nivel_risco"));
                if (hasMediaColumn) {
                    map.put("media_tempo_procedimento", rs.getBigDecimal("media_tempo_procedimento"));
                }
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listEscalas() throws Exception {
        String sql = "SELECT e.id_escala, u.nome AS nome_unidade, e.dia_semana, e.turno, r_pes.nome AS nome_residente, prec_pes.nome AS nome_preceptor " +
                     "FROM ESCALA e " +
                     "JOIN UNIDADE u ON e.id_unidade = u.id_unidade " +
                     "JOIN PESSOA r_pes ON e.id_residente = r_pes.id_pessoa " +
                     "JOIN PESSOA prec_pes ON e.id_preceptor = prec_pes.id_pessoa " +
                     "ORDER BY u.nome, e.dia_semana, e.turno";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id_escala", rs.getInt("id_escala"));
                map.put("nome_unidade", rs.getString("nome_unidade"));
                map.put("dia_semana", rs.getString("dia_semana"));
                map.put("turno", rs.getString("turno"));
                map.put("nome_residente", rs.getString("nome_residente"));
                map.put("nome_preceptor", rs.getString("nome_preceptor"));
                list.add(map);
            }
        }
        return list;
    }
}
