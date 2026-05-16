# 🎨 PROMPT — Redesign Completo do Frontend (Saúde Ocupacional)

---

## 📌 SKILLS REQUERIDAS

Antes de escrever qualquer linha de código, ative e aplique **todas** as seguintes skills:

| Skill | Motivo de Uso |
|---|---|
| `core-components` | Criar um design system coeso com tokens, variáveis CSS e biblioteca de componentes reutilizáveis |
| `design-spells` | Injetar micro-interações, estados de hover, feedback visual e personalidade nas telas |
| `hig-patterns` | Aplicar padrões de interação e UX de alta qualidade: navegação, formulários, hierarquia visual |
| `form-cro` | Otimizar todos os formulários médicos para reduzir fricção e aumentar clareza operacional |
| `fixing-motion-performance` | Garantir que animações e transições usem apenas propriedades compositor-safe (transform, opacity) |
| `design-md` | Sintetizar o design system resultante em um DESIGN.md vivo para garantir consistência |
| `full-output-enforcement` | Nenhum arquivo pode ter placeholder, comentário "// resto do código aqui" ou trecho omitido |
| `moyu` | Não expandir escopo além do que foi pedido; não adicionar dependências externas não solicitadas |
| `code-refactoring-refactor-clean` | Refatorar o CSS/JS existente aplicando princípios SOLID e clean code ao frontend |
| `i18n-localization` | Garantir que todas as strings hardcoded sejam em PT-BR correto e consistente |

---

## 🏥 CONTEXTO DO PROJETO

Você está redesenhando o **frontend completo** de um sistema de saúde ocupacional para ambulatório médico industrial brasileiro. O sistema gerencia atendimentos médicos, estoque de medicamentos e emissão de CATs (Comunicação de Acidente de Trabalho).

### Stack — O QUE NÃO MUDA (restrições absolutas):

```
Backend:   Java 17 + Spring Boot 3.2.0
Templates: Thymeleaf (server-side rendering — NÃO converter para SPA)
Estilo:    HTML5 + CSS3 vanilla (sem frameworks CSS externos como Bootstrap/Tailwind)
Scripts:   JavaScript Vanilla ES6+ (sem React, Vue, Angular ou jQuery)
Segurança: Spring Security + JWT (não alterar rotas, controllers ou endpoints)
Banco:     PostgreSQL com Flyway (não alterar migrations)
```

### Stack — O QUE VOCÊ VAI REFAZER:

```
/src/main/resources/templates/   → Todos os arquivos .html Thymeleaf
/src/main/resources/static/css/  → Todo o CSS global e por componente
/src/main/resources/static/js/   → Todo o JavaScript de interação
```

---

## 🎯 OBJETIVO

Reescrever **100% do frontend** com design profissional de nível enterprise, mantendo todas as integrações Thymeleaf, rotas, atributos `th:*`, fragmentos (`th:fragment`, `th:replace`), e lógica de servidor intactos.

**O backend não deve ser tocado.** Nenhum Controller, Service, Entity ou endpoint pode ser alterado.

---

## 🖥️ INVENTÁRIO DE TELAS (redesign obrigatório de TODAS as 12)

> Estas são as telas reais confirmadas no projeto. Nenhuma pode ser omitida.

### 1. `__layout.html` — Layout Base (template pai)
- Arquivo mestre que todos os outros herdam via `th:replace` ou `th:insert`
- Contém: `<head>` com meta tags, link para CSS global, sidebar, header e slot de conteúdo (`th:fragment="content"`)
- Inclui o script JS global e o token CSRF
- **Design:** Definir aqui a estrutura do grid principal (sidebar + main content), importar os tokens CSS e a tipografia (Google Fonts CDN)
- **CRÍTICO:** Todos os `th:fragment` neste arquivo devem manter nomes idênticos ao original

