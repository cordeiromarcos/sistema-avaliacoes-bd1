let usuarioLogado = null;
let usuarios = [];
let disciplinas = [];
let questoes = [];
let avaliacoes = [];
let charts = {};

window.onload = async function () {
    const salvo = sessionStorage.getItem('usuarioLogado');
    if (salvo) {
        usuarioLogado = JSON.parse(salvo);
        await iniciarSessao(usuarioLogado);
    }
};

async function fazerLogin(event) {
    event.preventDefault();
    const login = document.getElementById('loginIdentificador').value.trim();
    const senha = document.getElementById('loginSenha').value;

    const res = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, senha })
    });

    if (res.ok) {
        usuarioLogado = await res.json();
        sessionStorage.setItem('usuarioLogado', JSON.stringify(usuarioLogado));
        await iniciarSessao(usuarioLogado);
    } else {
        const err = await res.json();
        alert('Falha no login: ' + (err.erro || 'Credenciais inválidas.'));
    }
}

function fazerLogout() {
    sessionStorage.removeItem('usuarioLogado');
    usuarioLogado = null;
    document.getElementById('areaSistema').style.display = 'none';
    document.getElementById('secLogin').style.display = 'block';
    document.getElementById('loginSenha').value = '1234';
}

async function iniciarSessao(usuario) {
    document.getElementById('secLogin').style.display = 'none';
    document.getElementById('areaSistema').style.display = 'block';
    document.getElementById('infoUsuarioAtivo').textContent =
        `${usuario.nome} | Papel: ${usuario.papel} | Matrícula: ${usuario.matricula} (${usuario.email})`;

    const ehAluno = (usuario.papel === 'ALUNO');
    document.getElementById('btnNavQuestoes').style.display = ehAluno ? 'none' : 'inline-block';
    document.getElementById('btnNavAvaliacoes').style.display = ehAluno ? 'none' : 'inline-block';
    document.getElementById('btnNavResponder').style.display = ehAluno ? 'inline-block' : 'none';
    document.getElementById('btnNavRelatorios').style.display = 'inline-block';

    if (ehAluno) {
        mostrarSecao('secResponder');
        await carregarTodosRelatorios();
    } else {
        await carregarUsuarios();
        await carregarDisciplinas();
        await carregarQuestoes();
        await carregarAvaliacoes();
        await carregarTodosRelatorios();
        mostrarSecao('secQuestoes');
    }
}

function headersAutenticados() {
    return {
        'Content-Type': 'application/json',
        'X-Usuario-Id': usuarioLogado ? usuarioLogado.idUsuario : ''
    };
}

function mostrarSecao(id) {
    if (!usuarioLogado) return;

    const ehAluno = (usuarioLogado.papel === 'ALUNO');
    if (ehAluno && (id === 'secQuestoes' || id === 'secAvaliacoes')) {
        alert('Acesso restrito: alunos não podem cadastrar questões ou avaliações.');
        return;
    }
    if (!ehAluno && id === 'secResponder') {
        alert('Acesso restrito: docentes e coordenadores não respondem avaliações.');
        return;
    }

    ['secQuestoes', 'secAvaliacoes', 'secResponder', 'secRelatorios'].forEach(s => {
        document.getElementById(s).style.display = (s === id) ? 'block' : 'none';
    });

    if (id === 'secResponder') {
        carregarAvaliacoesDoUsuarioAtivo();
    }
    if (id === 'secRelatorios') {
        carregarTodosRelatorios();
    }
}

async function carregarUsuarios() {
    const res = await fetch('/api/usuarios');
    usuarios = await res.json();

    const boxAlunos = document.getElementById('listaCheckAlunos');
    boxAlunos.innerHTML = '';
    usuarios.filter(u => u.papel === 'ALUNO').forEach(a => {
        boxAlunos.innerHTML += `<label><input type="checkbox" class="chkAluno" value="${a.idUsuario}"> ${a.matricula} - ${a.nome}</label><br>`;
    });
}

async function carregarDisciplinas() {
    const res = await fetch('/api/disciplinas');
    disciplinas = await res.json();
    const sel = document.getElementById('avDisciplina');
    sel.innerHTML = '';
    disciplinas.forEach(d => {
        const opt = document.createElement('option');
        opt.value = d.idDisciplina;
        opt.textContent = `${d.codigo} - ${d.nome} (${d.nomeDocente})`;
        sel.appendChild(opt);
    });
}

function alternarCamposAlternativas() {
    const tipo = document.getElementById('qTipo').value;
    document.getElementById('boxAlternativas').style.display = (tipo === 'MULTIPLA_ESCOLHA') ? 'block' : 'none';
}

