
    create table if not exists acidentes_trabalho (
        ativo boolean not null,
        cat_emitida boolean,
        colaborador_id bigint not null,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        data_cat timestamp(6),
        data_hora timestamp(6) not null,
        id bigserial not null,
        registrado_por bigint,
        version bigint,
        causa TEXT,
        descricao TEXT,
        local_fabrica varchar(255) not null,
        medidas_tomadas TEXT,
        numero_cat varchar(255),
        testemunhas varchar(255),
        tipo varchar(255) not null check (tipo in ('CORTE','QUEIMA','QUEDA','ATROPELAMENTO','INTOXICACAO','ESMAGAMENTO','CHOQUE_ELETRICO','QUEDA_OBJETO','OUTROS')),
        primary key (id)
    );

    create table if not exists agendamentos (
        ativo boolean not null,
        agendado_por bigint,
        colaborador_id bigint not null,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        data_hora timestamp(6) not null,
        id bigserial not null,
        version bigint,
        observacoes varchar(255),
        status varchar(255) check (status in ('AGENDADO','REALIZADO','CANCELADO','FALTOU')),
        tipo varchar(255) not null check (tipo in ('ADMISSIONAL','PERIODICO','DEMISIONAL','RETORNO_AFASTAMENTO','MUDANCA_FUNCAO','EXAME_CLINICO','AUDIOMETRIA','ACUIDADE_VISUAL','ESPIROMETRIA','ECG')),
        primary key (id)
    );

    create table if not exists atendimentos (
        ativo boolean not null,
        emergencia boolean not null,
        atendente_id bigint not null,
        colaborador_id bigint not null,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        data_hora timestamp(6) not null,
        id bigserial not null,
        version bigint,
        conduta TEXT,
        encaminhamento varchar(255) check (encaminhamento in ('RETORNO_TRABALHO','AFASTAMENTO','ENCAMINHAMENTO_HOSPITAL','REDE_DESENVOLVEDORA','ACOMPANHAMENTO_AMBULATORIO')),
        gravidade varchar(255) not null check (gravidade in ('LEVE','MODERADA','GRAVE','CRITICA')),
        sintomas TEXT,
        tipo varchar(255) not null check (tipo in ('CONSULTA_ROTINA','EMERGENCIA','ACIDENTE_TRABALHO','RETORNO_TRABALHO','EXAME_PERIODICO','AVALIACAO_CLINICA')),
        primary key (id)
    );

    create table if not exists auditoria_logs (
        data_hora timestamp(6) not null,
        id bigserial not null,
        usuario_id bigint,
        acao varchar(255) not null,
        descricao varchar(255) not null,
        detalhes TEXT,
        ip varchar(255),
        modulo varchar(255),
        user_agent varchar(255),
        primary key (id)
    );

    create table if not exists colaboradores (
        ativo boolean not null,
        data_admissao date,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        id bigserial not null,
        version bigint,
        cargo varchar(255) not null,
        contato_emergencia varchar(255),
        epis_obrigatorios varchar(255),
        matricula varchar(255) not null unique,
        nome_completo varchar(255) not null,
        nome_contato_emergencia varchar(255),
        setor varchar(255) not null check (setor in ('CQ_EXTRUSAO','MATERIAIS','TI_SUPORTE','COMPRAS','FISCAL','RH','MANUTENCAO_INDUSTRIAL','EXTRUSAO')),
        status_funcionario varchar(255) check (status_funcionario in ('ATIVO','AFASTADO','EM_TRATAMENTO','DEMITIDO')),
        telefone_contato varchar(255),
        tipo_risco varchar(255) not null check (tipo_risco in ('BAIXO','MEDIO','ALTO')),
        primary key (id)
    );

    create table if not exists documentos (
        assinatura_valida boolean,
        ativo boolean not null,
        colaborador_id bigint,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        data_emissao timestamp(6),
        data_validade timestamp(6),
        emitido_por bigint,
        id bigserial not null,
        version bigint,
        conteudo TEXT,
        hash_documento varchar(255),
        numero_documento varchar(255),
        tipo varchar(255) not null check (tipo in ('ATESTADO','DECLARACAO_COMPARECIMENTO','CAT','ENCAMINHAMENTO_MEDICO','RELATORIO_EXAME','PRONTUARIO','LAUDO_MEDICO')),
        primary key (id)
    );

    create table if not exists medicamentos (
        ativo boolean not null,
        quantidade_estoque integer not null,
        quantidade_minima integer,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        data_validade timestamp(6),
        id bigserial not null,
        version bigint,
        categoria varchar(255) check (categoria in ('ANALGESICO','ANTIINFLAMATORIO','ANTIBIOTICO','ANTIALERGICO','CURATIVO','SOLUCAO','MATERIAL_DESCARTAVEL')),
        lote varchar(255),
        nome varchar(255) not null,
        principio_ativo varchar(255),
        unidade varchar(255),
        primary key (id)
    );

    create table if not exists movimentacoes_estoque (
        quantidade integer not null,
        data_hora timestamp(6) not null,
        id bigserial not null,
        medicamento_id bigint not null,
        responsavel_id bigint,
        motivo varchar(255),
        tipo varchar(255) not null check (tipo in ('ENTRADA','SAIDA','AJUSTE_ENTRADA','AJUSTE_SAIDA','VENCIMENTO','DEVOLUCAO')),
        primary key (id)
    );

    create table if not exists prontuarios_ocupacionais (
        ativo boolean not null,
        calor boolean,
        cargas boolean,
        machines boolean,
        risco_quimico boolean,
        ruido boolean,
        colaborador_id bigint not null unique,
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        id bigserial not null,
        proximo_exame timestamp(6),
        ultimo_exame timestamp(6),
        version bigint,
        alergias TEXT,
        historico_cirurgias TEXT,
        historico_doencas TEXT,
        medicacoes_uso TEXT,
        observacoes_gerais TEXT,
        restricoes_trabalho TEXT,
        riscos_exposicao TEXT,
        primary key (id)
    );

    create table if not exists usuarios (
        ativo boolean not null,
        tentativas_login integer,
        bloqueado_ate timestamp(6),
        data_atualizacao timestamp(6),
        data_cadastro timestamp(6) not null,
        id bigserial not null,
        ultimo_acesso timestamp(6),
        version bigint,
        nome varchar(255) not null,
        password varchar(255) not null,
        perfil varchar(255) not null check (perfil in ('MEDICO_TRABALHO','ENFERMEIRO','RH','SEGURANCA_TRABALHO','ADMINISTRADOR')),
        username varchar(255) not null unique,
        primary key (id)
    );

    create index idx_acidente_colaborador 
       on acidentes_trabalho (colaborador_id);

    create index idx_acidente_data 
       on acidentes_trabalho (data_hora);

    create index idx_acidente_tipo 
       on acidentes_trabalho (tipo);

    create index idx_acidente_cat 
       on acidentes_trabalho (cat_emitida);

    create index idx_agendamento_colaborador 
       on agendamentos (colaborador_id);

    create index idx_agendamento_data 
       on agendamentos (data_hora);

    create index idx_agendamento_status 
       on agendamentos (status);

    create index idx_agendamento_tipo 
       on agendamentos (tipo);

    create index idx_atendimento_colaborador 
       on atendimentos (colaborador_id);

    create index idx_atendimento_atendente 
       on atendimentos (atendente_id);

    create index idx_atendimento_data 
       on atendimentos (data_hora);

    create index idx_atendimento_tipo 
       on atendimentos (tipo);

    create index idx_atendimento_emergencia 
       on atendimentos (emergencia);

    create index idx_auditoria_usuario 
       on auditoria_logs (usuario_id);

    create index idx_auditoria_data 
       on auditoria_logs (data_hora);

    create index idx_auditoria_acao 
       on auditoria_logs (acao);

    create index idx_auditoria_modulo 
       on auditoria_logs (modulo);

    create index idx_colaborador_setor 
       on colaboradores (setor);

    create index idx_colaborador_status 
       on colaboradores (status_funcionario);

    create index idx_colaborador_ativo 
       on colaboradores (ativo);

    create index idx_colaborador_risco 
       on colaboradores (tipo_risco);

    create index idx_documento_colaborador 
       on documentos (colaborador_id);

    create index idx_documento_tipo 
       on documentos (tipo);

    create index idx_documento_numero 
       on documentos (numero_documento);

    create index idx_medicamento_nome 
       on medicamentos (nome);

    create index idx_medicamento_categoria 
       on medicamentos (categoria);

    create index idx_medicamento_ativo 
       on medicamentos (ativo);

    create index idx_movimentacao_medicamento 
       on movimentacoes_estoque (medicamento_id);

    create index idx_movimentacao_data 
       on movimentacoes_estoque (data_hora);

    create index idx_movimentacao_tipo 
       on movimentacoes_estoque (tipo);

    create index idx_usuario_perfil 
       on usuarios (perfil);

    create index idx_usuario_ativo 
       on usuarios (ativo);

    alter table if exists acidentes_trabalho 
       add constraint FKk69ref9xl2uwtakujp8kb02tf 
       foreign key (colaborador_id) 
       references colaboradores;

    alter table if exists acidentes_trabalho 
       add constraint FKh1tgx4vcj13r7i77or0lfs653 
       foreign key (registrado_por) 
       references usuarios;

    alter table if exists agendamentos 
       add constraint FKgrklfx7ngeml62xxf8vc3934i 
       foreign key (agendado_por) 
       references usuarios;

    alter table if exists agendamentos 
       add constraint FK71hap887fs5vwd6embti3brkr 
       foreign key (colaborador_id) 
       references colaboradores;

    alter table if exists atendimentos 
       add constraint FKk9ip8vx1v0mly2ba7vjtjf284 
       foreign key (atendente_id) 
       references usuarios;

    alter table if exists atendimentos 
       add constraint FKb48wiledar3va9n7be1r1ufcy 
       foreign key (colaborador_id) 
       references colaboradores;

    alter table if exists auditoria_logs 
       add constraint FKiu6jmj9ajcg7ubwqiyumnew97 
       foreign key (usuario_id) 
       references usuarios;

    alter table if exists documentos 
       add constraint FKo5wnjvfr96nu8vshyshsxo1nu 
       foreign key (colaborador_id) 
       references colaboradores;

    alter table if exists documentos 
       add constraint FKp0kd10igs7iqe1pse87dwxeyh 
       foreign key (emitido_por) 
       references usuarios;

    alter table if exists movimentacoes_estoque 
       add constraint FKgbgisoplrbexnsx3lxegmn8gd 
       foreign key (medicamento_id) 
       references medicamentos;

    alter table if exists movimentacoes_estoque 
       add constraint FK104h4oj671knqco86w89hxdkn 
       foreign key (responsavel_id) 
       references usuarios;

    alter table if exists prontuarios_ocupacionais 
       add constraint FKsloyxnt2c026fjmy5pa0j4gum 
       foreign key (colaborador_id) 
       references colaboradores;
