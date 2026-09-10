# Initial milestone architecture

This milestone keeps the first release deliberately narrow: the writing loop before knowledge extraction, visuals, or publishing.

## Module boundaries

* `identity` supplies BCrypt-hashed creator accounts through a session adapter. A managed OIDC/JWT adapter can validate the authenticated subject and retain the same `CurrentUser` contract.
* `projects` owns project metadata and ownership checks.
* `documents` owns chapters, creator-authored documents, and append-only document versions.
* `ai` exposes `AiGateway`; its development implementation returns clearly labelled proposals. No provider SDK appears outside that adapter.

## Creator authority

Document text is source content. A save records a creator version. AI endpoints return `AI_SUGGESTION` content only and never mutate documents, versions, or canon. The explicit approve/edit/reject flow is therefore safe to add later without changing this principle.

## API surface

* `GET/POST /api/projects`
* `GET /api/projects/{projectId}/workspace`
* `POST /api/projects/{projectId}/chapters`
* `GET/PUT /api/projects/{projectId}/documents/{documentId}`
* `GET /api/projects/{projectId}/documents/{documentId}/versions`
* `POST /api/projects/{projectId}/ai/proposals`

OpenAPI is generated at runtime by Springdoc.