async function cadastrarQuestao(event) {
    event.preventDefault();
    const tipo = document.getElementById('qTipo').value;
    const alternativas = [];
    if (tipo === 'MULTIPLA_ESCOLHA') {
        for (let i = 1; i <= 5; i++) {
            const texto = document.getElementById(`altTexto${i}`).value.trim();
            const peso = parseInt(document.getElementById(`altPeso${i}`).value);
            if (texto) {
                alternativas.push({ texto: texto, valorEscala: peso, correta: peso === 5 });
            }
        }
    }

    const payload = {
        enunciado: document.getElementById('qEnunciado').value,
        tipo: tipo,
        categoria: document.getElementById('qCategoria').value,
        alternativas: alternativas
    };

    const res = await fetch('/api/questoes', {
        method: 'POST',
        headers: headersAutenticados(),
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        alert('Questão cadastrada com sucesso!');
        document.getElementById('qEnunciado').value = '';
        await carregarQuestoes();
    } else {
        const err = await res.json();
        alert('Erro: ' + err.erro);
    }
}

async function carregarQuestoes() {
    const res = await fetch('/api/questoes');
    questoes = await res.json();

    const tbody = document.getElementById('tabelaQuestoes');
    const boxCheck = document.getElementById('listaCheckQuestoes');
    tbody.innerHTML = '';
    boxCheck.innerHTML = '';

    questoes.forEach(q => {
        tbody.innerHTML += `<tr>
            <td>${q.idQuestao}</td>
            <td>${q.enunciado}</td>
            <td>${q.tipo}</td>
            <td>${q.categoria}</td>
            <td>${q.alternativas ? q.alternativas.length : 0}</td>
        </tr>`;

        boxCheck.innerHTML += `<label><input type="checkbox" class="chkQuestao" value="${q.idQuestao}"> [${q.idQuestao}] (${q.tipo}) ${q.enunciado}</label><br>`;
    });
}

async function cadastrarAvaliacao(event) {
    event.preventDefault();
    const payload = {
        titulo: document.getElementById('avTitulo').value,
        descricao: document.getElementById('avDescricao').value,
        idDisciplina: parseInt(document.getElementById('avDisciplina').value),
        ano: parseInt(document.getElementById('avAno').value),
        semestre: parseInt(document.getElementById('avSemestre').value),
        dataAvaliacao: document.getElementById('avData').value,
        horaInicio: document.getElementById('avInicio').value + ':00',
        horaFim: document.getElementById('avFim').value + ':00'
    };

    const res = await fetch('/api/avaliacoes', {
        method: 'POST',
        headers: headersAutenticados(),
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        alert('Avaliação cadastrada com sucesso!');
        document.getElementById('avTitulo').value = '';
        await carregarAvaliacoes();
    } else {
        const err = await res.json();
        alert('Erro ao cadastrar avaliação (verifique unicidade de disciplina/ano/semestre): ' + err.erro);
    }
}

async function carregarAvaliacoes() {
    const res = await fetch('/api/avaliacoes');
    avaliacoes = await res.json();

    const tbody = document.getElementById('tabelaAvaliacoes');
    const selQ = document.getElementById('selAvQuestoes');
    const selD = document.getElementById('selAvDestinatarios');
    tbody.innerHTML = '';
    selQ.innerHTML = '';
    selD.innerHTML = '';

    avaliacoes.forEach(a => {
        tbody.innerHTML += `<tr>
            <td>${a.idAvaliacao}</td>
            <td>${a.titulo}</td>
            <td>${a.nomeDisciplina}</td>
            <td>${a.nomeDocente}</td>
            <td>${a.ano}/${a.semestre}</td>
            <td>${a.dataAvaliacao}</td>
            <td>${a.horaInicio} - ${a.horaFim}</td>
        </tr>`;

        const label = `${a.idAvaliacao} - ${a.titulo}`;
        selQ.innerHTML += `<option value="${a.idAvaliacao}">${label}</option>`;
        selD.innerHTML += `<option value="${a.idAvaliacao}">${label}</option>`;
    });
}

async function salvarQuestoesAvaliacao() {
    const idAvaliacao = document.getElementById('selAvQuestoes').value;
    const ids = Array.from(document.querySelectorAll('.chkQuestao:checked')).map(c => parseInt(c.value));
    if (ids.length === 0) {
        alert('Selecione ao menos uma questão.');
        return;
    }
    const res = await fetch(`/api/avaliacoes/${idAvaliacao}/questoes`, {
        method: 'POST',
        headers: headersAutenticados(),
        body: JSON.stringify(ids)
    });
    if (res.ok) {
        alert('Questões associadas à avaliação!');
    } else {
        const err = await res.json();
        alert('Erro: ' + err.erro);
    }
}

async function salvarDestinatariosAvaliacao() {
    const idAvaliacao = document.getElementById('selAvDestinatarios').value;
    const ids = Array.from(document.querySelectorAll('.chkAluno:checked')).map(c => parseInt(c.value));
    if (ids.length === 0) {
        alert('Selecione ao menos um aluno.');
        return;
    }
    const res = await fetch(`/api/avaliacoes/${idAvaliacao}/destinatarios`, {
        method: 'POST',
        headers: headersAutenticados(),
        body: JSON.stringify(ids)
    });
    if (res.ok) {
        alert('Alunos destinatários vinculados!');
    } else {
        const err = await res.json();
        alert('Erro: ' + err.erro);
    }
}

async function carregarAvaliacoesDoUsuarioAtivo() {
    if (!usuarioLogado) return;

    const res = await fetch(`/api/avaliacoes/aluno/${usuarioLogado.idUsuario}`);
    const lista = await res.json();

    const sel = document.getElementById('selAvaliacaoResponder');
    sel.innerHTML = '';
    lista.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.idAvaliacao;
        opt.dataset.respondido = a.respondido;
        opt.textContent = `${a.titulo} [${a.respondido ? 'JÁ RESPONDIDA' : 'PENDENTE'}]`;
        sel.appendChild(opt);
    });
    await carregarFormularioResposta();
}

