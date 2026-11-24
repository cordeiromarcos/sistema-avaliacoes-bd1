package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.AuthDAO;
import br.uel.avaliacoes.dao.SubmissaoDAO;
import br.uel.avaliacoes.model.SubmissaoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/submissoes")
public class SubmissaoController {

    private final SubmissaoDAO submissaoDAO;
    private final AuthDAO authDAO;

    public SubmissaoController(SubmissaoDAO submissaoDAO, AuthDAO authDAO) {
        this.submissaoDAO = submissaoDAO;
        this.authDAO = authDAO;
    }

    @PostMapping
    public ResponseEntity<?> responder(
            @RequestHeader(value = "X-Usuario-Id", required = false) Integer idUsuarioHeader,
            @RequestBody SubmissaoRequest request) {
        Integer idUsuario = (idUsuarioHeader != null) ? idUsuarioHeader : request.getIdUsuario();
        String papel = authDAO.buscarPapelPorId(idUsuario);
        if (!"ALUNO".equals(papel)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas alunos podem responder avaliacoes."));
        }

        try {
            submissaoDAO.registrarSubmissaoAnonima(request);
            return ResponseEntity.ok(Map.of("mensagem", "Avaliacao registrada anonimamente com sucesso!"));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}