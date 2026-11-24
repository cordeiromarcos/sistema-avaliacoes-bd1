package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.AuthDAO;
import br.uel.avaliacoes.dao.QuestaoDAO;
import br.uel.avaliacoes.model.Questao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoDAO questaoDAO;
    private final AuthDAO authDAO;

    public QuestaoController(QuestaoDAO questaoDAO, AuthDAO authDAO) {
        this.questaoDAO = questaoDAO;
        this.authDAO = authDAO;
    }

    @GetMapping
    public List<Questao> listar() throws SQLException {
        return questaoDAO.listarTodas();
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Integer idUsuario,
            @RequestBody Questao questao) {
        String papel = authDAO.buscarPapelPorId(idUsuario);
        if (papel == null || "ALUNO".equals(papel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas docentes ou coordenadores podem cadastrar questoes."));
        }

        try {
            Questao criada = questaoDAO.inserir(questao);
            return ResponseEntity.ok(criada);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