### 2. `login.html` — Tela de Autenticação
- Formulário com campos: usuário e senha
- Integração com `POST /api/auth/login` via form Thymeleaf
- Exibição de mensagem de erro em caso de falha
- **Design:** Tela fullscreen com card centralizado; lado esquerdo com identidade visual (logotipo + tagline do ambulatório); lado direito com o formulário. Responsivo: uma coluna no mobile

### 3. `index.html` — Página Inicial / Redirect
- Pode ser uma landing page simples ou redirect automático para o dashboard
- Preservar qualquer lógica de redirecionamento do controller
- **Design:** Se for página própria, aplicar a identidade visual com mensagem de boas-vindas

### 4. `dashboard.html` — Painel de Controle Principal
- Sidebar de navegação responsiva com hamburger menu no mobile
- Cards de KPIs: total de atendimentos, acidentes do mês, itens em estoque crítico, agendamentos do dia
- Atalhos visuais para as seções principais
- **Design:** Grid de 3–4 cards no topo, seção de atividade recente abaixo; sidebar fixa no desktop, drawer deslizante no mobile

### 5. `atendimento.html` — Gestão de Atendimentos
- Tabela com lista de atendimentos registrados (data, colaborador, tipo, status)
- Botão de registrar novo atendimento (abre formulário ou navega para sub-rota)
- Ação por linha: ver detalhes, exportar CAT em .xlsx
- Formulário de registro: dados do trabalhador, tipo de ocorrência, prescrição (com opção "Nenhum")
- **Design:** Tabela com zebra-striping, badges de status coloridos; formulário em seções colapsáveis com feedback de validação inline

### 6. `acidentes.html` — Registro de Acidentes de Trabalho
- Formulário para registro de acidente (dados do acidente, local, causa, testemunhas)
- Listagem de acidentes registrados com status da CAT
- Botão de emissão/exportação de CAT em .xlsx
- **Design:** Formulário com campos agrupados por contexto (dados do acidente / dados do trabalhador / CAT); tabela com indicador visual de urgência

### 7. `agendamentos.html` — Gestão de Agendamentos
- Calendário ou lista de agendamentos médicos
- Formulário para criar/editar agendamento (colaborador, data, hora, tipo de consulta)
- Indicadores de status: confirmado, pendente, cancelado
- **Design:** Visão de lista ou grid de cards por data; badges de status com cores semânticas

### 8. `colaboradores.html` — Cadastro de Colaboradores
- Tabela com lista de colaboradores (nome, matrícula, setor, cargo)
- Formulário de cadastro/edição de colaborador
- Busca e filtros por setor ou status
- **Design:** Tabela com avatar/iniciais do nome, campo de busca proeminente, formulário em modal ou painel lateral

### 9. `estoque.html` — Controle de Estoque de Medicamentos
- Tabela de medicamentos com quantidade atual, quantidade mínima e status
- Formulário de entrada/saída de estoque (dispensação vinculada ao atendimento)
- Indicadores visuais: estoque crítico (vermelho), atenção (amarelo), normal (verde)
- **Design:** Tabela com badge de status de estoque em cada linha; painel de resumo no topo com total de itens críticos

### 10. `relatorios.html` — Relatórios Gerenciais
- Cards ou lista de relatórios disponíveis para exportação
- Filtros de período (data início / data fim)
- Botões de exportação para Excel (.xlsx) com estado de loading durante geração
- **Design:** Cards de relatório com ícone, descrição do conteúdo e botão de exportar; skeleton loader enquanto processa

### 11. `usuarios.html` — Gestão de Usuários do Sistema
- Tabela com usuários cadastrados (nome, e-mail, role: Admin / Médico / Enfermeiro)
- Formulário de criação/edição de usuário
- Toggle de status ativo/inativo por usuário
- **Design:** Tabela com badge de role colorido; confirmação visual antes de desativar um usuário

### 12. `error.html` — Página de Erro
- Exibição amigável de erros HTTP (403, 404, 500)
- Mensagem clara do problema e botão de voltar ao dashboard
- **Design:** Ilustração ou ícone grande, código do erro em destaque, call-to-action visível

