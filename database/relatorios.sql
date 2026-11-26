-- ============================================================================
-- RELATÓRIO 1: Ranking e Média de Avaliação por Disciplina e Docente
-- ============================================================================
SELECT
    RANK() OVER (ORDER BY AVG(alt.valor_escala) DESC) AS posicao,
    d.codigo,
    d.nome AS disciplina,
    u.nome AS docente,
    a.ano || '/' || a.semestre AS periodo,
    COUNT(DISTINCT sa.id_submissao) AS total_submissoes,
    ROUND(AVG(alt.valor_escala), 2) AS media_disciplina,
    (
        SELECT ROUND(AVG(alt2.valor_escala), 2)
        FROM resposta r2
        JOIN alternativa alt2 ON r2.id_alternativa = alt2.id_alternativa
        WHERE alt2.valor_escala IS NOT NULL
    ) AS media_geral_instituicao,
    CASE
        WHEN AVG(alt.valor_escala) >= (
            SELECT AVG(alt3.valor_escala)
            FROM resposta r3
            JOIN alternativa alt3 ON r3.id_alternativa = alt3.id_alternativa
            WHERE alt3.valor_escala IS NOT NULL
        ) THEN 'Acima da média geral'
        ELSE 'Abaixo da média geral'
    END AS situacao
FROM disciplina d
JOIN usuario u ON d.id_docente = u.id_usuario
JOIN avaliacao a ON d.id_disciplina = a.id_disciplina
JOIN submissao_anonima sa ON a.id_avaliacao = sa.id_avaliacao
JOIN resposta r ON sa.id_submissao = r.id_submissao
JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa
WHERE alt.valor_escala IS NOT NULL
GROUP BY d.id_disciplina, d.codigo, d.nome, u.nome, a.ano, a.semestre
ORDER BY media_disciplina DESC;

-- ============================================================================
-- RELATÓRIO 2A: Taxa de Adesão por Avaliação
-- ============================================================================
SELECT
    a.id_avaliacao,
    a.titulo,
    d.codigo || ' - ' || d.nome AS disciplina,
    a.ano || '/' || a.semestre AS periodo,
    COUNT(ad.id_usuario) AS total_convocados,
    COALESCE(SUM(CASE WHEN ad.respondido THEN 1 ELSE 0 END), 0) AS total_respondidos,
    COALESCE(SUM(CASE WHEN NOT ad.respondido THEN 1 ELSE 0 END), 0) AS total_pendentes,
    CASE
        WHEN COUNT(ad.id_usuario) = 0 THEN 0
        ELSE ROUND(100.0 * SUM(CASE WHEN ad.respondido THEN 1 ELSE 0 END) / COUNT(ad.id_usuario), 1)
    END AS percentual_adesao
FROM avaliacao a
JOIN disciplina d ON a.id_disciplina = d.id_disciplina
LEFT JOIN avaliacao_destinatario ad ON a.id_avaliacao = ad.id_avaliacao
GROUP BY a.id_avaliacao, a.titulo, d.codigo, d.nome, a.ano, a.semestre
ORDER BY a.ano DESC, a.semestre DESC, a.titulo;

-- ============================================================================
-- RELATÓRIO 2B: Listagem de Alunos Pendentes (que ainda não responderam)
-- ============================================================================
SELECT
    a.titulo AS avaliacao,
    d.nome AS disciplina,
    u.matricula,
    u.nome AS aluno,
    u.email
FROM avaliacao_destinatario ad
JOIN usuario u ON ad.id_usuario = u.id_usuario
LEFT JOIN avaliacao a ON ad.id_avaliacao = a.id_avaliacao
LEFT JOIN disciplina d ON a.id_disciplina = d.id_disciplina
WHERE ad.respondido = FALSE
ORDER BY a.titulo, u.nome;

-- ============================================================================
-- RELATÓRIO 3: Questões com Menor Média de Satisfação e Questões Não Utilizadas
-- ============================================================================
SELECT
    q.id_questao,
    q.enunciado,
    q.categoria,
    q.tipo,
    COUNT(DISTINCT aq.id_avaliacao) AS qtd_avaliacoes,
    COUNT(r.id_resposta) AS total_respostas,
    COALESCE(ROUND(AVG(alt.valor_escala), 2), 0) AS nota_media
FROM questao q
LEFT JOIN avaliacao_questao aq ON q.id_questao = aq.id_questao
LEFT JOIN resposta r ON q.id_questao = r.id_questao
LEFT JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa
WHERE q.tipo = 'MULTIPLA_ESCOLHA'
GROUP BY q.id_questao, q.enunciado, q.categoria, q.tipo
HAVING COUNT(r.id_resposta) >= 0
ORDER BY
    CASE WHEN COUNT(r.id_resposta) = 0 THEN 1 ELSE 0 END ASC,
    nota_media ASC,
    q.id_questao ASC
FETCH FIRST 10 ROWS ONLY;

-- ============================================================================
-- RELATÓRIO 4: Desempenho por Período com Filtros Dinâmicos (Datas, IN e LIKE)
-- ============================================================================
SELECT
    a.data_avaliacao,
    a.ano || '/' || a.semestre AS periodo,
    d.codigo,
    d.nome AS disciplina,
    u.nome AS docente,
    COUNT(DISTINCT sa.id_submissao) AS total_respostas,
    ROUND(AVG(alt.valor_escala), 2) AS nota_media
FROM avaliacao a
JOIN disciplina d ON a.id_disciplina = d.id_disciplina
JOIN usuario u ON d.id_docente = u.id_usuario
JOIN submissao_anonima sa ON a.id_avaliacao = sa.id_avaliacao
JOIN resposta r ON sa.id_submissao = r.id_submissao
JOIN alternativa alt ON r.id_alternativa = alt.id_alternativa
WHERE a.data_avaliacao BETWEEN '2025-01-01' AND '2026-12-31'
  AND a.semestre IN (1, 2)
  AND (LOWER(d.nome) LIKE '%%' OR LOWER(u.nome) LIKE '%%')
GROUP BY a.id_avaliacao, a.data_avaliacao, a.ano, a.semestre, d.codigo, d.nome, u.nome
ORDER BY a.data_avaliacao ASC;
