ALTER TABLE acidentes_trabalho ADD COLUMN IF NOT EXISTS cnpj_empresa VARCHAR(255);
ALTER TABLE acidentes_trabalho ADD COLUMN IF NOT EXISTS cid VARCHAR(255);
ALTER TABLE acidentes_trabalho ADD COLUMN IF NOT EXISTS parte_corpo_atingida VARCHAR(255);
ALTER TABLE acidentes_trabalho ADD COLUMN IF NOT EXISTS dias_afastados INT;
