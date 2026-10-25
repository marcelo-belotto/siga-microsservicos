#!/bin/bash
# Carga do dataset de teste no primeiro boot do volume (mesmo mecanismo do monolito,
# que carrega backup_inserts.sql e script_notas_lotes.sql via docker-entrypoint-initdb.d).
# Substitui a execucao manual de init-scripts/init.sh.
# Roda depois de 01..06 (ordem alfabetica): bancos e tabelas ja existem.
set -euo pipefail
for par in gatewaydb:seed_gateway alunodb:seed_aluno academicodb:seed_academico \
           matriculadb:seed_matricula avaliacaodb:seed_avaliacao; do
  db="${par%%:*}"; arq="/seeds/${par#*:}.sql"
  echo "07-seeds: carregando $arq em $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" -f "$arq"
done
