-- Usuário ADMIN padrão para primeiro acesso.
-- Login: admin
-- Senha: Siga2025@ (mesma senha padrão do sistema original; o hash abaixo é BCrypt).
-- Idempotente: só insere se ainda não existir um usuário com este login,
-- para não falhar em reinicializações subsequentes da aplicação.
INSERT INTO usuario (codigo, nome_completo, email, login, senha, role, data_cadastro,
                      data_expiracao_senha, ativo, tentativas_login, bloqueado, primeiro_acesso)
SELECT 'ADM001', 'Administrador do Sistema', 'admin@horizontedosaber.local', 'admin',
       '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa',
       'ADMIN', now(), NULL, true, 0, false, true
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login = 'admin');
