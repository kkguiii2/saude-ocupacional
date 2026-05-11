<div align="center">
  
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Security-JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>

  <h1>🏥 Saúde Ocupacional - Ambulatório Médico Industrial</h1>
  
  <p>
    <strong>Sistema completo e seguro para gestão de saúde ocupacional, emissão de CATs e relatórios corporativos.</strong>
  </p>
</div>

<br/>

## 📖 Sobre o Projeto

O **Saúde Ocupacional** é um sistema projetado especificamente para ambulatórios médicos em ambientes industriais. Ele resolve o desafio da gestão de informações críticas de saúde do trabalhador, centralizando atendimentos, controle de estoque de medicamentos e emissão de documentos oficiais.

### 🎯 O Problema
Em muitas indústrias, o registro de acidentes de trabalho e os prontuários médicos são gerenciados por planilhas desconexas ou sistemas legados não integrados. Isso gera:
- Inconsistência nos dados e falhas em auditorias.
- Lentidão na emissão da CAT (Comunicação de Acidente de Trabalho), que possui prazos legais rigorosos.
- Falta de controle preciso sobre a dispensação de medicamentos.
- Interfaces defasadas que prejudicam a produtividade dos profissionais de saúde.

### 💡 A Solução
Desenvolvemos uma plataforma moderna, resiliente e auditável. O sistema padroniza o preenchimento de atendimentos, emite documentos profissionais formatados de acordo com os padrões legais (como formulários do INSS) e apresenta uma interface limpa e focada no usuário.

---

## 🚀 Principais Funcionalidades

- **Autenticação & Autorização:** Controle de acesso baseado em roles (Admin, Médico, Enfermeiro) com JWT (JSON Web Tokens). Conta de admin injetada automaticamente na inicialização.
- **Gestão de Atendimentos:** Registro ágil de ocorrências médicas, prescrição de medicamentos integrada ao estoque, com opção de "Nenhum" medicamento.
- **Emissão Oficial de CAT:** Geração automatizada de CAT em formato Excel (.xlsx) profissional, estruturada em grade e com formatação rigorosa idêntica ao documento do INSS.
- **Relatórios Corporativos:** Extração de relatórios gerenciais exportáveis para Excel com suporte completo a encoding UTF-8 (caracteres acentuados).
- **Dashboard Gerencial:** Visão geral rápida com métricas de saúde, navegação por sidebar responsiva (suporte total a dispositivos móveis e hamburger menu).
- **Auditoria Avançada:** Rastreamento completo de mudanças nas entidades utilizando o *Hibernate Envers*.

---

## 🛠 Tecnologias Utilizadas

### Backend
- **Java 17** & **Spring Boot 3.2.0**
- **Spring Data JPA** & **Hibernate Envers** (Auditoria de Banco de Dados)
- **Spring Security** & **JJWT** (Autenticação baseada em Token)
- **Apache POI** (Geração de planilhas Excel avançadas) & **OpenPDF**
- **Flyway** (Versionamento e migração de banco de dados)

### Frontend (UI/UX)
- **Thymeleaf** (Server-side rendering integrado)
- **HTML5, CSS3, JavaScript Vanilla**
- *Design System Customizado:* Interface premium com micro-interações, tokens de design padronizados, estados de hover dinâmicos e componentes responsivos.

### Infraestrutura & Dados
- **PostgreSQL** (Banco de dados de produção)
- **H2 Database** (Banco em memória para testes)
- **Docker** & **Docker Compose** (Orquestração de containers)

---

## 📂 Estrutura do Projeto

O projeto segue princípios de Arquitetura Limpa e MVC, organizado da seguinte forma:

```text
saude-ocupacional/
├── src/
│   ├── main/
│   │   ├── java/com/industrial/saudeocupacional/
│   │   │   ├── config/      # Configurações globais, CORS e Spring Security
│   │   │   ├── controllers/ # Endpoints REST e rotas de visualização (Views)
│   │   │   ├── dto/         # Objetos de Transferência de Dados
│   │   │   ├── entities/    # Modelos JPA / Regras de Negócio
│   │   │   ├── repositories/# Interfaces de acesso a dados (Spring Data JPA)
│   │   │   ├── security/    # Filtros e lógica JWT
│   │   │   └── services/    # Lógica de negócio isolada (Ex: RelatorioService)
│   │   └── resources/
│   │       ├── db/migration/# Scripts SQL Flyway
│   │       ├── static/      # Assets estáticos (CSS Global, JS, Imagens, Favicon)
│   │       ├── templates/   # Telas Thymeleaf (Dashboard, Relatórios, etc.)
│   │       └── application.yml
├── docker-compose.yml       # Orquestração do banco e app
├── Dockerfile               # Construção da imagem Docker
└── pom.xml                  # Dependências do Maven
```

