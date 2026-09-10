# Horizonte do Saber — SIGA (Arquitetura de Microsserviços)

Sistema de gestão escolar migrado de uma aplicação monolítica para uma arquitetura de microsserviços, orquestrada via Docker Compose.

## Arquitetura

| Serviço | Porta | Responsabilidade | Banco de dados |
|---|---|---|---|
| `api-gateway` | 8080 | Ponto único de entrada, roteamento, autenticação (login/JWT) | `gatewaydb` |
| `aluno-service` | 8081 | Cadastro de alunos | `alunodb` |
| `matricula-service` | 8082 | Matrículas e rematrículas | `matriculadb` |
| `academico-service` | 8083 | Turmas, professores, disciplinas, grade horária, ano letivo | `academicodb` |
| `avaliacao-service` | 8084 | Notas e frequência | `avaliacaodb` |

Todo o tráfego externo passa pelo **API Gateway** (`:8080`), que valida o token JWT e roteia a requisição para o microsserviço correspondente. Os serviços de negócio também revalidam o JWT ao receber a chamada (defesa em profundidade).

## Pré-requisitos

- [Docker Engine](https://docs.docker.com/engine/install/) 24+
- [Docker Compose plugin](https://docs.docker.com/compose/install/) (comando `docker compose`, sem hífen)

Verifique se está tudo certo:

```bash
docker --version
docker compose version
```

## Instalação

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd HorizonteDoSaber-API
```

### 2. Configure as variáveis de ambiente

O projeto usa um arquivo `.env` na raiz para armazenar credenciais de banco de dados e o segredo JWT — esse arquivo **não é versionado** (está no `.gitignore`).

Copie o template e ajuste os valores:

```bash
cp .env.example .env
```

Edite o `.env` com um editor de texto e preencha:

```dotenv
DB_USERNAME=siga
DB_PASSWORD=defina-uma-senha-forte

# Precisa ser o MESMO valor em todos os serviços — é usado para assinar e
# validar o token JWT entre o gateway e os microsserviços.
JWT_SECRET=gere-um-valor-longo-e-aleatorio

JWT_EXPIRATION=1800000
CORS_ALLOWED_ORIGINS=http://localhost:8080
```

Para gerar um `JWT_SECRET` forte:

```bash
openssl rand -base64 48
```

### 3. Suba os containers

Na raiz do projeto (onde está o `docker-compose.yml`):

```bash
docker compose up --build
```

A primeira execução demora alguns minutos — o Docker precisa baixar as imagens base e compilar os 5 serviços Java. Nas próximas vezes, sem alterações no código, `docker compose up` (sem `--build`) sobe muito mais rápido.

O compose sobe, nesta ordem:

1. **`postgres`** — um único container PostgreSQL com um banco de dados separado para cada serviço, criado e populado automaticamente pelos scripts em `docker/postgres-init/` no primeiro boot do volume.
2. **`api-gateway`** e os **4 microsserviços de negócio**, cada um aguardando o Postgres ficar saudável (`healthcheck`) antes de iniciar.

O `api-gateway` também popula, no boot, um usuário administrador padrão para o primeiro acesso ao sistema.

### 4. Verifique se subiu

```bash
docker compose ps
```

Todos os serviços devem aparecer com status `Up`/`running`.

## Primeiro acesso

Faça login para obter um token JWT:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","senha":"Siga2025@"}'
```

A resposta traz o token JWT. Use-o no cabeçalho `Authorization` para chamar qualquer rota da API através do gateway:

```bash
curl http://localhost:8080/api/turmas \
  -H "Authorization: Bearer <token-recebido-no-login>"
```

> **Importante:** troque a senha do usuário `admin` assim que possível, usando `POST /auth/trocar-senha`. A senha padrão é conhecida publicamente (está neste manual).

## Parar e reiniciar

```bash
# Para os containers, mantendo os dados do banco
docker compose down

# Sobe novamente
docker compose up

# Para os containers E apaga os dados do banco (recomeça do zero)
docker compose down -v
```

## Estrutura do repositório

```
HorizonteDoSaber-API/
├── docker-compose.yml              # Orquestração de todos os serviços
├── .env.example                    # Template de variáveis de ambiente
├── docker/
│   └── postgres-init/              # Scripts de criação dos bancos e DDLs
├── servicos-migrados/
│   ├── api-gateway/                # Roteamento + autenticação (porta 8080)
│   ├── aluno-service/              # Porta 8081
│   ├── matricula-service/          # Porta 8082
│   ├── academico-service/          # Porta 8083
│   └── avaliacao-service/          # Porta 8084
└── src/                            # Aplicação monolítica original (referência histórica)
```

## Rodando um serviço individualmente (fora do Docker)

Cada microsserviço também pode ser executado isoladamente, útil durante o desenvolvimento. Requer Java 21 e um PostgreSQL acessível.

```bash
cd servicos-migrados/<nome-do-servico>
./mvnw spring-boot:run
```

As variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GATEWAY_URL`) podem ser exportadas no shell ou colocadas em um arquivo `.env` dentro da própria pasta do serviço (não versionado).

## Solução de problemas

**`Permission denied` ao rodar `./mvnw`:**
Ocorre quando o arquivo perde a permissão de execução (comum após clonar/extrair em alguns ambientes). Corrija com:
```bash
chmod +x servicos-migrados/*/mvnw
```
(Os `Dockerfile`s já corrigem isso automaticamente durante o build, então esse problema só afeta execução local fora do Docker.)

**Erro de conexão com o banco ao subir um serviço individualmente:**
Confirme que o PostgreSQL está no ar e que o banco correspondente ao serviço já foi criado (veja a tabela de portas/bancos no início deste documento).

**Token JWT rejeitado por um serviço mas aceito por outro:**
Confirme que a variável `JWT_SECRET` é **idêntica** em todos os serviços — no `docker compose`, isso já é garantido pelo `.env` único na raiz.