CREATE TABLE IF NOT EXISTS revinfo (
    rev SERIAL PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE IF NOT EXISTS prontuarios_ocupacionais_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    colaborador_id BIGINT,
    historico_doencas TEXT,
    historico_cirurgias TEXT,
    alergias TEXT,
    medicacoes_uso TEXT,
    restricoes_trabalho TEXT,
    riscos_exposicao TEXT,
    ultimo_exame TIMESTAMP(6),
    proximo_exame TIMESTAMP(6),
    risco_quimico BOOLEAN,
    ruido BOOLEAN,
    calor BOOLEAN,
    machines BOOLEAN,
    cargas BOOLEAN,
    observacoes_gerais TEXT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_prontuario_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
