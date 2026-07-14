package com.hospital.dao;

import com.hospital.model.ProcedimentoRealizado;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HospitalDao {

    // Setup and initialization
    void initDatabase() throws Exception;

    // --- CRUD OPERATIONS ---
    void insertAttendance(LocalDateTime dataHora, int duracao, int pacienteId, int residenteId, int preceptorId, int unidadeId, List<ProcedimentoRealizado> procedimentos) throws Exception;
    
    List<Map<String, Object>> listAttendancesByPatient(int pacienteId) throws Exception;
    
    List<Map<String, Object>> listProceduresByAttendance(int atendimentoId) throws Exception;
    
    void updatePatient(int patientId, String numConvenio, String alergias) throws Exception;
    
    boolean deleteProcedureRealized(int atendimentoId, int procedimentoId) throws Exception;
    
    double calculateAverageDurationPerResident(int residenteId) throws Exception;

    // --- ANALYTICAL QUERIES ---
    List<Map<String, Object>> getResidentRanking() throws Exception;
    
    List<Map<String, Object>> getPreceptorsWithSupervisionLimit(int year, int month) throws Exception;
    
    List<Map<String, Object>> getShiftsCountPerResidentInCurrentMonth() throws Exception;
    
    List<Map<String, Object>> getPatientsWithoutHighRiskProcedures() throws Exception;

    // --- BASIC LISTINGS ---
    List<Map<String, Object>> listPacientes() throws Exception;
    List<Map<String, Object>> listPreceptores() throws Exception;
    List<Map<String, Object>> listResidentes() throws Exception;
    List<Map<String, Object>> listUnidades() throws Exception;
    List<Map<String, Object>> listProcedimentos() throws Exception;
    List<Map<String, Object>> listEscalas() throws Exception;
}
