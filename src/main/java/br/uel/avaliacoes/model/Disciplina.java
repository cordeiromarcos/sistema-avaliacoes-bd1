package br.uel.avaliacoes.model;

public class Disciplina {
    private Integer idDisciplina;
    private String codigo;
    private String nome;
    private Integer idDocente;
    private String nomeDocente;

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getIdDocente() { return idDocente; }
    public void setIdDocente(Integer idDocente) { this.idDocente = idDocente; }
    public String getNomeDocente() { return nomeDocente; }
    public void setNomeDocente(String nomeDocente) { this.nomeDocente = nomeDocente; }
}
