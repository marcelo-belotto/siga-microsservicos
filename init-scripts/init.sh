#!/bin/bash
docker exec -i siga-postgres psql -U siga -d gatewaydb < ./seed_gateway.sql
docker exec -i siga-postgres psql -U siga -d alunodb < ./seed_aluno.sql
docker exec -i siga-postgres psql -U siga -d academicodb < ./seed_academico.sql
docker exec -i siga-postgres psql -U siga -d matriculadb < ./seed_matricula.sql
#docker exec -i siga-postgres psql -U siga -d avaliacaodb < ./seed_avaliacao.sql
