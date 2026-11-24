package br.uel.avaliacoes.model;

public class RelatorioDisciplina {
    private int posicao;
    private String codigo;
    private String disciplina;
    private String docente;
    private String periodo;
    private int totalSubmissoes;
    private double mediaDisciplina;
    private double mediaGeralInstituicao;
    private String situacao;

    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }
    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public int getTotalSubmissoes() { return totalSubmissoes; }
    public void setTotalSubmissoes(int totalSubmissoes) { this.totalSubmissoes = totalSubmissoes; }
    public double getMediaDisciplina() { return mediaDisciplina; }
    public void setMediaDisciplina(double mediaDisciplina) { this.mediaDisciplina = mediaDisciplina; }
    public double getMediaGeralInstituicao() { return mediaGeralInstituicao; }
    public void setMediaGeralInstituicao(double mediaGeralInstituicao) { this.mediaGeralInstituicao = mediaGeralInstituicao; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}