---

## 🎨 DIREÇÃO DE DESIGN

### Identidade Visual

```
Paleta Primária:   Azul corporativo profundo (#1A3C5E ou equivalente)
Paleta Secundária: Verde saúde (#2E7D6B ou equivalente)
Neutros:           Tons de cinza quentes (não frios) para backgrounds
Alerta/Perigo:     Vermelho institucional (#C0392B)
Aviso:             Âmbar (#E67E22)
Sucesso:           Verde (#27AE60)

Tipografia:
  - Fonte principal: Inter, Roboto ou similar (Google Fonts, CDN)
  - Hierarquia clara: H1 (28px), H2 (22px), H3 (18px), body (14px/16px)
  - Line-height confortável para leitura de dados médicos: 1.6

Espaçamento:
  - Sistema de 8pt grid: 4px, 8px, 16px, 24px, 32px, 48px, 64px
  - Padding de cards: 24px
  - Gap entre seções: 32px
```

### Tokens CSS (criar em `:root` no CSS global)

```css
:root {
  /* Cores */
  --color-primary: #1A3C5E;
  --color-primary-light: #2563A8;
  --color-primary-surface: #EBF2FA;
  --color-secondary: #2E7D6B;
  --color-secondary-surface: #E8F5F2;
  --color-danger: #C0392B;
  --color-danger-surface: #FDEDEC;
  --color-warning: #E67E22;
  --color-warning-surface: #FEF9E7;
  --color-success: #27AE60;
  --color-success-surface: #EAFAF1;

  /* Neutros */
  --color-bg: #F5F6FA;
  --color-surface: #FFFFFF;
  --color-border: #E2E8F0;
  --color-text-primary: #1A202C;
  --color-text-secondary: #718096;
  --color-text-muted: #A0AEC0;

  /* Elevação */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.08);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.10);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.12);

  /* Espaçamento */
  --space-1: 4px; --space-2: 8px; --space-3: 12px;
  --space-4: 16px; --space-5: 20px; --space-6: 24px;
  --space-8: 32px; --space-10: 40px; --space-12: 48px;

  /* Border radius */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-full: 9999px;

  /* Transições (compositor-safe apenas) */
  --transition-fast: 150ms ease;
  --transition-base: 250ms ease;
}
```

### Componentes Obrigatórios (criar como classes CSS reutilizáveis)

```
.btn            → Botão base (variantes: .btn-primary, .btn-secondary, .btn-danger, .btn-ghost)
.card           → Card com sombra e border-radius (variante: .card--flat, .card--elevated)
.badge          → Etiqueta de status (variantes: .badge--success, .badge--danger, .badge--warning)
.form-group     → Wrapper de campo com label, input e mensagem de erro
.input          → Campo de texto estilizado (focus ring com var(--color-primary))
.table-wrapper  → Container responsivo para tabelas com scroll horizontal
.alert          → Mensagem de feedback (variantes: .alert--success, .alert--error, .alert--warning)
.sidebar        → Navegação lateral
.sidebar__item  → Item de menu (estado ativo: .sidebar__item--active)
.page-header    → Cabeçalho de página com título e ações
.loading-spinner→ Spinner de carregamento
```

---

## ⚙️ REGRAS TÉCNICAS OBRIGATÓRIAS

### Thymeleaf — O QUE PRESERVAR INTACTO

```html
<!-- Nunca remover ou renomear estes atributos -->
th:action, th:method, th:field, th:object, th:errors
th:text, th:utext, th:value, th:href, th:src
th:if, th:unless, th:each, th:switch, th:case
th:fragment, th:replace, th:insert, th:include
th:classappend, th:styleappend
th:attr, th:attrappend

<!-- CSRF token — NUNCA remover -->
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

<!-- Fragmentos — preservar nomes exatos -->
th:fragment="sidebar"
th:fragment="header"
th:fragment="alerts"
```

### JavaScript — Regras

