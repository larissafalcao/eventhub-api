# Event Hub API

API REST para gerenciamento de eventos, participantes e compra de ingressos.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Cache
- Spring Data Redis
- Flyway
- PostgreSQL
- Redis
- Spring Validation
- Springdoc OpenAPI (Swagger)
- Maven
- Docker
- Docker Compose

## Como rodar o projeto

### 1) Pre-requisitos

- Java 21 instalado
- Maven 3.9+
- Docker e Docker Compose

### 2) Subir toda a stack com Docker

No diretório raiz do projeto:

1. Crie seu arquivo de ambiente a partir do exemplo:

```bash
cp .env.example .env
```

2. Preencha as credenciais no arquivo `.env`

3. Suba os containers:

```bash
docker compose up --build
```

Isso sobe:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- PostgreSQL: `localhost:15432`
- Redis: `localhost:6379`

### 3) Rodar a aplicação localmente

Se preferir rodar a API fora do Docker, suba primeiro as dependências:

```bash
docker compose up -d postgres-eventhub redis-eventhub
```

Depois exporte as variáveis de ambiente necessárias e execute a aplicação com Maven:

```bash
export DB_URL=jdbc:postgresql://localhost:15432/seu-banco
export POSTGRES_USER=seu-usuario
export POSTGRES_PASSWORD=sua-senha
```

```bash
mvn spring-boot:run
```

Ou, se preferir reaproveitar as credenciais do `.env`:

```bash
export POSTGRES_DB=seu-banco
export POSTGRES_USER=seu-usuario
export POSTGRES_PASSWORD=sua-senha
export DB_URL=jdbc:postgresql://localhost:15432/${POSTGRES_DB}
mvn spring-boot:run
```

A aplicação sobe, por padrão, em:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 4) Variáveis de ambiente

Credenciais e parâmetros sensíveis não ficam hardcoded no projeto. Para rodar com Docker Compose, defina pelo menos:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

Variáveis suportadas:

- `DB_URL` (default: `jdbc:postgresql://localhost:15432/eventhub-api-db`)
- `DB_DRIVER_CLASS_NAME` (default: `org.postgresql.Driver`)
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT` (default: `15432`)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `APP_PORT` (default: `8080`)
- `CACHE_NAMES` (default: `events`)
- `CACHE_TTL` (default: `10m`)
- `LOG_LEVEL_ROOT` (default: `INFO`)
- `LOG_CONSOLE_FORMAT` (default: `logstash`)
- `SHUTDOWN_TIMEOUT_PER_PHASE` (default: `30s`)

### 5) Cache de eventos

A listagem de eventos (`GET /events`) utiliza cache Redis.

- Cache configurado: `events`
- TTL padrão: `10m`
- Invalidação automática em criação, atualização e remoção de eventos

## Como rodar os testes

```bash
mvn test
```

## Decisões técnicas

### 1) PostgreSQL como banco relacional

O domínio envolve entidades relacionais com integridade forte (`events`, `participants`, `tickets`) e regras de consistencia, como:

- chave estrangeira entre ingressos, eventos e participantes;
- restrição de unicidade para e-mail de participante;
- indices para consultas por evento e participante.

PostgreSQL foi escolhido pela robustez, suporte a constraints e bom desempenho para esse tipo de modelo.

### 2) Redis para cache da listagem de eventos

Redis foi adotado para a listagem de eventos porque:

- reduz leituras repetidas no banco em consultas paginadas;
- funciona bem em ambiente containerizado;
- permite TTL configurável e invalidação simples do cache.

### 3) Flyway para versionamento de schema

As mudanças de banco são aplicadas por migrações versionadas, garantindo:

- rastreabilidade de alterações;
- inicialização automática do schema ao subir a aplicação.

### 4) Spring Data JPA

Foi adotado para reduzir boilerplate no acesso ao banco e acelerar implementação de operações CRUD, mantendo o código de persistência simples e consistente com o ecossistema Spring.

### 5) Jakarta Bean Validation

As validações de entrada são declarativas nos DTOs e executadas automaticamente nos endpoints com `@Valid`, garantindo:

- erro rápido para payload inválido;
- regras centralizadas e reutilizáveis;
- respostas de erro padronizadas.

### 6) Swagger/OpenAPI com Springdoc

A documentação da API foi integrada via Springdoc para:

- facilitar exploração e testes manuais dos endpoints;
- expor contrato OpenAPI para integrações;
