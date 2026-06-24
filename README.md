# Farmácia Delivery — Microsserviços

Sistema de delivery de farmácias baseado em microsserviços, para a disciplina de Softwares Escaláveis (turma de segunda e quarta).

## Integrantes e divisão de responsabilidades

| Integrante | Microsserviço sob responsabilidade | Banco |
|---|---|---|
| **Gabriel Cruz Ferreira** | `catalogo-service` (+ infra: `discovery-server`, `api-gateway`) | PostgreSQL + Elasticsearch |
| **Rafael Broz** | `pedido-service` (+ observabilidade: Prometheus, Grafana) | PostgreSQL |

> Entrega em dupla. Cada integrante é responsável por pelo menos 1 microsserviço, conforme exige o TP.

## Problema que o sistema resolve

Uma rede de farmácias precisa vender online: o cliente busca um medicamento, vê em quais farmácias há estoque e faz um pedido. O domínio se decompõe naturalmente em **catálogo/estoque** (o que existe e onde) e **pedidos** (a compra em si) — dois contextos com ciclos de vida e cargas de trabalho diferentes, daí a arquitetura de microsserviços.

## Arquitetura

```
Cliente
  └─► API Gateway (8080)  ──────────────►  Discovery Server / Eureka (8761)
        ├─► /api/catalogo/**  → catalogo-service (8081) ── PostgreSQL (5433) + Elasticsearch (9200)
        └─► /api/pedidos/**   → pedido-service   (8082) ── PostgreSQL (5434)
                                      └─► chama o catalogo-service via Feign
                                          (Timeout + Fallback → 503 se o catálogo cair)
```

- **Discovery Server (Eureka):** os serviços se registram e se descobrem pelo nome lógico (`lb://`), sem IP/porta fixos.
- **API Gateway:** ponto único de entrada; roteia para os serviços e esconde as portas internas.
- **Bancos separados por serviço:** cada microsserviço é dono dos seus dados (instâncias PostgreSQL distintas).

## Microsserviços

| Serviço | Responsabilidade | Porta | Banco | Endpoints principais |
|---|---|---|---|---|
| discovery-server | Registro/descoberta (Eureka) | 8761 | — | painel em `/` |
| api-gateway | Entrada única / roteamento | 8080 | — | `/api/catalogo/**`, `/api/pedidos/**` |
| catalogo-service | Farmácias, produtos, estoque, busca | 8081 | PostgreSQL + Elasticsearch | `/estabelecimentos`, `/produtos`, `/produtos/buscar`, `/estoque` |
| pedido-service | Criação e consulta de pedidos | 8082 | PostgreSQL | `/pedidos` |

## Banco não relacional (justificativa)

O `catalogo-service` usa **Elasticsearch** para a **busca textual** de produtos (`/produtos/buscar?termo=`): relevância ranqueada, busca parcial/por prefixo e tolerância a digitação — coisas caras de fazer bem em SQL. O **PostgreSQL é a fonte da verdade** (consistência ACID do catálogo/estoque); o Elasticsearch é um **espelho otimizado para leitura/busca**. Se o Elasticsearch cair, escrita e leitura via PostgreSQL continuam; só a busca textual é afetada.

## Resiliência (comunicação entre serviços)

| Quem chama | Quem é chamado | Risco | Estratégia (TP3) |
|---|---|---|---|
| pedido-service | catalogo-service | catálogo lento ou fora do ar | **Timeout (Feign) + Retry + Circuit Breaker (Resilience4j) + Fallback específico** |

A verificação de estoque (`verificarDisponibilidade`, GET idempotente) usa **Resilience4j** no `CatalogoServiceImpl`:

