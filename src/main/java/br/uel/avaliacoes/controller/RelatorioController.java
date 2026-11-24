package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.RelatorioDAO;
import br.uel.avaliacoes.model.*;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioDAO relatorioDAO;

    public RelatorioController(RelatorioDAO relatorioDAO) {
        this.relatorioDAO = relatorioDAO;
    }

    @GetMapping("/disciplinas")
    public List<RelatorioDisciplina> disciplinas() throws SQLException {
        return relatorioDAO.relatorioRankingDisciplinas();
    }

    @GetMapping("/adesao")
    public Map<String, Object> adesao() throws SQLException {
        List<RelatorioAdesao> resumo = relatorioDAO.relatorioAdesao();
        List<RelatorioPendente> pendentes = relatorioDAO.relatorioPendentes();
        return Map.of("resumo", resumo, "pendentes", pendentes);
    }

    @GetMapping("/questoes")
    public List<RelatorioQuestao> questoes() throws SQLException {
        return relatorioDAO.relatorioQuestoesCriticas();
    }

    @GetMapping("/periodo")
    public List<RelatorioPeriodo> periodo(
            @RequestParam(defaultValue = "2025-01-01") String dataInicio,
            @RequestParam(defaultValue = "2026-12-31") String dataFim,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(defaultValue = "") String termo
    ) throws SQLException {
        return relatorioDAO.relatorioPorPeriodo(
                LocalDate.parse(dataInicio),
                LocalDate.parse(dataFim),
                semestre,
                termo
        );
    }
}
