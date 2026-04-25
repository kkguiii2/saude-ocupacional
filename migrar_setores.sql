-- Script de migração de Setores
-- Execute este script no banco de dados PostgreSQL para atualizar os colaboradores
-- cadastrados com os setores antigos para os novos valores.

-- Mapeamento sugerido (ajuste conforme necessário):
-- PRODUCAO       → EXTRUSAO        (produção → extrusão)
-- MANUTENCAO     → MANUTENCAO_INDUSTRIAL
-- LOGISTICA      → MATERIAIS
-- ADMINISTRATIVO → COMPRAS
-- QUALIDADE      → CQ_EXTRUSAO
-- EXPEDICAO      → EXTRUSAO
-- ALMOXARIFADO   → MATERIAIS

UPDATE colaboradores SET setor = 'EXTRUSAO'              WHERE setor = 'PRODUCAO';
UPDATE colaboradores SET setor = 'MANUTENCAO_INDUSTRIAL' WHERE setor = 'MANUTENCAO';
UPDATE colaboradores SET setor = 'MATERIAIS'             WHERE setor = 'LOGISTICA';
UPDATE colaboradores SET setor = 'COMPRAS'               WHERE setor = 'ADMINISTRATIVO';
UPDATE colaboradores SET setor = 'CQ_EXTRUSAO'           WHERE setor = 'QUALIDADE';
UPDATE colaboradores SET setor = 'EXTRUSAO'              WHERE setor = 'EXPEDICAO';
UPDATE colaboradores SET setor = 'MATERIAIS'             WHERE setor = 'ALMOXARIFADO';
