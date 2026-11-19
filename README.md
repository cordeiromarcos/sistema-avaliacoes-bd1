# Sistema de Avaliação de Disciplinas e Docentes — Banco de Dados I (UEL)

- **Disciplina:** Banco de Dados I (2COP014/1COP017/2COP045) — Prof. Dr. Anderson Paulo Avila Santos
- **Integrantes:**
  - `MARCOS CORDEIRO MOREIRA` — RA: `202501220182`

---

## 1. Natureza do Sistema e Particularidade (RF6)

- **Natureza escolhida:** **Avaliação de disciplinas ou de docentes**.
- **Particularidade implementada:**
  1. **Anonimato garantido das respostas:** O controle de quem foi convocado e quem já respondeu fica isolado na tabela associativa `avaliacao_destinatario` (`id_avaliacao`, `id_usuario`, `respondido`, `data_participacao`), enquanto as respostas são gravadas nas tabelas `submissao_anonima` e `resposta`, que **não possuem nenhuma chave estrangeira ou coluna vinculada a `usuario`**.
  2. **Participação única por disciplina e semestre:** A restrição `UNIQUE (id_disciplina, ano, semestre)` em `avaliacao` combinada com a chave primária `(id_avaliacao, id_usuario)` em `avaliacao_destinatario` e bloqueio transacional (`SELECT ... FOR UPDATE`) impede que um mesmo aluno avalie a mesma disciplina mais de uma vez no mesmo ano/semestre.

---

## 2. Modelagem e Documentação

- **Diagrama Entidade-Relacionamento (DER):** [`docs/DER.png`](docs/DER.png)
- **Esquema Relacional e Justificativa da 3FN:** [`docs/esquema_relacional.md`](docs/esquema_relacional.md)

---

## 3. Como Rodar o Sistema (Passo a Passo)

### Pré-requisitos
- **PostgreSQL 14+**
- **Java JDK 17+** e **Apache Maven 3.8+**

### Passo 1: Criar o banco e rodar os scripts SQL
1. No **pgAdmin 4** (ou via `psql`), crie um banco de dados vazio chamado `sistema_avaliacoes`:
   ```sql
   CREATE DATABASE sistema_avaliacoes;
   ```
2. Conecte-se ao banco `sistema_avaliacoes` e execute o script de criação das tabelas:
   - [`database/schema.sql`](database/schema.sql)
3. Em seguida, execute o script de carga de dados de teste (26 usuários, 6 disciplinas, 6 avaliações, 32 questões e 46 submissões anônimas):
   - [`database/carga.sql`](database/carga.sql)

### Passo 2: Configurar a conexão (se necessário)
Edite o arquivo [`src/main/resources/application.properties`](src/main/resources/application.properties) caso o usuário ou senha do seu PostgreSQL local sejam diferentes de `postgres` / `postgres`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_avaliacoes
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Passo 3: Executar o back-end Java (JDBC puro, sem JPA)
Na raiz do projeto, execute:
```bash
mvn spring-boot:run
```
A aplicação estará disponível no navegador em: **`http://localhost:8081`**

---

## 4. Relatórios Implementados (RF7)

Todas as consultas SQL estão reunidas e comentadas em [`database/relatorios.sql`](database/relatorios.sql).

### Relatório 1 — Ranking e Média de Avaliação por Disciplina e Docente
- **Objetivo:** Classificar as disciplinas e docentes pela nota média obtida (escala 1 a 5) e comparar cada disciplina com a média institucional geral.
- **Recursos SQL utilizados:** Junção interna (`JOIN` em 6 tabelas), Agregação e agrupamento (`COUNT`, `AVG`, `GROUP BY`), Subconsulta escalar (cálculo da média geral), Ordenação e ranking (`RANK() OVER`, `ORDER BY`).
- **Captura de tela:** `![Relatório 1](docs/relatorio1.png)`

### Relatório 2 — Taxa de Adesão e Alunos Pendentes por Avaliação
- **Objetivo:** Exibir o percentual de participação por avaliação e listar os alunos convocados que ainda não responderam (preservando o sigilo das respostas de quem já respondeu).
- **Recursos SQL utilizados:** Junção externa (`LEFT JOIN`), Agregação condicional (`COUNT`, `SUM(CASE WHEN ...)`), Agrupamento (`GROUP BY`) e Ordenação (`ORDER BY`).
- **Captura de tela:** `![Relatório 2](docs/relatorio2.png)`

### Relatório 3 — Top 10 Questões com Menor Índice de Satisfação e Questões Não Utilizadas
- **Objetivo:** Identificar os pontos pedagógicos ou estruturais com menor média de avaliação, além de listar questões cadastradas no banco que ainda não foram utilizadas em nenhuma avaliação.
- **Recursos SQL utilizados:** Junção externa (`LEFT JOIN`), Agregação e agrupamento (`COUNT`, `AVG`, `GROUP BY`, `HAVING`) e Limitação de ranking (`ORDER BY`, `FETCH FIRST 10 ROWS ONLY`).
- **Captura de tela:** `![Relatório 3](docs/relatorio3.png)`

### Relatório 4 — Desempenho por Período com Filtros Dinâmicos
- **Objetivo:** Permitir filtrar as avaliações por intervalo de datas, semestre letivo e nome parcial da disciplina ou do docente, exibindo a evolução das médias.
- **Recursos SQL utilizados:** Junção interna (`JOIN`), Filtros diversos (`WHERE` com `BETWEEN` de datas, `IN` de semestres e `LIKE` textual), Agregação (`COUNT`, `AVG`, `GROUP BY`) e Ordenação (`ORDER BY`).
- **Captura de tela:** `![Relatório 4](docs/relatorio4.png)`
