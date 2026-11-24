package br.uel.avaliacoes.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Avaliacao {
    private Integer idAvaliacao;
    private String titulo;
    private String descricao;
    private Integer idDisciplina;
    private String nomeDisciplina;
    private String nomeDocente;
    private Integer ano;
    private Integer semestre;
    private LocalDate dataAvaliacao;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Boolean respondido;

    public Integer getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(Integer idAvaliacao) { this.idAvaliacao = idAvaliacao; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public String getNomeDocente() { return nomeDocente; }
    public void setNomeDocente(String nomeDocente) { this.nomeDocente = nomeDocente; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDate dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public Boolean getRespondido() { return respondido; }
    public void setRespondido(Boolean respondido) { this.respondido = respondido; }
}
