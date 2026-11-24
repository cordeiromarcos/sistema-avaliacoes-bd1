package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.AuthDAO;
import br.uel.avaliacoes.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthDAO authDAO;

    public AuthController(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String login = credenciais.get("login");
        String senha = credenciais.get("senha");

        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Informe e-mail/matrícula e senha."));
        }

        Usuario usuario = authDAO.autenticar(login, senha);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas."));
        }

        return ResponseEntity.ok(usuario);
    }
}