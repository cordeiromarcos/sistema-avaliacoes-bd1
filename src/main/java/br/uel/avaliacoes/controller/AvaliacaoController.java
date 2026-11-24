package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.AuthDAO;
import br.uel.avaliacoes.dao.AvaliacaoDAO;
import br.uel.avaliacoes.model.Avaliacao;
import br.uel.avaliacoes.model.Questao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoDAO avaliacaoDAO;
    private final AuthDAO authDAO;

    public AvaliacaoController(AvaliacaoDAO avaliacaoDAO, AuthDAO authDAO) {
        this.avaliacaoDAO = avaliacaoDAO;
        this.authDAO = authDAO;
    }

    @GetMapping
    public List<Avaliacao> listarTodas() throws SQLException {
        return avaliacaoDAO.listarTodas();
    }

    @GetMapping("/aluno/{idUsuario}")
    public List<Avaliacao> listarPorAluno(@PathVariable int idUsuario) throws SQLException {
        return avaliacaoDAO.listarPorAluno(idUsuario);
    }

    @GetMapping("/{idAvaliacao}/questoes")
    public List<Questao> listarQuestoes(@PathVariable int idAvaliacao) throws SQLException {
        return avaliacaoDAO.listarQuestoesDaAvaliacao(idAvaliacao);
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Integer idUsuario,
            @RequestBody Avaliacao avaliacao) {
        String papel = authDAO.buscarPapelPorId(idUsuario);
        if (papel == null || "ALUNO".equals(papel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas docentes ou coordenadores podem cadastrar avaliacoes."));
        }

        try {
            return ResponseEntity.ok(avaliacaoDAO.inserir(avaliacao));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{idAvaliacao}/questoes")
    public ResponseEntity<?> associarQuestoes(
            @RequestHeader(value = "X-Usuario-Id", required = false) Integer idUsuario,
            @PathVariable int idAvaliacao,
            @RequestBody List<Integer> idsQuestoes) {
        String papel = authDAO.buscarPapelPorId(idUsuario);
        if (papel == null || "ALUNO".equals(papel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas docentes ou coordenadores podem associar questoes."));
        }

        try {
            avaliacaoDAO.associarQuestoes(idAvaliacao, idsQuestoes);
            return ResponseEntity.ok(Map.of("mensagem", "Questoes associadas com sucesso."));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{idAvaliacao}/destinatarios")
    public ResponseEntity<?> associarDestinatarios(
            @RequestHeader(value = "X-Usuario-Id", required = false) Integer idUsuario,
            @PathVariable int idAvaliacao,
            @RequestBody List<Integer> idsUsuarios) {
        String papel = authDAO.buscarPapelPorId(idUsuario);
        if (papel == null || "ALUNO".equals(papel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas docentes ou coordenadores podem associar destinatarios."));
        }

        try {
            avaliacaoDAO.associarDestinatarios(idAvaliacao, idsUsuarios);
            return ResponseEntity.ok(Map.of("mensagem", "Destinatarios associados com sucesso."));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}