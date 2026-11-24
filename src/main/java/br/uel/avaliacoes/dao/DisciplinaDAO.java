package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Disciplina;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DisciplinaDAO {

    private final DataSource dataSource;

    public DisciplinaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Disciplina> listarTodas() throws SQLException {
        String sql = "SELECT d.id_disciplina, d.codigo, d.nome, d.id_docente, u.nome AS nome_docente " +
                     "FROM disciplina d JOIN usuario u ON d.id_docente = u.id_usuario ORDER BY d.codigo";
        List<Disciplina> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Disciplina d = new Disciplina();
                d.setIdDisciplina(rs.getInt("id_disciplina"));
                d.setCodigo(rs.getString("codigo"));
                d.setNome(rs.getString("nome"));
                d.setIdDocente(rs.getInt("id_docente"));
                d.setNomeDocente(rs.getString("nome_docente"));
                lista.add(d);
            }
        }
        return lista;
    }
}