- **Timeout:** o Feign tem `connect-timeout`/`read-timeout` (5s) — o pedido não fica preso esperando o catálogo. (Não usamos `@TimeLimiter`: ele só atua em retorno `CompletableFuture`/reativo; aqui a chamada é síncrona.)
- **Retry:** 3 tentativas com backoff exponencial (200ms→400ms), só para falha **transitória** (`feign.RetryableException`). Seguro por ser idempotente.
- **Circuit Breaker:** janela de 10 chamadas, abre com ≥50% de falha, fica 10s aberto (falha rápido, protege o catálogo) e testa 3 chamadas em half-open.
- **Fallback específico:** trata separadamente **circuito aberto** (`CallNotPermittedException`) e **conexão/timeout** (`FeignException`), ambos → **503 `CATALOGO_INDISPONIVEL`**. Exceções **inesperadas sobem** (viram 500) — não engolimos bug com `catch` genérico.
- **Como simular:** suba tudo, derrube o `catalogo-service` e crie pedidos → primeiras respostas **503** por timeout/retry; após acumular falhas, o circuito **abre** e as respostas passam a falhar rápido.

> Detalhes e o porquê de cada número em [`docs/estudo/tp3-resiliencia-tracing-logs.md`](docs/estudo/tp3-resiliencia-tracing-logs.md).

## Tecnologias

Java 21 · Spring Boot 4.0.6 · Spring Cloud 2025.1.2 (Eureka, Gateway WebFlux, OpenFeign) · PostgreSQL · Elasticsearch 9.0.2 · **Apache Kafka (KRaft)** · **Resilience4j** (Retry + Circuit Breaker) · **Actuator + Micrometer + Prometheus + Grafana** · **Tracing: Micrometer Tracing + OpenTelemetry → Zipkin** · **Logs: Papertrail (syslog)** · Docker Compose · Maven.

## Como executar

**Pré-requisitos:** Docker e Docker Compose (modo A). Para o modo B, também Java 21 + Maven.

### Modo A — tudo via Docker (recomendado)

Sobe **tudo** (bancos, Elasticsearch, Kafka, Prometheus, Grafana, Zipkin e os 4 serviços) num comando:

```bash
docker compose up --build -d
```

Os serviços rodam com o profile `docker` (no `pedido-service`, `prod,docker`), usando os nomes de container nas URLs internas. Acesse pelo gateway em `http://localhost:8080`.

### Modo B — desenvolvimento local (serviços via Maven)

Sobe só a infraestrutura no Docker e roda os serviços na máquina:

```bash
docker compose up -d postgres-catalogo postgres-pedido elasticsearch kafka kafka-ui prometheus grafana zipkin
cd discovery-server && ./mvnw spring-boot:run    # terminal 1
cd catalogo-service && ./mvnw spring-boot:run    # terminal 2
cd api-gateway      && ./mvnw spring-boot:run    # terminal 3
cd pedido-service   && ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod   # terminal 4
```
> O `pedido-service` roda no profile **`prod`** para integração real (Eureka + Feign + Kafka). Sem profile (`dev`/`default`) ele usa um **mock** do catálogo, útil para desenvolver isolado.
> (No modo B, o Prometheus do compose aponta para nomes de container; para coletar serviços no host, ajuste `monitoring/prometheus.yml` para `host.docker.internal`.)

### Portas

| Serviço | Porta |
|---|---|
| Discovery (Eureka) | 8761 |
| API Gateway | 8080 |
| Catalogo Service | 8081 |
| Pedido Service | 8082 |
| Postgres Catalogo | 5433 |
| Postgres Pedido | 5434 |
| Elasticsearch | 9200 |
| Kafka | 9092 |
| Kafka UI | 8085 |
| Prometheus | 9090 |
| Grafana | 3000 |
| Zipkin | 9411 |

## Discovery Server

Painel em **http://localhost:8761** — mostra `CATALOGO-SERVICE`, `PEDIDO-SERVICE` e `API-GATEWAY` registrados.

## Exemplos de requisições (via API Gateway)

