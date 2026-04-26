CREATE SEQUENCE IF NOT EXISTS revinfo_seq;

CREATE TABLE IF NOT EXISTS revinfo (
    rev INT PRIMARY KEY DEFAULT nextval('revinfo_seq'),
    revtstmp BIGINT
);