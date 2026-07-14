package com.hospital.model;

import java.math.BigDecimal;

public class Procedimento {

    private Integer idProcedimento;
    private String codigo;
    private String nome;
    private int tempoMedioMinutos;
    private String nivelRisco; // BAIXO, MEDIO, ALTO
    private BigDecimal mediaTempoProcedimento;

    public Procedimento() {}

    public Procedimento(String codigo, String nome, int tempoMedioMinutos, String nivelRisco) {
        this.codigo = codigo;
        this.nome = nome;
        this.tempoMedioMinutos = tempoMedioMinutos;
        this.nivelRisco = nivelRisco;
    }

    public Integer getIdProcedimento() { return idProcedimento; }
    public void setIdProcedimento(Integer idProcedimento) { this.idProcedimento = idProcedimento; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getTempoMedioMinutos() { return tempoMedioMinutos; }
    public void setTempoMedioMinutos(int tempoMedioMinutos) { this.tempoMedioMinutos = tempoMedioMinutos; }

    public String getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(String nivelRisco) { this.nivelRisco = nivelRisco; }

    public BigDecimal getMediaTempoProcedimento() { return mediaTempoProcedimento; }
    public void setMediaTempoProcedimento(BigDecimal mediaTempoProcedimento) { this.mediaTempoProcedimento = mediaTempoProcedimento; }
}
