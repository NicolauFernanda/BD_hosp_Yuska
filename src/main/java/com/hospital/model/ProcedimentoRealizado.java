package com.hospital.model;

import java.time.LocalDateTime;

public class ProcedimentoRealizado {

    private Integer idAtendimento;
    private Procedimento procedimento;
    private int quantidade;
    private int tempoRealMinutos;
    private String observacao;
    private LocalDateTime dataHoraInicio;
    private boolean faturado = false;

    public ProcedimentoRealizado() {}

    public ProcedimentoRealizado(Integer idAtendimento, Procedimento procedimento, int quantidade, int tempoRealMinutos, String observacao, LocalDateTime dataHoraInicio) {
        this.idAtendimento = idAtendimento;
        this.procedimento = procedimento;
        this.quantidade = quantidade;
        this.tempoRealMinutos = tempoRealMinutos;
        this.observacao = observacao;
        this.dataHoraInicio = dataHoraInicio;
    }

    public Integer getIdAtendimento() { return idAtendimento; }
    public void setIdAtendimento(Integer idAtendimento) { this.idAtendimento = idAtendimento; }

    public Procedimento getProcedimento() { return procedimento; }
    public void setProcedimento(Procedimento procedimento) { this.procedimento = procedimento; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public int getTempoRealMinutos() { return tempoRealMinutos; }
    public void setTempoRealMinutos(int tempoRealMinutos) { this.tempoRealMinutos = tempoRealMinutos; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public boolean isFaturado() { return faturado; }
    public void setFaturado(boolean faturado) { this.faturado = faturado; }
}
