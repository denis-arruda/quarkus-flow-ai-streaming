# Quarkus Flow AI Streaming Demo

A demonstration of [Quarkus Flow](https://docs.quarkiverse.io/quarkus-flow/dev/index.html) powering an agentic AI workflow with Kafka as both input and output.

## What This Demonstrates

An **automated content intelligence pipeline** that helps organizations publish content faster and more safely.

When a piece of content is published, a single agentic AI workflow runs three agents in sequence:

1. **Content Enrichment** — AI extracts themes, emotional tone, target audience, and keywords from the raw content, eliminating manual tagging.
2. **Sensitivity & Compliance** — AI evaluates age ratings, regional restrictions, and risk flags before the content reaches any audience, reducing legal and reputational exposure.
3. **Marketing Asset Generation** — AI produces headlines, taglines, and promotional copy instantly, cutting time-to-market for content promotion.

The pipeline transforms raw published content into compliance-checked, audience-ready marketing assets — reducing manual effort and human error across the entire content lifecycle.

### Technical highlights

- **Kafka → Flow → Kafka**: A CloudEvent arrives on a Kafka topic, triggers the agentic workflow, and the enriched result is published back to another Kafka topic.
- **Three LangChain4j agents in one Flow**: Enrichment, compliance, and marketing run as sequential tasks inside a single Quarkus Flow workflow.
- **Redis persistence**: Workflow state is checkpointed to Redis after each task — interrupted workflows resume automatically from their last checkpoint.
- **Quarkus Flow DSL**: Type-safe Java DSL that maps to the CNCF Serverless Workflow Specification.
- **Dev UI**: Live workflow diagram (Mermaid), execution tracing, and test forms — available at `http://localhost:8080/q/dev`.

## Architecture

```
Kafka topic: flow-in
       │
       ▼ (CloudEvent: dev.denisarruda.content.published)
┌──────────────────────────────────────────────────────┐
│                  Quarkus Flow Engine                  │
│                                                      │
│  listen                                              │
│    │                                                 │
│    ▼                                                 │
│  EnrichmentAgent   (themes, tone, audience, keywords)│
│    │                                                 │
│    ▼                                                 │
│  ComplianceAgent   (age rating, restrictions, risks) │
│    │                                                 │
│    ▼                                                 │
│  MarketingAgent    (headlines, taglines, copy)       │
│    │                                                 │
│    ▼                                                 │
│  emit result                                         │
└──────────────────────────────────────────────────────┘
       │
       ▼ (CloudEvent: dev.denisarruda.content.ready)
Kafka topic: flow-out
```

## Modules

| Module             | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `content-pipeline` | Quarkus app — Kafka listener, agentic AI workflow, Kafka publisher          |
| `frontend-web`     | Web UI to publish content and view finalized pipeline results               |

## Tech Stack

| Component        | Library                                      |
|-----------------|----------------------------------------------|
| Runtime          | Quarkus 3.x                                  |
| Workflow engine  | `quarkus-flow` (Quarkiverse)                 |
| AI agents        | `quarkus-langchain4j` + OpenAI               |
| Messaging        | `quarkus-messaging-kafka` (SmallRye Kafka)   |
| Persistence      | Redis (`quarkus-flow-redis`) — workflow checkpoints  |
| Container images | Quarkus JIB (`quarkus-container-image-jib`)  |
| Build            | Maven + Maven Wrapper (`./mvnw`)             |
| Observability    | `grafana/otel-lgtm` (Loki + Grafana + Tempo + Mimir) |
| Kafka UI         | `provectuslabs/kafka-ui`                     |

## Event Model

All events accumulate fields as they pass through the pipeline. Previous fields are never modified.

### Input event (`flow-in`)

```json
{
  "contentId": "S123",
  "title": "Shadow District",
  "description": "A political thriller set in a dystopian city.",
  "genre": "Thriller",
  "region": "GLOBAL",
  "timestamp": "2026-03-01T10:00:00Z"
}
```

### Final event (`flow-out`) — fully accumulated

```json
{
  "contentId": "S123",
  "title": "Shadow District",
  "description": "A political thriller set in a dystopian city.",
  "genre": "Thriller",
  "region": "GLOBAL",
  "timestamp": "2026-03-01T10:00:00Z",
  "enrichment": {
    "themes": ["corruption", "moral conflict"],
    "emotionalTone": "dark suspense",
    "audienceProfile": "adults 25-45",
    "keywords": ["political intrigue", "power struggle"]
  },
  "sensitivity": {
    "ageRatingSuggested": "16+",
    "sensitiveRegions": ["DE", "IN"],
    "riskFlags": ["political corruption theme"]
  },
  "marketing": {
    "headlineGlobal": "In a city of shadows, trust is the ultimate risk.",
    "tagline": "Power hides in the dark.",
    "shortDescription": "A suspense thriller about corruption and survival."
  }
}
```

## Flow DSL Overview

Workflows are plain CDI beans extending `Flow`. The messaging bridge is automatic — no boilerplate consumer/producer code needed:

```java
@ApplicationScoped
public class ContentIntelligenceFlow extends Flow {

    @Override
    public Workflow descriptor() {
        return workflow("content-intelligence")
            .tasks(
                listen("waitContent", toOne("dev.denisarruda.content.published"))
                    .outputAs((Collection<Object> c) -> c.iterator().next()),

                agent("enrich", enrichmentAgent::enrich, ContentEnrichment.class)
                    .inputFrom(".")
                    .exportAs("{ enrichment: . }"),

                agent("compliance", complianceAgent::evaluate, Sensitivity.class)
                    .inputFrom(".")
                    .exportAs("{ sensitivity: . }"),

                agent("marketing", marketingAgent::generate, Marketing.class)
                    .inputFrom(".")
                    .exportAs("{ marketing: . }"),

                emitJson("dev.denisarruda.content.ready", Map.class)
            )
            .build();
    }
}
```

## Kafka Configuration

The `quarkus.flow.messaging.defaults-enabled=true` property wires Quarkus Flow to two channels:

| Channel    | Direction | Kafka Topic  |
|------------|-----------|--------------|
| `flow-in`  | Inbound   | `flow-in`    |
| `flow-out` | Outbound  | `flow-out`   |

Messages are CloudEvents with a JSON string payload.

## Running Locally

### Prerequisites

- Java 26
- Docker (for Kafka, Redis, Grafana, Kafka UI)
- An OpenAI API key exported as `LLM_API_KEY`

```bash
export LLM_API_KEY=sk-...
```

### Start

```bash
docker compose up -d
./mvnw quarkus:dev
```

### Send a test event

```bash
# Publish a CloudEvent to the flow-in Kafka topic
# (example command — TBD once producer is wired)
```

### Observe

| Tool          | URL                        | Purpose                              |
|---------------|----------------------------|--------------------------------------|
| Quarkus Dev UI | `http://localhost:8080/q/dev` | Workflow diagrams, execution traces, manual triggers |
| Kafka UI      | `http://localhost:8090`    | Browse topics, inspect messages      |
| Grafana       | `http://localhost:3000`    | Metrics, logs, and traces (LGTM stack) |
| Redis         | `localhost:6379`           | Workflow state checkpoints           |

## Project Structure

```
quarkus-flow-ai-streaming/          ← parent POM
├── content-pipeline/               ← Quarkus module (agentic workflow)
│   └── src/main/java/
│       └── dev/denisarruda/
│           ├── content/
│           │   ├── control/        # ContentIntelligenceFlow (workflow definition)
│           │   └── entity/         # Records: ContentEvent, ContentEnrichment,
│           │                       #          Sensitivity, Marketing
│           └── ai/
│               └── boundary/       # @RegisterAiService interfaces:
│                                   #   EnrichmentAgent, SensitivityAgent, MarketingAgent
└── frontend-web/                   ← Web frontend module
```

## References

- [Quarkus Flow Docs](https://docs.quarkiverse.io/quarkus-flow/dev/index.html)
- [Quarkus Flow + LangChain4j](https://docs.quarkiverse.io/quarkus-flow/dev/langchain4j.html)
- [Quarkus Flow Messaging (Kafka)](https://docs.quarkiverse.io/quarkus-flow/dev/messaging.html)
- [Quarkus Flow Persistence (Redis)](https://docs.quarkiverse.io/quarkus-flow/dev/persistence.html)
- [CNCF Serverless Workflow Specification](https://serverlessworkflow.io/)
