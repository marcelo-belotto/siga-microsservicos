#!/bin/bash
# Cria um banco de dados dedicado para cada microsserviço (Database-per-Service).
# Executado automaticamente pela imagem oficial do Postgres no primeiro boot
# do volume (arquivos em /docker-entrypoint-initdb.d/ rodam em ordem alfabética).
set -e

for DB in alunodb matriculadb academicodb avaliacaodb gatewaydb; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE $DB'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB')\gexec
EOSQL
done
