package br.uel.avaliacoes.model;

public class RelatorioQuestao {
    private int idQuestao;
    private String enunciado;
    private String categoria;
    private String tipo;
    private int qtdAvaliacoes;
    private int totalRespostas;
    private double notaMedia;

    public int getIdQuestao() { return idQuestao; }
    public void setIdQuestao(int idQuestao) { this.idQuestao = idQuestao; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getQtdAvaliacoes() { return qtdAvaliacoes; }
    public void setQtdAvaliacoes(int qtdAvaliacoes) { this.qtdAvaliacoes = qtdAvaliacoes; }
    public int getTotalRespostas() { return totalRespostas; }
    public void setTotalRespostas(int totalRespostas) { this.totalRespostas = totalRespostas; }
    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }
}
