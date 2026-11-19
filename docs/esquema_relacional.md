# Esquema Relacional e Justificativa da 3FN

## 1. Esquema Relacional Mapeado

Abaixo está o mapeamento do DER para o modelo relacional (chaves primárias em **negrito e sublinhadas**; chaves estrangeiras indicadas com `FK`):

1. **USUARIO** (<u>**id_usuario**</u>, nome, email `UNIQUE`, matricula `UNIQUE`, papel, senha)
2. **DISCIPLINA** (<u>**id_disciplina**</u>, codigo `UNIQUE`, nome, id_docente `FK -> USUARIO`)
3. **QUESTAO** (<u>**id_questao**</u>, enunciado, tipo, categoria)
4. **ALTERNATIVA** (<u>**id_alternativa**</u>, id_questao `FK -> QUESTAO`, texto, valor_escala, correta)
5. **AVALIACAO** (<u>**id_avaliacao**</u>, titulo, descricao, id_disciplina `FK -> DISCIPLINA`, ano, semestre, data_avaliacao, hora_inicio, hora_fim)
   - *Restrição de unicidade:* `UNIQUE (id_disciplina, ano, semestre)`
6. **AVALIACAO_QUESTAO** (<u>**id_avaliacao**</u> `FK -> AVALIACAO`, <u>**id_questao**</u> `FK -> QUESTAO`, ordem)
   - *Restrição de unicidade:* `UNIQUE (id_avaliacao, ordem)`
7. **AVALIACAO_DESTINATARIO** (<u>**id_avaliacao**</u> `FK -> AVALIACAO`, <u>**id_usuario**</u> `FK -> USUARIO`, respondido, data_participacao)
8. **SUBMISSAO_ANONIMA** (<u>**id_submissao**</u>, id_avaliacao `FK -> AVALIACAO`, data_submissao)
9. **RESPOSTA** (<u>**id_resposta**</u>, id_submissao `FK -> SUBMISSAO_ANONIMA`, id_questao `FK -> QUESTAO`, id_alternativa `FK -> ALTERNATIVA NULL`, texto_resposta `NULL`)
   - *Restrição de unicidade:* `UNIQUE (id_submissao, id_questao)`

---

## 2. Garantia da Particularidade (Anonimato e Participação Única — RF6)

- **Separação estrutural:** A tabela `AVALIACAO_DESTINATARIO` controla quais alunos foram convocados e marca `respondido = TRUE` quando o aluno conclui a avaliação, mas não possui referência às respostas gravadas.
- **Impossibilidade de rastreamento:** As tabelas `SUBMISSAO_ANONIMA` e `RESPOSTA` não possuem coluna ou chave estrangeira para `USUARIO`, garantindo o anonimato absoluto das respostas.
- **Unicidade por disciplina e semestre:** A restrição `UNIQUE (id_disciplina, ano, semestre)` em `AVALIACAO` combinada com a chave primária composta `(id_avaliacao, id_usuario)` em `AVALIACAO_DESTINATARIO` impede que um mesmo aluno avalie a mesma disciplina mais de uma vez no mesmo ano/semestre.

---

## 3. Justificativa de Normalização (Terceira Forma Normal — 3FN)

O esquema relacional encontra-se na **Terceira Forma Normal (3FN)** porque todas as relações possuem apenas atributos atômicos (1FN), não há dependências parciais em nenhuma tabela com chave primária composta (`AVALIACAO_QUESTAO` possui apenas `ordem`, que depende da chave inteira `(id_avaliacao, id_questao)`, e `AVALIACAO_DESTINATARIO` possui `respondido` e `data_participacao`, que dependem da chave inteira `(id_avaliacao, id_usuario)` — 2FN), e nenhum atributo não-chave determina transitivamente outro atributo não-chave em nenhuma das nove tabelas (3FN).
