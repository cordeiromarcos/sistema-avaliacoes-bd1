package br.uel.avaliacoes.model;

public class RelatorioAdesao {
    private int idAvaliacao;
    private String titulo;
    private String disciplina;
    private String periodo;
    private int totalConvocados;
    private int totalRespondidos;
    private int totalPendentes;
    private double percentualAdesao;

    public int getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(int idAvaliacao) { this.idAvaliacao = idAvaliacao; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public int getTotalConvocados() { return totalConvocados; }
    public void setTotalConvocados(int totalConvocados) { this.totalConvocados = totalConvocados; }
    public int getTotalRespondidos() { return totalRespondidos; }
    public void setTotalRespondidos(int totalRespondidos) { this.totalRespondidos = totalRespondidos; }
    public int getTotalPendentes() { return totalPendentes; }
    public void setTotalPendentes(int totalPendentes) { this.totalPendentes = totalPendentes; }
    public double getPercentualAdesao() { return percentualAdesao; }
    public void setPercentualAdesao(double percentualAdesao) { this.percentualAdesao = percentualAdesao; }
}
