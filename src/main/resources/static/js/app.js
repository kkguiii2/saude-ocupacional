const API_URL = '/api';

let token = localStorage.getItem('token');

function setToken(t) {
    token = t;
    localStorage.setItem('token', t);
}

function getToken() {
    return token || localStorage.getItem('token');
}

function logout() {
    console.info('[Auth] logout() chamado — removendo token e redirecionando');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.replace('/login');
}

// ─────────────────────────────────────────────────────────────────────────────
// CORREÇÃO CRÍTICA:
// O apiRequest anterior chamava logout() em QUALQUER resposta 401.
// Isso causava o bug: se /api/alertas ou /api/dashboard retornasse 401
// (ex: erro de DB, usuário não encontrado no filter), o sistema apagava o
// token VÁLIDO do localStorage e mandava o usuário para o login.
// Na outra máquina, qualquer falha de rede/servidor disparava esse logout falso.
//
// NOVO COMPORTAMENTO:
// - 401 sem token → redireciona para login (sem token de forma alguma)
// - 401 com token existente → loga o erro, retorna null, NÃO destrói a sessão
// ─────────────────────────────────────────────────────────────────────────────
async function apiRequest(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const t = getToken();
    if (t) {
        headers['Authorization'] = `Bearer ${t}`;
        console.debug(`[API] ${options.method || 'GET'} ${url} | Token: ${t.substring(0, 25)}...`);
    } else {
        console.warn(`[API] ${options.method || 'GET'} ${url} | ⚠ SEM TOKEN`);
    }

    try {
        const response = await fetch(`${API_URL}${url}`, {
            ...options,
            headers
        });

        console.log(`[API] ${options.method || 'GET'} ${url} → HTTP ${response.status}`);

        if (response.status === 401) {
            const currentToken = getToken();
            console.error(`[API] ⛔ 401 em ${url} | token no storage: ${currentToken ? 'SIM' : 'NÃO'}`);

            if (!currentToken) {
                // Sem token de forma alguma → login legítimo necessário
                console.warn('[Auth] Sem token — redirecionando para /login');
                window.location.replace('/login');
            }
            // COM token: não destrói a sessão. Pode ser falha pontual do servidor.
            return null;
        }

        if (!response.ok) {
            const error = await response.text();
            console.error(`[API] Erro ${response.status} em ${url}:`, error);
            throw new Error(error || `HTTP ${response.status}`);
        }

        const text = await response.text();
        if (!text) return null;

        try {
            return JSON.parse(text);
        } catch {
            return text;
        }
    } catch (error) {
        console.error('[API] Falha na requisição:', url, error.message);
        return null;
    }
}

// ─── Módulos de API ───────────────────────────────────────────────────────────

const Auth = {
    login: async (username, password) => {
        const data = await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        if (data && data.token) {
            setToken(data.token);
            localStorage.setItem('user', JSON.stringify(data));
            console.info('[Auth] Token salvo:', data.token.substring(0, 30) + '...');
        }
        return data;
    }
};

