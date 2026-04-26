CREATE TABLE IF NOT EXISTS atendimento_medicamento (
    id BIGSERIAL PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT fk_atendimento_med_atend FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id),
    CONSTRAINT fk_atendimento_med_med FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id)
);