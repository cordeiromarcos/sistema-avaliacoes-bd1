package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Avaliacao;
import br.uel.avaliacoes.model.Questao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AvaliacaoDAO {

    private final DataSource dataSource;
    private final QuestaoDAO questaoDAO;

    public AvaliacaoDAO(DataSource dataSource, QuestaoDAO questaoDAO) {
        this.dataSource = dataSource;
        this.questaoDAO = questaoDAO;
    }

    // RF2: Cadastrar avaliacao
    public Avaliacao inserir(Avaliacao av) throws SQLException {
        String sql = "INSERT INTO avaliacao (titulo, descricao, id_disciplina, ano, semestre, data_avaliacao, hora_inicio, hora_fim) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_avaliacao";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, av.getTitulo());
            ps.setString(2, av.getDescricao());
            ps.setInt(3, av.getIdDisciplina());
            ps.setInt(4, av.getAno());
            ps.setInt(5, av.getSemestre());
            ps.setDate(6, Date.valueOf(av.getDataAvaliacao()));
            ps.setTime(7, Time.valueOf(av.getHoraInicio()));
            ps.setTime(8, Time.valueOf(av.getHoraFim()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    av.setIdAvaliacao(rs.getInt("id_avaliacao"));
                }
            }
        }
        return av;
    }

    // RF3: Associar questoes a avaliacao em transacao
    public void associarQuestoes(int idAvaliacao, List<Integer> idsQuestoes) throws SQLException {
        String sqlDelete = "DELETE FROM avaliacao_questao WHERE id_avaliacao = ?";
        String sqlInsert = "INSERT INTO avaliacao_questao (id_avaliacao, id_questao, ordem) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                    psDel.setInt(1, idAvaliacao);
                    psDel.executeUpdate();
                }
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    int ordem = 1;
                    for (Integer idQuestao : idsQuestoes) {
                        psIns.setInt(1, idAvaliacao);
                        psIns.setInt(2, idQuestao);
                        psIns.setInt(3, ordem++);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // RF4: Indicar alunos destinatarios da avaliacao em transacao
    public void associarDestinatarios(int idAvaliacao, List<Integer> idsUsuarios) throws SQLException {
        String sql = "INSERT INTO avaliacao_destinatario (id_avaliacao, id_usuario, respondido) " +
                     "VALUES (?, ?, FALSE) ON CONFLICT (id_avaliacao, id_usuario) DO NOTHING";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Integer idUsuario : idsUsuarios) {
                    ps.setInt(1, idAvaliacao);
                    ps.setInt(2, idUsuario);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Avaliacao> listarTodas() throws SQLException {
        String sql = "SELECT a.id_avaliacao, a.titulo, a.descricao, a.id_disciplina, " +
                     "d.codigo || ' - ' || d.nome AS nome_disciplina, u.nome AS nome_docente, " +
                     "a.ano, a.semestre, a.data_avaliacao, a.hora_inicio, a.hora_fim " +
                     "FROM avaliacao a " +
                     "JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
                     "JOIN usuario u ON d.id_docente = u.id_usuario " +
                     "ORDER BY a.ano DESC, a.semestre DESC, a.id_avaliacao DESC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapAvaliacao(rs, false));
            }
        }
        return lista;
    }

    public List<Avaliacao> listarPorAluno(int idUsuario) throws SQLException {
        String sql = "SELECT a.id_avaliacao, a.titulo, a.descricao, a.id_disciplina, " +
                     "d.codigo || ' - ' || d.nome AS nome_disciplina, u.nome AS nome_docente, " +
                     "a.ano, a.semestre, a.data_avaliacao, a.hora_inicio, a.hora_fim, ad.respondido " +
                     "FROM avaliacao a " +
                     "JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
                     "JOIN usuario u ON d.id_docente = u.id_usuario " +
                     "JOIN avaliacao_destinatario ad ON a.id_avaliacao = ad.id_avaliacao " +
                     "WHERE ad.id_usuario = ? " +
                     "ORDER BY ad.respondido ASC, a.data_avaliacao DESC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAvaliacao(rs, true));
                }
            }
        }
        return lista;
    }

    public List<Questao> listarQuestoesDaAvaliacao(int idAvaliacao) throws SQLException {
        String sql = "SELECT q.id_questao, q.enunciado, q.tipo, q.categoria " +
                     "FROM avaliacao_questao aq " +
                     "JOIN questao q ON aq.id_questao = q.id_questao " +
                     "WHERE aq.id_avaliacao = ? ORDER BY aq.ordem";
        List<Questao> questoes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAvaliacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Questao q = new Questao();
                    q.setIdQuestao(rs.getInt("id_questao"));
                    q.setEnunciado(rs.getString("enunciado"));
                    q.setTipo(rs.getString("tipo"));
                    q.setCategoria(rs.getString("categoria"));
                    q.setAlternativas(questaoDAO.listarAlternativasPorQuestao(conn, q.getIdQuestao()));
                    questoes.add(q);
                }
            }
        }
        return questoes;
    }

    private Avaliacao mapAvaliacao(ResultSet rs, boolean includeRespondido) throws SQLException {
        Avaliacao a = new Avaliacao();
        a.setIdAvaliacao(rs.getInt("id_avaliacao"));
        a.setTitulo(rs.getString("titulo"));
        a.setDescricao(rs.getString("descricao"));
        a.setIdDisciplina(rs.getInt("id_disciplina"));
        a.setNomeDisciplina(rs.getString("nome_disciplina"));
        a.setNomeDocente(rs.getString("nome_docente"));
        a.setAno(rs.getInt("ano"));
        a.setSemestre(rs.getInt("semestre"));
        a.setDataAvaliacao(rs.getDate("data_avaliacao").toLocalDate());
        a.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        a.setHoraFim(rs.getTime("hora_fim").toLocalTime());
        if (includeRespondido) {
            a.setRespondido(rs.getBoolean("respondido"));
        }
        return a;
    }
}
