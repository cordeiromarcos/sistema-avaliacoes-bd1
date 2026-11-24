package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.DisciplinaDAO;
import br.uel.avaliacoes.dao.UsuarioDAO;
import br.uel.avaliacoes.model.Disciplina;
import br.uel.avaliacoes.model.Usuario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UsuarioDisciplinaController {

    private final UsuarioDAO usuarioDAO;
    private final DisciplinaDAO disciplinaDAO;

    public UsuarioDisciplinaController(UsuarioDAO usuarioDAO, DisciplinaDAO disciplinaDAO) {
        this.usuarioDAO = usuarioDAO;
        this.disciplinaDAO = disciplinaDAO;
    }

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    @GetMapping("/usuarios/alunos")
    public List<Usuario> listarAlunos() throws SQLException {
        return usuarioDAO.listarAlunos();
    }

    @GetMapping("/disciplinas")
    public List<Disciplina> listarDisciplinas() throws SQLException {
        return disciplinaDAO.listarTodas();
    }
}