---

## 🔌 Endpoints da API (Visão Geral)

Embora o sistema possua telas renderizadas (Thymeleaf), a lógica é exposta de maneira padronizada:

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/auth/login` | Autenticação do usuário e geração de token JWT. |
| `GET`  | `/atendimentos` | Retorna a view e os dados da lista de atendimentos. |
| `POST` | `/api/atendimentos`| Registra um novo atendimento no sistema. |
| `GET`  | `/relatorios/cat/export`| Exporta a CAT de um atendimento em `.xlsx` formatado. |

> **Nota de Design de API:** Os controllers estão divididos logicamente. Os dados que entram via `POST/PUT` são validados via anotações (`@Valid`) antes de atingir os Services.

---

## ⚙️ Como Rodar o Projeto

### Pré-requisitos
- **Java 17** ou superior instalado.
- **Maven** instalado.
- **Docker** e **Docker Compose** (Para o PostgreSQL).

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/kkguiii2/saude-ocupacional.git
   cd saude-ocupacional
   ```

2. **Inicie o Banco de Dados:**
   Usando o Docker Compose para subir a instância do PostgreSQL:
   ```bash
   docker-compose up -d db
   ```

3. **Configure as Variáveis de Ambiente:**
   Crie ou edite o arquivo `.env` na raiz do projeto contendo as credenciais de banco (se aplicável) ou deixe o Spring Boot assumir as configurações padrão do `application.yml`.

4. **Execute a Aplicação:**
   ```bash
   mvn spring-boot:run
   ```
   > Na primeira inicialização, o *Flyway* criará todas as tabelas e o sistema gerará automaticamente as credenciais iniciais do Administrador no banco de dados.

5. **Acesse no Navegador:**
   Acesse `http://localhost:8080`.

---

## 🧪 Testes e Confiabilidade (TDD)

O sistema foi desenvolvido focado na resiliência:
- **Testes Unitários:** O núcleo das regras de negócios (cálculo de estoque, lógicas de dispensa) é coberto por testes.
- **Testes de Integração:** O banco em memória `H2` é utilizado via `spring-boot-starter-test` para garantir que as transações e relacionamentos (JPA) funcionem como esperado antes de cada build.
- **Tratamento de Erros:** Exceções são interceptadas em nível global (`@ControllerAdvice`), garantindo respostas limpas para a UI e logs detalhados para o console (Debugging robusto).

---

## 🛡️ Segurança da Aplicação

Levamos a privacidade médica a sério. Práticas de segurança implementadas:
- **Zero Trust Local:** Nenhuma rota sensível é acessível sem um token JWT válido assinado (HS256).
- **Proteção contra ataques comuns:** Mitigação nativa do Spring Security contra CSRF e Headers de segurança (XSS protection).
- **Auditoria Ativa (Envers):** Qualquer modificação em tabelas cruciais (como Atendimentos) salva a versão anterior em uma tabela de log, permitindo investigar quem alterou o que, e quando.
- **Segredos Isolados:** Chaves JWT e senhas de banco não são "hardcoded" na aplicação, respeitando as boas práticas de configuração em 12 fatores.

---

## 📈 Melhorias Futuras (Roadmap)

- [ ] **Integração Assíncrona:** Implementar filas (RabbitMQ/Kafka) para o envio de e-mails da CAT aos supervisores para não travar a thread principal.
- [ ] **Cobertura E2E:** Adicionar suite de testes com Cypress ou Playwright para as telas do Thymeleaf.
- [ ] **Autenticação Multi-Fator (MFA):** Adicionar camada extra de segurança para médicos e enfermeiros.
- [ ] **Dashboard Analítico Avançado:** Inclusão de gráficos interativos com Chart.js diretamente renderizados pela API.

---

<div align="center">
  <p>Desenvolvido com dedicação para a saúde e segurança do trabalhador. 👷‍♂️👷‍♀️</p>
</div>
