-- V8: Adiciona campo matricula na tabela usuarios
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS matricula VARCHAR(50);

-- Índice para busca rápida por matrícula
CREATE INDEX IF NOT EXISTS idx_usuario_matricula ON usuarios (matricula);
