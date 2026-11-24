package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioDAO {

    private final DataSource dataSource;

    public UsuarioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id_usuario, nome, email, matricula, papel FROM usuario ORDER BY papel, nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setMatricula(rs.getString("matricula"));
                u.setPapel(rs.getString("papel"));
                lista.add(u);
            }
        }
        return lista;
    }

    public List<Usuario> listarAlunos() throws SQLException {
        String sql = "SELECT id_usuario, nome, email, matricula, papel FROM usuario WHERE papel = 'ALUNO' ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setMatricula(rs.getString("matricula"));
                u.setPapel(rs.getString("papel"));
                lista.add(u);
            }
        }
        return lista;
    }
}
