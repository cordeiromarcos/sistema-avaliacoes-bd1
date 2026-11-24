package br.uel.avaliacoes.model;

public class RelatorioPeriodo {
    private String dataAvaliacao;
    private String periodo;
    private String codigo;
    private String disciplina;
    private String docente;
    private int totalRespostas;
    private double notaMedia;

    public String getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(String dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }
    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }
    public int getTotalRespostas() { return totalRespostas; }
    public void setTotalRespostas(int totalRespostas) { this.totalRespostas = totalRespostas; }
    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }
}
