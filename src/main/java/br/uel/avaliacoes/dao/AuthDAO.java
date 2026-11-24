package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuthDAO {

    private final JdbcTemplate jdbcTemplate;

    public AuthDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Usuario autenticar(String login, String senha) {
        String sql = """
            SELECT id_usuario, nome, email, matricula, papel
            FROM usuario
            WHERE (LOWER(email) = LOWER(?) OR LOWER(matricula) = LOWER(?))
              AND senha = ?
            """;

        List<Usuario> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Usuario u = new Usuario();
            u.setIdUsuario(rs.getInt("id_usuario"));
            u.setNome(rs.getString("nome"));
            u.setEmail(rs.getString("email"));
            u.setMatricula(rs.getString("matricula"));
            u.setPapel(rs.getString("papel"));
            return u;
        }, login.trim(), login.trim(), senha);

        return lista.isEmpty() ? null : lista.get(0);
    }

    public String buscarPapelPorId(Integer idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        String sql = "SELECT papel FROM usuario WHERE id_usuario = ?";
        List<String> papeis = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("papel"),
                idUsuario
        );
        return papeis.isEmpty() ? null : papeis.get(0);
    }
}
