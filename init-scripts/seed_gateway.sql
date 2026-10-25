
INSERT INTO usuario (
    ativo,
    bloqueado,
    data_expiracao_senha,
    primeiro_acesso,
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
    FALSE,
    0,
    '2026-05-24 09:45:23.285864',
    'COD' || n,
    'usuario' || n || '@teste.com',
    'usuario' || n,
    'Usuario ' || n,
    'ADMIN',
    '$2a$10$wtCOyqTrpkkiT1ITi3ukfeykkvL7LbnLBkOIgYwmJ8FWnbFEJMNFG'
FROM generate_series(1, 200) AS n;