async function carregarFormularioResposta() {
    const sel = document.getElementById('selAvaliacaoResponder');
    const form = document.getElementById('formResponder');
    const statusDiv = document.getElementById('statusAvaliacaoAluno');
    form.innerHTML = '';
    statusDiv.innerHTML = '';

    if (!sel.value) {
        statusDiv.innerHTML = '<p>Nenhuma avaliação atribuída ao seu usuário.</p>';
        return;
    }

    const opt = sel.options[sel.selectedIndex];
    if (opt.dataset.respondido === 'true') {
        statusDiv.innerHTML = '<p><strong>Você já respondeu esta avaliação neste semestre.</strong> (Apenas 1 participação por disciplina/semestre).</p>';
        return;
    }

    const res = await fetch(`/api/avaliacoes/${sel.value}/questoes`);
    const qs = await res.json();

    if (qs.length === 0) {
        statusDiv.innerHTML = '<p>Esta avaliação ainda não possui questões associadas.</p>';
        return;
    }

    qs.forEach((q, idx) => {
        let html = `<fieldset><legend>Questão ${idx + 1} (${q.categoria})</legend><p>${q.enunciado}</p>`;
        if (q.tipo === 'MULTIPLA_ESCOLHA') {
            q.alternativas.forEach(alt => {
                html += `<label><input type="radio" name="q_${q.idQuestao}" value="${alt.idAlternativa}" required> ${alt.texto}</label><br>`;
            });
        } else {
            html += `<textarea name="txt_${q.idQuestao}" rows="2" cols="60" required placeholder="Digite sua resposta..."></textarea>`;
        }
        html += `</fieldset><br>`;
        form.innerHTML += html;
    });

    form.innerHTML += `<button type="submit">Enviar Avaliação Anonimamente</button>`;
}

async function enviarRespostaAnonima(event) {
    event.preventDefault();
    const idAvaliacao = parseInt(document.getElementById('selAvaliacaoResponder').value);
    const idUsuario = usuarioLogado.idUsuario;

    const resQ = await fetch(`/api/avaliacoes/${idAvaliacao}/questoes`);
    const qs = await resQ.json();

    const respostas = qs.map(q => {
        if (q.tipo === 'MULTIPLA_ESCOLHA') {
            const marcada = document.querySelector(`input[name="q_${q.idQuestao}"]:checked`);
            return { idQuestao: q.idQuestao, idAlternativa: parseInt(marcada.value), textoResposta: null };
        } else {
            const txt = document.querySelector(`textarea[name="txt_${q.idQuestao}"]`).value;
            return { idQuestao: q.idQuestao, idAlternativa: null, textoResposta: txt };
        }
    });

    const res = await fetch('/api/submissoes', {
        method: 'POST',
        headers: headersAutenticados(),
        body: JSON.stringify({ idAvaliacao, idUsuario, respostas })
    });

    if (res.ok) {
        alert('Avaliação registrada anonimamente com sucesso!');
        await carregarAvaliacoesDoUsuarioAtivo();
        await carregarTodosRelatorios();
    } else {
        const err = await res.json();
        alert('Erro: ' + err.erro);
    }
}

function renderizarGraficoMonocromatico(canvasId, tipo, labels, datasets) {
    if (charts[canvasId]) {
        charts[canvasId].destroy();
    }
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (typeof Chart === 'undefined') return;
    charts[canvasId] = new Chart(ctx, {
        type: tipo,
        data: { labels, datasets },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true } }
        }
    });
}

