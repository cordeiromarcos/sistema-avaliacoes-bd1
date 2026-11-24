package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RelatorioDAO {

    private final DataSource dataSource;

    public RelatorioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Relatorio 1: Ranking e media por disciplina vs media geral (JOIN, GROUP BY, AVG, Subconsulta, RANK)
    public List<RelatorioDisciplina> relatorioRankingDisciplinas() throws SQLException {
        String sql = "SELECT " +
            "RANK() OVER (ORDER BY AVG(alt.valor_escala) DESC) AS posicao, " +
            "d.codigo, " +
            "d.nome AS disciplina, " +
            "u.nome AS docente, " +
            "a.ano || '/' || a.semestre AS periodo, " +
            "COUNT(DISTINCT sa.id_submissao) AS total_submissoes, " +
            "ROUND(AVG(alt.valor_escala), 2) AS media_disciplina, " +
            "(SELECT ROUND(AVG(alt2.valor_escala), 2) " +
            " FROM resposta r2 JOIN alternativa alt2 ON r2.id_alternativa = alt2.id_alternativa " +
            " WHERE alt2.valor_escala IS NOT NULL) AS media_geral_instituicao, " +
            "CASE WHEN AVG(alt.valor_escala) >= (" +
            " SELECT AVG(alt3.valor_escala) FROM resposta r3 JOIN alternativa alt3 ON r3.id_alternativa = alt3.id_alternativa WHERE alt3.valor_escala IS NOT NULL" +
            ") THEN 'Acima da media geral' ELSE 'Abaixo da media geral' END AS situacao " +
            "FROM disciplina d " +
            "JOIN usuario u ON d.id_docente = u.id_usuario " +
            "JOIN avaliacao a ON d.id_disciplina = a.id_disciplina " +
            "JOIN submissao_anonima sa ON a.id_avaliacao = sa.id_avaliacao " +
            "JOIN resposta r ON sa.id_submissao = r.id_submissao " +
            "JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa " +
            "WHERE alt.valor_escala IS NOT NULL " +
            "GROUP BY d.id_disciplina, d.codigo, d.nome, u.nome, a.ano, a.semestre " +
            "ORDER BY media_disciplina DESC";

        List<RelatorioDisciplina> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RelatorioDisciplina item = new RelatorioDisciplina();
                item.setPosicao(rs.getInt("posicao"));
                item.setCodigo(rs.getString("codigo"));
                item.setDisciplina(rs.getString("disciplina"));
                item.setDocente(rs.getString("docente"));
                item.setPeriodo(rs.getString("periodo"));
                item.setTotalSubmissoes(rs.getInt("total_submissoes"));
                item.setMediaDisciplina(rs.getDouble("media_disciplina"));
                item.setMediaGeralInstituicao(rs.getDouble("media_geral_instituicao"));
                item.setSituacao(rs.getString("situacao"));
                lista.add(item);
            }
        }
        return lista;
    }

    // Relatorio 2A: Adesao por avaliacao (LEFT JOIN, COUNT, SUM, GROUP BY)
    public List<RelatorioAdesao> relatorioAdesao() throws SQLException {
        String sql = "SELECT " +
            "a.id_avaliacao, " +
            "a.titulo, " +
            "d.codigo || ' - ' || d.nome AS disciplina, " +
            "a.ano || '/' || a.semestre AS periodo, " +
            "COUNT(ad.id_usuario) AS total_convocados, " +
            "COALESCE(SUM(CASE WHEN ad.respondido THEN 1 ELSE 0 END), 0) AS total_respondidos, " +
            "COALESCE(SUM(CASE WHEN NOT ad.respondido THEN 1 ELSE 0 END), 0) AS total_pendentes, " +
            "CASE WHEN COUNT(ad.id_usuario) = 0 THEN 0 " +
            "ELSE ROUND(100.0 * SUM(CASE WHEN ad.respondido THEN 1 ELSE 0 END) / COUNT(ad.id_usuario), 1) END AS percentual_adesao " +
            "FROM avaliacao a " +
            "JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
            "LEFT JOIN avaliacao_destinatario ad ON a.id_avaliacao = ad.id_avaliacao " +
            "GROUP BY a.id_avaliacao, a.titulo, d.codigo, d.nome, a.ano, a.semestre " +
            "ORDER BY a.ano DESC, a.semestre DESC, a.titulo";

        List<RelatorioAdesao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RelatorioAdesao item = new RelatorioAdesao();
                item.setIdAvaliacao(rs.getInt("id_avaliacao"));
                item.setTitulo(rs.getString("titulo"));
                item.setDisciplina(rs.getString("disciplina"));
                item.setPeriodo(rs.getString("periodo"));
                item.setTotalConvocados(rs.getInt("total_convocados"));
                item.setTotalRespondidos(rs.getInt("total_respondidos"));
                item.setTotalPendentes(rs.getInt("total_pendentes"));
                item.setPercentualAdesao(rs.getDouble("percentual_adesao"));
                lista.add(item);
            }
        }
        return lista;
    }

    // Relatorio 2B: Alunos pendentes (LEFT JOIN)
    public List<RelatorioPendente> relatorioPendentes() throws SQLException {
        String sql = "SELECT " +
            "a.titulo AS avaliacao, " +
            "d.nome AS disciplina, " +
            "u.matricula, " +
            "u.nome AS aluno, " +
            "u.email " +
            "FROM avaliacao_destinatario ad " +
            "JOIN usuario u ON ad.id_usuario = u.id_usuario " +
            "LEFT JOIN avaliacao a ON ad.id_avaliacao = a.id_avaliacao " +
            "LEFT JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
            "WHERE ad.respondido = FALSE " +
            "ORDER BY a.titulo, u.nome";

        List<RelatorioPendente> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RelatorioPendente item = new RelatorioPendente();
                item.setAvaliacao(rs.getString("avaliacao"));
                item.setDisciplina(rs.getString("disciplina"));
                item.setMatricula(rs.getString("matricula"));
                item.setAluno(rs.getString("aluno"));
                item.setEmail(rs.getString("email"));
                lista.add(item);
            }
        }
        return lista;
    }

    // Relatorio 3: Questoes com menor media e nao utilizadas (LEFT JOIN, GROUP BY, HAVING, FETCH FIRST)
    public List<RelatorioQuestao> relatorioQuestoesCriticas() throws SQLException {
        String sql = "SELECT " +
            "q.id_questao, " +
            "q.enunciado, " +
            "q.categoria, " +
            "q.tipo, " +
            "COUNT(DISTINCT aq.id_avaliacao) AS qtd_avaliacoes, " +
            "COUNT(r.id_resposta) AS total_respostas, " +
            "COALESCE(ROUND(AVG(alt.valor_escala), 2), 0) AS nota_media " +
            "FROM questao q " +
            "LEFT JOIN avaliacao_questao aq ON q.id_questao = aq.id_questao " +
            "LEFT JOIN resposta r ON q.id_questao = r.id_questao " +
            "LEFT JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa " +
            "WHERE q.tipo = 'MULTIPLA_ESCOLHA' " +
            "GROUP BY q.id_questao, q.enunciado, q.categoria, q.tipo " +
            "HAVING COUNT(r.id_resposta) >= 0 " +
            "ORDER BY CASE WHEN COUNT(r.id_resposta) = 0 THEN 1 ELSE 0 END ASC, nota_media ASC, q.id_questao ASC " +
            "FETCH FIRST 10 ROWS ONLY";

        List<RelatorioQuestao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RelatorioQuestao item = new RelatorioQuestao();
                item.setIdQuestao(rs.getInt("id_questao"));
                item.setEnunciado(rs.getString("enunciado"));
                item.setCategoria(rs.getString("categoria"));
                item.setTipo(rs.getString("tipo"));
                item.setQtdAvaliacoes(rs.getInt("qtd_avaliacoes"));
                item.setTotalRespostas(rs.getInt("total_respostas"));
                item.setNotaMedia(rs.getDouble("nota_media"));
                lista.add(item);
            }
        }
        return lista;
    }

    // Relatorio 4: Filtros diversos (WHERE com BETWEEN de datas, IN de semestres, LIKE)
    public List<RelatorioPeriodo> relatorioPorPeriodo(LocalDate dataInicio, LocalDate dataFim, Integer semestre, String termo) throws SQLException {
        String sql = "SELECT " +
            "a.data_avaliacao, " +
            "a.ano || '/' || a.semestre AS periodo, " +
            "d.codigo, " +
            "d.nome AS disciplina, " +
            "u.nome AS docente, " +
            "COUNT(DISTINCT sa.id_submissao) AS total_respostas, " +
            "ROUND(AVG(alt.valor_escala), 2) AS nota_media " +
            "FROM avaliacao a " +
            "JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
            "JOIN usuario u ON d.id_docente = u.id_usuario " +
            "JOIN submissao_anonima sa ON a.id_avaliacao = sa.id_avaliacao " +
            "JOIN resposta r ON sa.id_submissao = r.id_submissao " +
            "JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa " +
            "WHERE a.data_avaliacao BETWEEN ? AND ? " +
            "  AND a.semestre IN (?, ?) " +
            "  AND (LOWER(d.nome) LIKE ? OR LOWER(u.nome) LIKE ?) " +
            "GROUP BY a.id_avaliacao, a.data_avaliacao, a.ano, a.semestre, d.codigo, d.nome, u.nome " +
            "ORDER BY a.data_avaliacao ASC";

        List<RelatorioPeriodo> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dataInicio));
            ps.setDate(2, Date.valueOf(dataFim));
            if (semestre != null && (semestre == 1 || semestre == 2)) {
                ps.setInt(3, semestre);
                ps.setInt(4, semestre);
            } else {
                ps.setInt(3, 1);
                ps.setInt(4, 2);
            }
            String likePattern = "%" + (termo == null ? "" : termo.trim().toLowerCase()) + "%";
            ps.setString(5, likePattern);
            ps.setString(6, likePattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RelatorioPeriodo item = new RelatorioPeriodo();
                    item.setDataAvaliacao(rs.getDate("data_avaliacao").toString());
                    item.setPeriodo(rs.getString("periodo"));
                    item.setCodigo(rs.getString("codigo"));
                    item.setDisciplina(rs.getString("disciplina"));
                    item.setDocente(rs.getString("docente"));
                    item.setTotalRespostas(rs.getInt("total_respostas"));
                    item.setNotaMedia(rs.getDouble("nota_media"));
                    lista.add(item);
                }
            }
        }
        return lista;
    }
}