```javascript
// ✅ Permitido
document.addEventListener('DOMContentLoaded', () => { ... });
fetch('/api/...', { method: 'POST', headers: {...}, body: JSON.stringify({...}) });
element.classList.toggle('active');
const { data } = await response.json();

// ❌ Proibido
jQuery / $ (não instalar)
import React from 'react' (não converter para SPA)
window.location não pode alterar rotas definidas no backend
XMLHttpRequest (usar fetch)
```

### CSS — Regras

```css
/* ✅ Usar */
transform: translateX(-100%); /* sidebar mobile */
opacity: 0 → 1; /* fade-in */
CSS Custom Properties (var(--token))
@media queries com breakpoints: 768px (tablet), 1024px (desktop)

/* ❌ Proibido */
animation em propriedades que causam layout (width, height, top, left)
!important exceto em reset/normalize
Seletores com especificidade > 0-3-0
```

---

## 📋 CRITÉRIOS DE ACEITAÇÃO (Definition of Done)

Para cada tela entregue, verificar:

- [ ] Renderiza corretamente com dados reais do Thymeleaf (sem erros de template)
- [ ] Todos os formulários submetem para os endpoints corretos sem erro 403/404/500
- [ ] Token CSRF presente em todos os forms com `th:method="post"`
- [ ] Responsivo: layout funcional em 375px (mobile), 768px (tablet), 1280px (desktop)
- [ ] Sidebar colapsa para drawer/hamburger no mobile
- [ ] Estados de loading visíveis nos botões de exportação (.xlsx)
- [ ] Mensagens de erro do servidor exibidas no formato visual correto (`.alert--error`)
- [ ] Nenhum `console.error` ou stack trace visível no frontend
- [ ] Contraste WCAG AA mínimo em todos os textos sobre fundos coloridos
- [ ] Nenhum arquivo tem código omitido, placeholder ou `// TODO` sem implementação

---

## 🚀 ORDEM DE EXECUÇÃO RECOMENDADA

```
FASE 1 — Fundação (fazer primeiro, o resto depende disso)
  1. base.css         → Reset, tokens CSS (:root), tipografia global
  2. components.css   → Biblioteca de componentes: .btn, .card, .badge, .table-wrapper, .alert, .form-group
  3. __layout.html    → Template pai: estrutura sidebar + main, imports CSS/JS, slot de conteúdo

FASE 2 — Autenticação e Início
  4. login.html       → Tela de entrada do sistema
  5. index.html       → Redirect ou landing inicial
  6. error.html       → Página de erros HTTP amigável

FASE 3 — Núcleo Operacional
  7. dashboard.html   → Painel com KPIs e navegação
  8. atendimento.html → Lista + formulário de atendimentos
  9. acidentes.html   → Registro de acidentes e emissão de CAT

FASE 4 — Módulos de Suporte
  10. agendamentos.html  → Gestão de agenda médica
  11. estoque.html       → Controle de medicamentos
  12. colaboradores.html → Cadastro de trabalhadores

FASE 5 — Administração e Fechamento
  13. relatorios.html  → Exportação de relatórios gerenciais
  14. usuarios.html    → Gestão de usuários e roles
  15. Revisão global   → Micro-interações, performance de animações, responsividade em todas as telas
```

---

## ⚠️ AVISOS FINAIS

> **NÃO** instale nenhuma dependência nova no `pom.xml`.
> **NÃO** altere nenhum arquivo `.java`, `.yml`, `.sql` ou `docker-compose.yml`.
> **NÃO** crie arquivos de rota, controller ou configuração.
> **NÃO** use CDN de frameworks CSS (Bootstrap, Tailwind, Bulma, etc.).
> **NÃO** entregue arquivos com trechos omitidos — aplique `full-output-enforcement`.
> **APLIQUE** `moyu` para evitar expansão de escopo não solicitada.

A entrega deve ser um frontend que um profissional de saúde usaria diariamente com confiança, velocidade e sem erros. Qualidade de produto, não de protótipo.
