package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.SubmissaoRequest;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class SubmissaoDAO {

    private final DataSource dataSource;

    public SubmissaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * RF5 e RF6: Registra a avaliacao anonima em transacao unica.
     * 1. Verifica e bloqueia a linha do destinatario (FOR UPDATE) garantindo que so responda 1 vez.
     * 2. Marca respondido = TRUE em avaliacao_destinatario.
     * 3. Cria registro em submissao_anonima SEM id_usuario.
     * 4. Grava as respostas ligadas apenas ao id_submissao anonimo.
     */
    public void registrarSubmissaoAnonima(SubmissaoRequest req) throws SQLException {
        String sqlCheck = "SELECT respondido FROM avaliacao_destinatario " +
                          "WHERE id_avaliacao = ? AND id_usuario = ? FOR UPDATE";
        String sqlUpdateDest = "UPDATE avaliacao_destinatario " +
                               "SET respondido = TRUE, data_participacao = CURRENT_DATE " +
                               "WHERE id_avaliacao = ? AND id_usuario = ?";
        String sqlInsertSub = "INSERT INTO submissao_anonima (id_avaliacao, data_submissao) " +
                              "VALUES (?, CURRENT_DATE) RETURNING id_submissao";
        String sqlInsertResp = "INSERT INTO resposta (id_submissao, id_questao, id_alternativa, texto_resposta) " +
                               "VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                    psCheck.setInt(1, req.getIdAvaliacao());
                    psCheck.setInt(2, req.getIdUsuario());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Usuario nao esta habilitado como destinatario desta avaliacao.");
                        }
                        if (rs.getBoolean("respondido")) {
                            throw new SQLException("Este aluno ja avaliou esta disciplina neste ano/semestre.");
                        }
                    }
                }

                try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdateDest)) {
                    psUpd.setInt(1, req.getIdAvaliacao());
                    psUpd.setInt(2, req.getIdUsuario());
                    psUpd.executeUpdate();
                }

                int idSubmissao;
                try (PreparedStatement psSub = conn.prepareStatement(sqlInsertSub)) {
                    psSub.setInt(1, req.getIdAvaliacao());
                    try (ResultSet rsSub = psSub.executeQuery()) {
                        if (!rsSub.next()) {
                            throw new SQLException("Falha ao criar submissao anonima.");
                        }
                        idSubmissao = rsSub.getInt("id_submissao");
                    }
                }

                try (PreparedStatement psResp = conn.prepareStatement(sqlInsertResp)) {
                    for (SubmissaoRequest.RespostaItem item : req.getRespostas()) {
                        psResp.setInt(1, idSubmissao);
                        psResp.setInt(2, item.getIdQuestao());
                        if (item.getIdAlternativa() != null) {
                            psResp.setInt(3, item.getIdAlternativa());
                        } else {
                            psResp.setNull(3, Types.INTEGER);
                        }
                        if (item.getTextoResposta() != null && !item.getTextoResposta().isBlank()) {
                            psResp.setString(4, item.getTextoResposta().trim());
                        } else {
                            psResp.setNull(4, Types.VARCHAR);
                        }
                        psResp.addBatch();
                    }
                    psResp.executeBatch();
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
}
