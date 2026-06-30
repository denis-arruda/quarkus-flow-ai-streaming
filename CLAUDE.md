# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-module Maven project: an agentic AI content intelligence pipeline where Kafka delivers raw published content, a Quarkus Flow workflow runs three sequential LangChain4j AI agents, and enriched results are published back to Kafka.

Modules:
- `content-pipeline` — Quarkus app hosting the agentic workflow and Kafka integration
- `frontend-web` — Web UI for publishing content and viewing finalized results

## Build Commands

```bash
# Build all modules
./mvnw clean package

# Build skipping tests
./mvnw clean package -DskipTests

# Run content-pipeline in dev mode
./mvnw -pl content-pipeline quarkus:dev

# Run a single test
./mvnw -pl content-pipeline test -Dtest=MyTestClass

# Build container images (JIB)
./mvnw clean package -Dquarkus.container-image.build=true
```

## Container Images

Container images are built using Quarkus JIB (`quarkus-container-image-jib`):

```bash
./mvnw clean package -Dquarkus.container-image.build=true
```

JIB was chosen over the Buildpack extension because this is a multi-module Maven project: the Buildpack extension rebuilds from source inside an isolated container where the parent POM is not resolvable, causing a fatal build failure. JIB avoids this entirely by packaging the pre-built JAR directly into the image — no Docker daemon or source rebuild required.

## Infrastructure

Start all dependencies before running the application:

```bash
docker compose up -d
export LLM_API_KEY=sk-...
```

| Service        | URL                           |
|----------------|-------------------------------|
| Kafka UI       | http://localhost:8090         |
| Grafana        | http://localhost:3000         |
| Redis          | localhost:6379                |
| Quarkus Dev UI | http://localhost:8080/q/dev   |

All services (Kafka, Redis, Grafana LGTM, Kafka UI) are defined in `docker-compose.yml` at the project root.

## Architecture

### Data Flow

```
Kafka (flow-in) → Quarkus Flow Engine → [EnrichmentAgent → ComplianceAgent → MarketingAgent] → Kafka (flow-out)
```

The Quarkus Flow messaging bridge (`quarkus.flow.messaging.defaults-enabled=true`) automatically wires the `flow-in` / `flow-out` Kafka channels — no manual `@Incoming` / `@Outgoing` consumers needed. Messages are CloudEvents serialized as byte arrays.

### Workflow

`ContentIntelligenceFlow` extends `io.quarkiverse.flow.Flow` and defines the full pipeline as a single `descriptor()` method using the `FuncWorkflowBuilder` DSL:

1. `listen` — waits for CloudEvent type `dev.denisarruda.content.published` on `flow-in`
2. `agent("enrich", ...)` — appends `enrichment` object to payload
3. `agent("compliance", ...)` — appends `sensitivity` object to payload
4. `agent("marketing", ...)` — appends `marketing` object to payload
5. `emitJson(...)` — publishes `dev.denisarruda.content.ready` to `flow-out`

Each agent exports its result under its own key — the full payload accumulates as the workflow progresses. Previous fields are never modified (immutability by convention).

### Event Model

Input event fields (from `flow-in`): `contentId`, `title`, `description`, `genre`, `region`, `timestamp`.

Each agent appends one object:

| Agent             | Key appended    | Fields                                                          |
|-------------------|-----------------|-----------------------------------------------------------------|
| `EnrichmentAgent` | `enrichment`    | `themes[]`, `emotionalTone`, `audienceProfile`, `keywords[]`   |
| `SensitivityAgent`| `sensitivity`   | `ageRatingSuggested`, `sensitiveRegions[]`, `riskFlags[]`      |
| `MarketingAgent`  | `marketing`     | `headlineGlobal`, `tagline`, `shortDescription`                |

The `flow-out` event is the fully accumulated payload containing all four layers.

### Package Structure

Follows BCE (Boundary–Control–Entity) pattern under `dev.denisarruda`:

- `content.control` — `ContentIntelligenceFlow` (workflow definition)
- `content.entity` — records: `ContentEvent`, `ContentEnrichment`, `Sensitivity`, `Marketing`
- `ai.boundary` — `@RegisterAiService` interfaces: `EnrichmentAgent`, `SensitivityAgent`, `MarketingAgent`

### Persistence

`quarkus-flow-redis` checkpoints workflow state to Redis after each completed task. If the application restarts mid-workflow, it resumes automatically from the last checkpoint (`quarkus.flow.persistence.autoRestore=true` by default). Redis connection: `quarkus.redis.hosts=redis://localhost:6379`.

### Observability

`grafana/otel-lgtm` is the single observability container (Loki + Grafana + Tempo + Mimir). The application exports Prometheus metrics at `/q/metrics` via `quarkus-micrometer-registry-prometheus`. The `otel-lgtm` container scrapes this endpoint via a mounted `prometheus.yml`.