const Colaboradores = {
    getAll: async () => {
        const r = await apiRequest('/colaboradores');
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getAtivos: async () => {
        const r = await apiRequest('/colaboradores/ativos');
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getById: async (id) => {
        const r = await apiRequest(`/colaboradores/${id}`);
        return r?.data;
    },
    getByMatricula: async (matricula) => {
        const r = await apiRequest(`/colaboradores/matricula/${matricula}`);
        return r?.data;
    },
    getBySetor: async (setor) => {
        const r = await apiRequest(`/colaboradores/setor/${setor}`);
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    save: async (data) => {
        const r = await apiRequest('/colaboradores', { method: 'POST', body: JSON.stringify(data) });
        return r?.data;
    },
    update: async (id, data) => {
        const r = await apiRequest(`/colaboradores/${id}`, { method: 'PUT', body: JSON.stringify(data) });
        return r?.data;
    },
    delete: (id) => apiRequest(`/colaboradores/${id}`, { method: 'DELETE' })
};

const Atendimentos = {
    getAll: async () => {
        const r = await apiRequest('/atendimentos');
        if (!r) return [];
        if (Array.isArray(r)) return r;
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        return [];
    },
    getHoje: async () => {
        const r = await apiRequest('/atendimentos/hoje');
        if (!r) return [];
        if (Array.isArray(r)) return r;
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        return [];
    },
    getEmergencias: async () => {
        const r = await apiRequest('/atendimentos/emergencias');
        if (!r) return [];
        if (Array.isArray(r)) return r;
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        return [];
    },
    getByColaborador: async (id) => {
        const r = await apiRequest(`/atendimentos/colaborador/${id}`);
        if (!r) return [];
        if (Array.isArray(r)) return r;
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        return [];
    },
    // O controller retorna o DTO diretamente (sem envelope {data: ...})
    save: async (data) => {
        const r = await apiRequest('/atendimentos', { method: 'POST', body: JSON.stringify(data) });
        // r já é o AtendimentoDTO ou null em caso de erro
        return r;
    }
};

const Acidentes = {
    getAll: async () => {
        const r = await apiRequest('/acidentes');
        if (!r) return [];
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getMes: async () => {
        const r = await apiRequest('/acidentes/mes');
        if (!r) return [];
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getPendentes: async () => {
        const r = await apiRequest('/acidentes/pendentes');
        if (!r) return [];
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getByColaborador: async (id) => {
        const r = await apiRequest(`/acidentes/colaborador/${id}`);
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    save: async (data) => {
        const r = await apiRequest('/acidentes', { method: 'POST', body: JSON.stringify(data) });
        return r?.data;
    },
    emitirCat: async (id) => {
        const t = getToken();
        const response = await fetch(`${API_URL}/acidentes/${id}/cat`, {
            method: 'POST',
            headers: t ? { 'Authorization': `Bearer ${t}` } : {}
        });
        if (!response.ok) throw new Error('Falha ao emitir CAT');
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `CAT-${id}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }
};

const Agendamentos = {
    getAll: async () => {
        const r = await apiRequest('/agendamentos');
        if (!r) return [];
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getPendentes: async () => {
        const r = await apiRequest('/agendamentos/pendentes');
        if (!r) return [];
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    save: async (data) => {
        const r = await apiRequest('/agendamentos', { method: 'POST', body: JSON.stringify(data) });
        return r?.data;
    },
    realizar: (id) => apiRequest(`/agendamentos/${id}/realizar`, { method: 'POST' }),
    cancelar: (id) => apiRequest(`/agendamentos/${id}/cancelar`, { method: 'POST' })
};

const Estoque = {
    getAll: async () => {
        const r = await apiRequest('/estoque');
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getBaixo: async () => {
        const r = await apiRequest('/estoque/baixo');
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    save: async (data) => {
        const r = await apiRequest('/estoque', { method: 'POST', body: JSON.stringify(data) });
        return r?.data;
    },
    entrada: (id, qtd) => apiRequest(`/estoque/${id}/entrada?quantidade=${qtd}`, { method: 'POST' }),
    saida: (id, qtd) => apiRequest(`/estoque/${id}/saida?quantidade=${qtd}`, { method: 'POST' })
};

const Dashboard = {
    getDados: async () => {
        const r = await apiRequest('/dashboard');
        return r?.data || r;
    }
};

const Historico = {
    getByColaborador: async (id) => {
        const r = await apiRequest(`/historico/colaborador/${id}`);
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    },
    getExamesVencidos: async () => {
        const r = await apiRequest('/historico/exames-vencidos');
        if (r?.data?.content) return r.data.content;
        if (r?.content) return r.content;
        if (Array.isArray(r?.data)) return r.data;
        if (Array.isArray(r)) return r;
        return [];
    }
};

const Alertas = {
    getAll: async () => {
        const r = await apiRequest('/alertas');
        return r?.data || r;
    }
};

const Notificacoes = {
    emitter: null,
    ouvintes: [],
    
    conectar: async () => {
        const token = getToken();
        if (!token) return;
        
        try {
            Notificacoes.emitter = new EventSource('/api/notificacoes/stream', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            Notificacoes.emitter.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    Notificacoes.ouvintes.forEach(fn => fn(data));
                } catch (e) {}
            };
            
            Notificacoes.emitter.addEventListener('ESTOQUE_BAIXO', (event) => {
                try {
                    const data = JSON.parse(event.data);
                    Notificacoes.ouvintes.forEach(fn => fn({ tipo: 'ESTOQUE_BAIXO', dados: data }));
                    Notificacoes.mostrarToastEstoqueBaixo(data);
                } catch (e) {
                    console.error('[Notificacoes] Erro ao processar:', e);
                }
            });

            Notificacoes.emitter.addEventListener('VENCIMENTO_PROXIMO', (event) => {
                try {
                    const data = JSON.parse(event.data);
                    Notificacoes.ouvintes.forEach(fn => fn({ tipo: 'VENCIMENTO_PROXIMO', dados: data }));
                    Notificacoes.mostrarToastVencimento(data, false);
                } catch (e) {
                    console.error('[Notificacoes] Erro ao processar:', e);
                }
            });

            Notificacoes.emitter.addEventListener('MEDICAMENTO_VENCIDO', (event) => {
                try {
                    const data = JSON.parse(event.data);
                    Notificacoes.ouvintes.forEach(fn => fn({ tipo: 'MEDICAMENTO_VENCIDO', dados: data }));
                    Notificacoes.mostrarToastVencimento(data, true);
                } catch (e) {
                    console.error('[Notificacoes] Erro ao processar:', e);
                }
            });
            
            Notificacoes.emitter.onerror = (err) => {
                console.error('[Notificacoes] Erro SSE:', err);
                Notificacoes.desconectar();
                setTimeout(() => Notificacoes.conectar(), 5000);
            };
        } catch (err) {
            console.error('[Notificacoes] Erro ao conectar:', err);
        }
    },
    
    desconectar: () => {
        if (Notificacoes.emitter) {
            Notificacoes.emitter.close();
            Notificacoes.emitter = null;
        }
    },
    
    adicionarOuvinte: (fn) => {
        Notificacoes.ouvintes.push(fn);
    },
    
    getStatus: async () => {
        const r = await apiRequest('/notificacoes/estoque-baixo');
        return r;
    },
    
    verificar: async () => {
        const r = await apiRequest('/notificacoes/verificar');
        return r;
    },
    
    mostrarToastEstoqueBaixo: (itens) => {
        if (!itens || itens.length === 0) return;
        
        const container = document.createElement('div');
        container.className = 'notification-toast-container';
        container.innerHTML = `
            <div class="notification-toast notification-toast-warning">
                <div class="notification-toast-header">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                    <span>Estoque Baixo</span>
                </div>
                <div class="notification-toast-body">
                    ${itens.map(i => `<div>• ${i.nome}: ${i.quantidadeAtual}/${i.quantidadeMinima} ${i.unidade}</div>`).join('')}
                </div>
            </div>
        `;
        document.body.appendChild(container);
        setTimeout(() => container.remove(), 10000);
    },
    
    mostrarToastVencimento: (itens, isVencido) => {
        if (!itens || itens.length === 0) return;
        
        const typeClass = isVencido ? 'notification-toast-danger' : 'notification-toast-warning';
        const title = isVencido ? 'Medicamentos Vencidos' : 'Vencimento Próximo';
        const colorClass = isVencido ? 'text-red-600' : 'text-amber-500';
        
        const container = document.createElement('div');
        container.className = 'notification-toast-container';
        // Ajuste de z-index ou offset se já existir outro toast
        container.style.bottom = document.querySelectorAll('.notification-toast-container').length * 100 + 20 + 'px';
        
        container.innerHTML = `
            <div class="notification-toast ${typeClass}">
                <div class="notification-toast-header">
                    <svg class="w-5 h-5 ${colorClass}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                    </svg>
                    <span>${title}</span>
                </div>
                <div class="notification-toast-body">
                    ${itens.map(i => `<div>• ${i.nome} (Lote: ${i.lote}) - Validade: ${i.dataValidade.split('T')[0]}</div>`).join('')}
                </div>
            </div>
        `;
        document.body.appendChild(container);
        setTimeout(() => container.remove(), 10000);
    }
};

// ─── UI Helpers ───────────────────────────────────────────────────────────────

function showModal(id) {
    document.getElementById(id).classList.add('active');
}

function hideModal(id) {
    document.getElementById(id).classList.remove('active');
}

function showMessage(msg, type = 'success') {
    const div = document.createElement('div');
    div.className = `alert alert-${type}`;
    div.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;animation:slideIn 0.3s ease-out';
    div.textContent = msg;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('pt-BR');
}

function formatDateTime(date) {
    return new Date(date).toLocaleString('pt-BR');
}

function showAlert(message, type = 'success') {
    const el = document.createElement('div');
    el.className = `alert alert-${type}`;
    el.textContent = message;
    document.body.insertBefore(el, document.body.firstChild);
    setTimeout(() => el.remove(), 3000);
}

// ─── Guard de Autenticação ────────────────────────────────────────────────────
function checkAuth() {
    const publicPaths = ['/login', '/index.html', '/'];
    const currentPath = window.location.pathname;
    const savedToken = localStorage.getItem('token');

    console.debug(`[Auth] checkAuth | path="${currentPath}" | token=${savedToken ? '"' + savedToken.substring(0, 25) + '..."' : 'NULO'}`);

    if (!savedToken && !publicPaths.includes(currentPath)) {
        console.warn('[Auth] ⚠ Token ausente — redirecionando para /login');
        window.location.replace('/login');
        return false;
    }

    // Sincroniza a variável de módulo com o valor atual do localStorage
    token = savedToken;
    return true;
}

// Executa imediatamente ao carregar app.js (bloqueia render de páginas sem token)
(function () {
    checkAuth();

    // Exibe o botão/link "Usuários" na sidebar apenas para perfil ADMINISTRADOR
    try {
        var t = localStorage.getItem('token');
        if (t) {
            var payload = JSON.parse(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
            var role = payload.role || payload.perfil || '';
            if (role === 'ADMINISTRADOR') {
                var btn = document.getElementById('btn-usuarios');
                if (btn) btn.style.display = '';
            }
        }
    } catch (e) { /* token inválido */ }
})();