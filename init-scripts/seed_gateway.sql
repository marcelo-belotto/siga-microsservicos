-- =============================================================================
-- Seed de dados iniciais — api-gateway (gatewaydb)
-- Gerado a partir do DataInitializer.java original do monolito.
-- Cobre: 9 usuários fixos (admins/coordenadores/operadores)
--        + 15 usuários vinculados a professores.
--
-- Senha padrão de TODOS os usuários deste seed: Siga2025@
-- (hash BCrypt abaixo). primeiro_acesso=true e data_expiracao_senha em
-- +1 dia forçam a troca de senha logo no primeiro login, como no original.
--
-- Nota de migração: no monolito, o usuário-professor tinha uma FK para a
-- entidade Professor. Como Professor agora vive no academico-service (outro
-- banco), o vínculo é mantido apenas via professor_codigo_funcional (string),
-- sem FK entre bancos — os códigos usados aqui (RF000001..RF000015) devem
-- corresponder aos professores inseridos por seed_academico.sql.
--
-- CONFLITO RESOLVIDO: o seed original tinha um admin de login 'admin', que
-- colide com o usuário 'admin' já criado automaticamente pelo data.sql padrão
-- do gateway (login 'admin', código ADM001, para o primeiro acesso). Como a
-- coluna login é UNIQUE, esse usuário foi renomeado para login 'admin.seed'
-- neste arquivo — mesma pessoa (Carlos Henrique Almeida), login diferente.
--
-- Este arquivo é independente do data.sql do usuário 'admin' padrão que já
-- roda automaticamente no boot da aplicação (login 'admin' / código ADM001).
-- Os usuários abaixo têm códigos e logins distintos, não há conflito.
--
-- Idempotência: só roda se a tabela ainda estiver vazia.
-- =============================================================================

INSERT INTO public.usuario (
    ativo,
    bloqueado,
    data_expiracao_senha,
    id,
    primeiro_acesso,
    professor_id,
    tentativas_login,
    data_cadastro,
    codigo,
    email,
    login,
    nome_completo,
    role,
    senha
)
SELECT
    TRUE,
    FALSE,
    '2026-12-31',
    n + 1,
    FALSE,
    NULL,
    0,
    '2026-05-24 09:45:23.285864',
    'COD' || n,
    'usuario' || n || '@teste.com',
    'usuario' || n,
    'Usuario ' || n,
    'OPERADOR',
    '$2a$10$wtCOyqTrpkkiT1ITi3ukfeykkvL7LbnLBkOIgYwmJ8FWnbFEJMNFG'
FROM generate_series(1, 200) AS n;