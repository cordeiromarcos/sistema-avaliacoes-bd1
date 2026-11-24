package br.uel.avaliacoes.model;

public class Alternativa {
    private Integer idAlternativa;
    private Integer idQuestao;
    private String texto;
    private Integer valorEscala;
    private Boolean correta;

    public Integer getIdAlternativa() { return idAlternativa; }
    public void setIdAlternativa(Integer idAlternativa) { this.idAlternativa = idAlternativa; }
    public Integer getIdQuestao() { return idQuestao; }
    public void setIdQuestao(Integer idQuestao) { this.idQuestao = idQuestao; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Integer getValorEscala() { return valorEscala; }
    public void setValorEscala(Integer valorEscala) { this.valorEscala = valorEscala; }
    public Boolean getCorreta() { return correta; }
    public void setCorreta(Boolean correta) { this.correta = correta; }
}