async function carregarTodosRelatorios() {
    const r1 = await (await fetch('/api/relatorios/disciplinas')).json();
    const tb1 = document.getElementById('tbodyRel1');
    tb1.innerHTML = '';
    r1.forEach(item => {
        tb1.innerHTML += `<tr>
            <td>${item.posicao}º</td>
            <td>${item.codigo}</td>
            <td>${item.disciplina}</td>
            <td>${item.docente}</td>
            <td>${item.periodo}</td>
            <td>${item.totalSubmissoes}</td>
            <td>${item.mediaDisciplina.toFixed(2)}</td>
            <td>${item.mediaGeralInstituicao.toFixed(2)}</td>
            <td>${item.situacao}</td>
        </tr>`;
    });
    renderizarGraficoMonocromatico('chartRel1', 'bar',
        r1.map(x => `${x.codigo} (${x.periodo})`),
        [
            { label: 'Média da Disciplina', data: r1.map(x => x.mediaDisciplina), backgroundColor: '#444444' },
            { label: 'Média Geral (Subconsulta)', data: r1.map(x => x.mediaGeralInstituicao), backgroundColor: '#AAAAAA' }
        ]
    );

    const r2 = await (await fetch('/api/relatorios/adesao')).json();
    const tb2 = document.getElementById('tbodyRel2');
    tb2.innerHTML = '';
    r2.resumo.forEach(item => {
        tb2.innerHTML += `<tr>
            <td>${item.titulo}</td>
            <td>${item.disciplina}</td>
            <td>${item.periodo}</td>
            <td>${item.totalConvocados}</td>
            <td>${item.totalRespondidos}</td>
            <td>${item.totalPendentes}</td>
            <td>${item.percentualAdesao}%</td>
        </tr>`;
    });
    renderizarGraficoMonocromatico('chartRel2', 'bar',
        r2.resumo.map(x => x.disciplina),
        [
            { label: 'Respondidos', data: r2.resumo.map(x => x.totalRespondidos), backgroundColor: '#333333' },
            { label: 'Pendentes', data: r2.resumo.map(x => x.totalPendentes), backgroundColor: '#BBBBBB' }
        ]
    );

    const tb2p = document.getElementById('tbodyRel2Pendentes');
    tb2p.innerHTML = '';
    r2.pendentes.forEach(p => {
        tb2p.innerHTML += `<tr>
            <td>${p.avaliacao}</td>
            <td>${p.disciplina}</td>
            <td>${p.matricula}</td>
            <td>${p.aluno}</td>
            <td>${p.email}</td>
        </tr>`;
    });

    const r3 = await (await fetch('/api/relatorios/questoes')).json();
    const tb3 = document.getElementById('tbodyRel3');
    tb3.innerHTML = '';
    r3.forEach(q => {
        tb3.innerHTML += `<tr>
            <td>${q.idQuestao}</td>
            <td>${q.enunciado}</td>
            <td>${q.categoria}</td>
            <td>${q.qtdAvaliacoes}</td>
            <td>${q.totalRespostas}</td>
            <td>${q.totalRespostas === 0 ? 'Sem respostas (Não utilizada)' : q.notaMedia.toFixed(2)}</td>
        </tr>`;
    });
    renderizarGraficoMonocromatico('chartRel3', 'bar',
        r3.map(q => `Q${q.idQuestao} (${q.categoria})`),
        [{ label: 'Nota Média (1 a 5)', data: r3.map(q => q.notaMedia), backgroundColor: '#555555' }]
    );

    await carregarRelatorio4();
}

async function carregarRelatorio4() {
    const di = document.getElementById('fDataInicio').value;
    const df = document.getElementById('fDataFim').value;
    const sem = document.getElementById('fSemestre').value;
    const termo = encodeURIComponent(document.getElementById('fTermo').value);

    let url = `/api/relatorios/periodo?dataInicio=${di}&dataFim=${df}&termo=${termo}`;
    if (sem) url += `&semestre=${sem}`;

    const r4 = await (await fetch(url)).json();
    const tb4 = document.getElementById('tbodyRel4');
    tb4.innerHTML = '';
    r4.forEach(item => {
        tb4.innerHTML += `<tr>
            <td>${item.dataAvaliacao}</td>
            <td>${item.periodo}</td>
            <td>${item.codigo}</td>
            <td>${item.disciplina}</td>
            <td>${item.docente}</td>
            <td>${item.totalRespostas}</td>
            <td>${item.notaMedia.toFixed(2)}</td>
        </tr>`;
    });
    renderizarGraficoMonocromatico('chartRel4', 'line',
        r4.map(x => `${x.dataAvaliacao} - ${x.codigo}`),
        [{ label: 'Nota Média no Período', data: r4.map(x => x.notaMedia), borderColor: '#222222', backgroundColor: '#777777' }]
    );
}