# Manga Studio

The creator-first foundation for a story and manga workspace. This repository is a modular monolith: an Angular client, Spring Boot REST API, and PostgreSQL database.

## Run locally

Prerequisites: Docker Desktop (recommended), or Node 20+ and Java 21+.

```bash
docker compose up --build
```

Open `http://localhost:4200`. The API is available at `http://localhost:8080/api` and its OpenAPI UI at `http://localhost:8080/swagger-ui.html`.

For development, start PostgreSQL with `docker compose up db`, then run the API with `mvn spring-boot:run` from `apps/api` (Java 21+) and the client with `npm install && npm start` from `apps/web`.

## Initial milestone

Implemented: local development identity, projects, chapters and documents, automatic document snapshots, a focused writing workspace, and an AI gateway seam. AI responses are always returned as proposals with `AI_SUGGESTION` authority; accepting a proposal is deliberately a separate future action.

Not implemented by design: image generation, public discovery/social features, semantic retrieval, and automatic canon changes.
