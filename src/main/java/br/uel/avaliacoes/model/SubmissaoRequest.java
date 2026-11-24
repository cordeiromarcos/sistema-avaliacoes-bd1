package br.uel.avaliacoes.model;

import java.util.List;

public class SubmissaoRequest {
    private Integer idAvaliacao;
    private Integer idUsuario;
    private List<RespostaItem> respostas;

    public static class RespostaItem {
        private Integer idQuestao;
        private Integer idAlternativa;
        private String textoResposta;

        public Integer getIdQuestao() { return idQuestao; }
        public void setIdQuestao(Integer idQuestao) { this.idQuestao = idQuestao; }
        public Integer getIdAlternativa() { return idAlternativa; }
        public void setIdAlternativa(Integer idAlternativa) { this.idAlternativa = idAlternativa; }
        public String getTextoResposta() { return textoResposta; }
        public void setTextoResposta(String textoResposta) { this.textoResposta = textoResposta; }
    }

    public Integer getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(Integer idAvaliacao) { this.idAvaliacao = idAvaliacao; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public List<RespostaItem> getRespostas() { return respostas; }
    public void setRespostas(List<RespostaItem> respostas) { this.respostas = respostas; }
}
