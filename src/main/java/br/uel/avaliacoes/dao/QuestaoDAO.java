package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.Questao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QuestaoDAO {

    private final DataSource dataSource;

    public QuestaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // RF1: Cadastra questao e suas alternativas em uma transacao unica
    public Questao inserir(Questao questao) throws SQLException {
        String sqlQuestao = "INSERT INTO questao (enunciado, tipo, categoria) VALUES (?, ?, ?) RETURNING id_questao";
        String sqlAlt = "INSERT INTO alternativa (id_questao, texto, valor_escala, correta) VALUES (?, ?, ?, ?) RETURNING id_alternativa";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlQuestao)) {
                    ps.setString(1, questao.getEnunciado());
                    ps.setString(2, questao.getTipo());
                    ps.setString(3, questao.getCategoria());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            questao.setIdQuestao(rs.getInt("id_questao"));
                        }
                    }
                }

                if ("MULTIPLA_ESCOLHA".equals(questao.getTipo()) && questao.getAlternativas() != null) {
                    try (PreparedStatement psAlt = conn.prepareStatement(sqlAlt)) {
                        for (Alternativa alt : questao.getAlternativas()) {
                            psAlt.setInt(1, questao.getIdQuestao());
                            psAlt.setString(2, alt.getTexto());
                            if (alt.getValorEscala() != null) {
                                psAlt.setInt(3, alt.getValorEscala());
                            } else {
                                psAlt.setNull(3, Types.INTEGER);
                            }
                            psAlt.setBoolean(4, Boolean.TRUE.equals(alt.getCorreta()));
                            try (ResultSet rsAlt = psAlt.executeQuery()) {
                                if (rsAlt.next()) {
                                    alt.setIdAlternativa(rsAlt.getInt("id_alternativa"));
                                    alt.setIdQuestao(questao.getIdQuestao());
                                }
                            }
                        }
                    }
                }
                conn.commit();
                return questao;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Questao> listarTodas() throws SQLException {
        String sql = "SELECT id_questao, enunciado, tipo, categoria FROM questao ORDER BY id_questao";
        List<Questao> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Questao q = new Questao();
                q.setIdQuestao(rs.getInt("id_questao"));
                q.setEnunciado(rs.getString("enunciado"));
                q.setTipo(rs.getString("tipo"));
                q.setCategoria(rs.getString("categoria"));
                q.setAlternativas(listarAlternativasPorQuestao(conn, q.getIdQuestao()));
                lista.add(q);
            }
        }
        return lista;
    }

    public List<Alternativa> listarAlternativasPorQuestao(Connection conn, int idQuestao) throws SQLException {
        String sql = "SELECT id_alternativa, id_questao, texto, valor_escala, correta " +
                     "FROM alternativa WHERE id_questao = ? ORDER BY id_alternativa";
        List<Alternativa> alts = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idQuestao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alternativa a = new Alternativa();
                    a.setIdAlternativa(rs.getInt("id_alternativa"));
                    a.setIdQuestao(rs.getInt("id_questao"));
                    a.setTexto(rs.getString("texto"));
                    int val = rs.getInt("valor_escala");
                    a.setValorEscala(rs.wasNull() ? null : val);
                    a.setCorreta(rs.getBoolean("correta"));
                    alts.add(a);
                }
            }
        }
        return alts;
    }
}