```bash
G=http://localhost:8080

# 1) Criar farmácia
curl -X POST $G/api/catalogo/estabelecimentos -H "Content-Type: application/json" \
  -d '{"nome":"Farmacia Central","endereco":"Rua A, 100","telefone":"3199999","horarioFuncionamento":"08-22"}'

# 2) Criar produto (indexa no Elasticsearch)
curl -X POST $G/api/catalogo/produtos -H "Content-Type: application/json" \
  -d '{"nome":"Dipirona 500mg","descricao":"Analgesico","categoria":"Medicamento","fabricante":"NeoQuimica"}'

# 3) Adicionar estoque
curl -X POST $G/api/catalogo/estoque -H "Content-Type: application/json" \
  -d '{"estabelecimentoId":1,"produtoId":1,"quantidade":10,"preco":9.90}'

# 4) Buscar produto (Elasticsearch)
curl "$G/api/catalogo/produtos/buscar?termo=dipi"

# 5) Criar pedido (verifica estoque no catálogo via Feign e decrementa)
curl -X POST $G/api/pedidos -H "Content-Type: application/json" \
  -d '{"estabelecimentoId":1,"itens":[{"produtoId":1,"quantidade":3,"precoUnitario":9.90}]}'

# 6) Consultar pedido
curl $G/api/pedidos/1
```

Respostas esperadas: pedido criado com `status: CONFIRMADO`, `statusPagamento: PAGAMENTO_SIMULADO_APROVADO` e estoque decrementado. Com o catálogo fora do ar, o passo 5 responde **503**.

## TP2 — Mensageria assíncrona e Observabilidade

### O que muda no fluxo

No TP1, ao confirmar o pedido, o `pedido-service` decrementava o estoque chamando o catálogo via Feign (síncrono). No TP2, **o decremento vira assíncrono via Kafka**:

1. `pedido-service` **verifica** o estoque via Feign (continua síncrono — precisa saber se há estoque antes de confirmar).
2. Ao confirmar, **publica o evento `PedidoCriado`** no tópico `pedidos.criados`.
3. `catalogo-service` **consome** o evento e **decrementa** o estoque.

**Por que Kafka e não HTTP?** Se o catálogo cair após o pedido ser confirmado, com HTTP o decremento se perderia. Com Kafka, o evento fica persistido no tópico e é processado quando o catálogo voltar (consistência eventual).

| Produtor | Evento | Tópico | Consumidor | Ação |
|---|---|---|---|---|
| pedido-service | `PedidoCriado` | `pedidos.criados` | catalogo-service | decrementa estoque |

**Cuidados tratados:** idempotência (dedup por `pedidoId`, não decrementa duas vezes), consumidor offline (Kafka mantém a mensagem; o consumer group retoma pelo offset), erro no consumo (try/catch + log).

### Observabilidade

- **Métricas:** `/actuator/prometheus` nos dois serviços; métricas de negócio `pedidos_criados_total` e `eventos_pedido_consumidos_total` (Micrometer). Prometheus coleta; Grafana visualiza.
- **Correlação:** um `correlationId` (UUID) é gerado no pedido, viaja **dentro do evento Kafka** e entra no MDC — os logs dos dois serviços carregam o **mesmo id**. *(No TP3 este id manual foi superado pelo `traceId` real do OpenTelemetry — ver abaixo.)*

### Modelo reativo

O **API Gateway** (Spring Cloud Gateway = WebFlux/Netty) é o componente **reativo/não-bloqueante** da arquitetura; todo o tráfego externo passa por ele. Os serviços de domínio seguem no modelo servlet (JPA/JDBC é bloqueante). Detalhe em `docs/estudo/tp2-reativo.md`.

### Como testar o TP2

```bash
docker compose up -d        # sobe bancos, ES, Kafka, kafka-ui, Prometheus, Grafana
# suba os 4 serviços (ver "Como executar")

# crie farmácia/produto/estoque e um pedido (ver "Exemplos de requisições")
# após o POST /api/pedidos, o estoque é decrementado de forma ASSÍNCRONA (em ~1-2s)
```

