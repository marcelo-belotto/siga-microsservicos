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

-- Usuários fixos (9 no total)
INSERT INTO usuario (codigo, nome_completo, email, login, senha, role, data_cadastro,
                      data_expiracao_senha, ativo, tentativas_login, bloqueado, primeiro_acesso)
SELECT * FROM (VALUES
    ('USR00001', 'Carlos Henrique Almeida', 'carlos.almeida@horizonte.edu.br', 'admin.seed', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'ADMIN', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00002', 'Marcos Vieira Souza', 'marcos.souza@horizonte.edu.br', 'admin2', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'ADMIN', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00003', 'Renata Oliveira', 'renata.oliveira@horizonte.edu.br', 'admin3', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'ADMIN', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00004', 'Administrador Sistema', 'admin@horizonte.edu.br', 'sysadmin', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'ADMIN', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00005', 'Juliana Araújo Lima', 'juliana.lima@horizonte.edu.br', 'coord1', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'COORDENADOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00006', 'Ricardo Santos Moura', 'ricardo.moura@horizonte.edu.br', 'coord2', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'COORDENADOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00007', 'Sandra Machado Silva', 'sandra.silva@horizonte.edu.br', 'oper1', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'OPERADOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00008', 'Paulo Roberto Cunha', 'paulo.cunha@horizonte.edu.br', 'oper2', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'OPERADOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true),
    ('USR00009', 'Tatiana Fonseca', 'tatiana.fonseca@horizonte.edu.br', 'oper3', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'OPERADOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true)
) AS v(codigo, nome_completo, email, login, senha, role, data_cadastro,
       data_expiracao_senha, ativo, tentativas_login, bloqueado, primeiro_acesso)
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login IN (
'admin.seed','admin2','admin3','sysadmin','coord1','coord2','oper1','oper2','oper3'
));

-- Usuários vinculados a professores (15 no total)
INSERT INTO usuario (codigo, nome_completo, email, login, senha, role, data_cadastro,
                      data_expiracao_senha, ativo, tentativas_login, bloqueado, primeiro_acesso,
                      professor_codigo_funcional)
SELECT * FROM (VALUES
    ('USR00010', 'Ana Silva Oliveira', 'ana.oliveira@horizonte.edu.br', 'prof1', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000001'),
    ('USR00011', 'Bruno Costa Lima', 'bruno.lima@horizonte.edu.br', 'prof2', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000002'),
    ('USR00012', 'Carla Mendes Souza', 'carla.souza@horizonte.edu.br', 'prof3', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000003'),
    ('USR00013', 'Daniel Ferreira Santos', 'daniel.santos@horizonte.edu.br', 'prof4', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000004'),
    ('USR00014', 'Elena Rodrigues Alves', 'elena.alves@horizonte.edu.br', 'prof5', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000005'),
    ('USR00015', 'Fernando Gomes Pereira', 'fernando.pereira@horizonte.edu.br', 'prof6', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000006'),
    ('USR00016', 'Gabriela Martins Costa', 'gabriela.costa@horizonte.edu.br', 'prof7', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000007'),
    ('USR00017', 'Henrique Almeida Dias', 'henrique.dias@horizonte.edu.br', 'prof8', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000008'),
    ('USR00018', 'Isabela Nascimento', 'isabela.nasc@horizonte.edu.br', 'prof9', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000009'),
    ('USR00019', 'José Carlos Barbosa', 'jose.barbosa@horizonte.edu.br', 'prof10', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000010'),
    ('USR00020', 'Karen Lopes Vieira', 'karen.vieira@horizonte.edu.br', 'prof11', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000011'),
    ('USR00021', 'Lucas Ramos Freitas', 'lucas.freitas@horizonte.edu.br', 'prof12', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000012'),
    ('USR00022', 'Maria Helena Cardoso', 'maria.cardoso@horizonte.edu.br', 'prof13', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000013'),
    ('USR00023', 'Nelson Teixeira Pinto', 'nelson.pinto@horizonte.edu.br', 'prof14', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000014'),
    ('USR00024', 'Patricia Rocha Melo', 'patricia.melo@horizonte.edu.br', 'prof15', '$2b$10$yFQ4AcxbIjItdT7KDQy8zeynl586.Tq1Ufr2V9XHJV2PnkDsEenDa', 'PROFESSOR', now(), (CURRENT_DATE + INTERVAL '1 day'), true, 0, false, true, 'RF000015')
) AS v(codigo, nome_completo, email, login, senha, role, data_cadastro,
       data_expiracao_senha, ativo, tentativas_login, bloqueado, primeiro_acesso,
       professor_codigo_funcional)
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login LIKE 'prof%');
