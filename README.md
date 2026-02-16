# Event Hub API

API REST para gerenciamento de eventos, participantes e compra de ingressos.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Flyway
- PostgreSQL
- Spring Validation
- Springdoc OpenAPI (Swagger)
- Maven

## Como rodar o projeto

### 1) Pre-requisitos

- Java 21 instalado
- Maven 3.9+ (ou uso do wrapper `./mvnw`)
- Docker e Docker Compose (opcional, mas recomendado para subir o banco)

### 2) Subir o banco PostgreSQL com Docker

No diretório raiz do projeto:

```bash
docker compose up -d
```

Isso sobe o PostgreSQL com:

- Database: `eventhub-api-db`
- User: `postgres`
- Password: `postgres`
- Porta local: `15432`

### 3) Rodar a aplicação

Use o Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Ou com Maven instalado:

```bash
mvn spring-boot:run
```

A aplicação sobe, por padrão, em:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 4) Variáveis de ambiente (opcional)

Caso queira sobrescrever os valores padrão:

- `DB_URL` (default: `jdbc:postgresql://localhost:15432/eventhub-api-db`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `DB_DRIVER_CLASS_NAME` (default: `org.postgresql.Driver`)
- `LOG_LEVEL_ROOT` (default: `INFO`)
- `LOG_CONSOLE_FORMAT` (default: `logstash`)
- `SHUTDOWN_TIMEOUT_PER_PHASE` (default: `30s`)

## Como rodar os testes

```bash
./mvnw test
```

## Decisões técnicas

### 1) PostgreSQL como banco relacional

O domínio envolve entidades relacionais com integridade forte (`events`, `participants`, `tickets`) e regras de consistencia, como:

- chave estrangeira entre ingressos, eventos e participantes;
- restrição de unicidade para e-mail de participante;
- indices para consultas por evento e participante.

PostgreSQL foi escolhido pela robustez, suporte a constraints e bom desempenho para esse tipo de modelo.

### 2) Flyway para versionamento de schema

As mudanças de banco são aplicadas por migrações versionadas, garantindo:

- rastreabilidade de alterações;
- inicialização automática do schema ao subir a aplicação.

### 3) Spring Data JPA

Foi adotado para reduzir boilerplate no acesso ao banco e acelerar implementação de operações CRUD, mantendo o código de persistência simples e consistente com o ecossistema Spring.

### 4) Jakarta Bean Validation

As validações de entrada são declarativas nos DTOs e executadas automaticamente nos endpoints com `@Valid`, garantindo:

- erro rápido para payload inválido;
- regras centralizadas e reutilizáveis;
- respostas de erro padronizadas.

### 5) Swagger/OpenAPI com Springdoc

A documentação da API foi integrada via Springdoc para:

- facilitar exploração e testes manuais dos endpoints;
- expor contrato OpenAPI para integrações;