- **Kafka UI:** http://localhost:8085 — veja o tópico `pedidos.criados` e as mensagens.
- **Métricas:** http://localhost:8081/actuator/prometheus e http://localhost:8082/actuator/prometheus
- **Prometheus:** http://localhost:9090 (em *Status → Targets*, os dois serviços devem estar `up`).
- **Grafana:** http://localhost:3000 (admin/admin) — adicione o Prometheus (`http://prometheus:9090`) como data source.
- **Correlação:** procure o mesmo `correlationId` nos logs do pedido-service e do catalogo-service.

## TP3 — Resiliência, Tracing e Agregação de logs

Três peças novas, todas amarradas pelo **mesmo `traceId`**. O porquê de cada decisão (com marcadores 🟦 exige / 🟩 padrão / 🟨 nossa escolha) está em [`docs/estudo/tp3-resiliencia-tracing-logs.md`](docs/estudo/tp3-resiliencia-tracing-logs.md).

### Resiliência — Resilience4j
**Retry + Circuit Breaker** na chamada síncrona `pedido → catalogo` (`verificarDisponibilidade`), com fallback específico por tipo de falha. Ver a seção [Resiliência](#resiliência-comunicação-entre-serviços).
- Config: `pedido-service/src/main/resources/application.yml` (bloco `resilience4j:`).
- Código: `pedido-service/.../service/CatalogoServiceImpl.java` (anotações `@Retry`/`@CircuitBreaker` + fallbacks).

### Tracing — Micrometer Tracing + OpenTelemetry → Zipkin
- O trace **nasce no api-gateway** (entrada HTTP) e é **auto-instrumentado** pelo Spring; o `traceId`/`spanId` se propagam por **Feign** e **Kafka** e entram no MDC dos logs.
- **Onde configura o tracing:** `*/application.yml` (`management.tracing.sampling` + `management.tracing.export.zipkin.endpoint`) nos 3 serviços (gateway, pedido, catálogo).
- **Pegadinhas do Boot 4** (todas exigidas para funcionar — ver doc de estudo): a auto-config de tracing saiu do `actuator-autoconfigure`, então são **4 deps** — `spring-boot-micrometer-tracing-opentelemetry` + `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-zipkin` + **`spring-boot-zipkin`** (o transporte HTTP). A propriedade do endpoint foi renomeada para **`management.tracing.export.zipkin.endpoint`**. O Feign só propaga o trace com **`feign-micrometer`**; o Kafka só com **`spring.kafka.{template,listener}.observation-enabled: true`**.
- **Zipkin:** http://localhost:9411 — busque por um `traceId` para ver a requisição inteira (gateway → pedido → Feign/Kafka → catálogo).

### Agregação de logs — Papertrail
- **Arquivo que exporta os logs:** `*/src/main/resources/logback-spring.xml` (appender `PAPERTRAIL`, um por serviço). Ativo só em `prod`/`docker`.
- Destino configurável por env vars **`PAPERTRAIL_HOST`/`PAPERTRAIL_PORT`** (default inofensivo `localhost:1514` para não derrubar a app sem Papertrail). Não precisa de conta ativa para a config existir.
- Como cada linha já carrega o `traceId`, no Papertrail basta buscar por um `traceId` para ver **todos** os serviços daquela requisição.

### Verificação (e2e completo, com Zipkin no ar)
Validado de ponta a ponta: um `POST /api/pedidos` gera um **único trace de 7 spans** no Zipkin atravessando gateway → pedido → Feign → catálogo → Kafka; o **mesmo `traceId`** aparece nos logs dos dois serviços; e a resiliência percorre o ciclo **`closed → open → half_open → closed`** ao derrubar/religar o catálogo (respostas 503 → 201). Passo a passo e descobertas em [`docs/estudo/tp3-resiliencia-tracing-logs.md`](docs/estudo/tp3-resiliencia-tracing-logs.md).

## Convenção de commits

`feat:` · `fix:` · `docs:` · `refactor:` · `chore:` (Conventional Commits). Branch `main` estável; `feature/*` por funcionalidade.
