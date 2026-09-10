# Manga Studio — Architecture

## 1. Architecture principles

Manga Studio should begin as a modular monolith with clear domain boundaries.

Do not introduce microservices unless there is a demonstrated operational or scaling reason.

The architecture must allow future extraction of modules into independent services.

Primary stack:

* Frontend: Angular
* Backend: Spring Boot
* Database: PostgreSQL
* Vector search: PostgreSQL + pgvector initially
* Object storage: S3-compatible storage
* Cache: Redis when required
* Authentication: managed authentication initially
* AI: provider abstraction
* Deployment: Docker

---

# 2. High-level architecture

```text
                    Web Browser
                         |
                         v
                 Angular Application
                         |
                         v
                   Spring Boot API
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
   Story Module      AI Module       Community Module
        |                |                |
        +----------------+----------------+
                         |
                  PostgreSQL
                  /        \
                 /          \
        Structured Data    pgvector
                         |
                    Object Storage
```

---

# 3. Backend modules

Initial modules:

```text
identity
projects
documents
chapters
entities
characters
locations
timeline
knowledge
ai
visual
publishing
community
notifications
```

Modules should have explicit boundaries.

Avoid arbitrary cross-module database access.

---

# 4. Identity module

Responsibilities:

* Users
* Authentication integration
* User preferences
* Roles
* Permissions

Roles may eventually include:

* READER
* CREATOR
* MODERATOR
* ADMIN

Do not assume every user is only a writer or only a reader.

---

# 5. Project module

A project represents one creative work.

Example:

Project

* id
* owner
* title
* description
* status
* createdAt
* updatedAt

A project owns:

* Documents
* Chapters
* Characters
* Locations
* Timeline events
* Assets
* AI knowledge

---

# 6. Document module

Documents represent creator-authored content.

A document may be:

* Chapter
* Note
* Outline
* Draft
* Scene
* Other

Creator-authored documents are authoritative sources.

AI-generated derived data must never replace the original document.

---

# 7. Chapter module

Chapters belong to projects.

A chapter contains ordered content.

Potential states:

* DRAFT
* REVIEW
* PUBLISHED
* ARCHIVED

Publishing should create a stable published version.

Editing a draft after publication must not silently modify an already published version.

---

# 8. Entity module

The system should support persistent entities.

Initial entity types:

* CHARACTER
* LOCATION
* ORGANIZATION
* OBJECT
* EVENT
* CONCEPT

Entities may be discovered automatically from documents.

Entities should have references back to the source documents.

---

# 9. Character module

Characters are persistent entities with structured information.

Character data should support:

* Identity
* Attributes
* Relationships
* Abilities
* Clothing
* Equipment
* Visual references
* History
* Notes
* Versions

Character data must be versioned.

---

# 10. Timeline module

Timeline events represent story events.

An event may contain:

* Title
* Description
* Story time
* Relative ordering
* Related characters
* Related locations
* Source document

The system should support uncertain ordering.

For example:

Event A happened before Event B.

Exact dates are not required.

---

# 11. Knowledge module

The knowledge module stores information extracted from creator content.

Knowledge has two layers:

### Structured knowledge

Examples:

* Character age
* Character relationships
* Location information
* Timeline events

### Semantic knowledge

Embeddings representing:

* Scenes
* Chapters
* Notes
* Character descriptions
* Important story passages

Structured knowledge is authoritative for explicit facts.

Vector search is used for semantic retrieval.

---

# 12. AI module

All AI requests go through an internal AI Gateway.

Business modules must never directly call an external AI provider.

Example:

```text
AiGateway
 |
 +-- TextProvider
 +-- EmbeddingProvider
 +-- ImageProvider
 +-- VisionProvider
```

Provider implementations may include different vendors.

The application must remain provider-independent.

---

# 13. AI request lifecycle

```text
User request
    |
    v
AI Controller
    |
    v
AI Orchestrator
    |
    +--> identify task
    |
    +--> retrieve context
    |
    +--> retrieve canon
    |
    +--> retrieve relevant entities
    |
    +--> construct prompt
    |
    +--> call provider
    |
    +--> validate response
    |
    +--> return proposal
    |
    v
Creator
    |
    +--> Accept
    +--> Reject
    +--> Edit
    +--> Regenerate
```

AI should normally produce a proposal rather than directly modifying project data.

---

# 14. AI cost control

Every AI request should be measurable.

Track:

* User
* Project
* Request type
* Model
* Input tokens where available
* Output tokens where available
* Image generations
* Estimated cost
* Timestamp

This is necessary for:

* quotas
* billing
* abuse prevention
* analytics
* cost optimization

---

# 15. Visual generation module

Visual generation must be independent from story generation.

It should manage:

* Assets
* Character references
* Location references
* Scene specifications
* Generation requests
* Generation results
* Regeneration
* Versioning

An image is an artifact.

It is not automatically canon.

---

# 16. Asset model

Assets may include:

* Character reference
* Character expression
* Location reference
* Object reference
* Scene
* Panel
* Page
* Cover

Each asset should retain:

* Provider
* Model
* Prompt/context version
* Source references
* Generation timestamp
* Parent asset
* Project
* Character references
* Scene references

This allows visual assets to be reproduced or regenerated later.

---

# 17. Manga editor

The manga editor should eventually support:

* Pages
* Panels
* Panel ordering
* Panel dimensions
* Images
* Speech bubbles
* Text
* Captions
* Sound effects
* Layering

A page should be stored as structured data rather than as one flattened image.

---

# 18. Community module

Responsibilities:

* Published stories
* Likes
* Ratings
* Comments
* Follows
* Bookmarks
* Reading progress
* Discovery

The public reader experience should operate primarily on published snapshots.

---

# 19. Security

Never expose AI provider API keys to the browser.

The frontend communicates with the backend.

The backend communicates with external AI providers.

Implement:

* Authentication
* Authorization
* Project ownership
* Rate limiting
* AI quotas
* Input validation
* File validation
* Content moderation hooks
* Audit logging for sensitive operations

---

# 20. Storage

Use PostgreSQL for structured application data.

Use object storage for:

* Images
* Generated assets
* Covers
* Manga pages
* Character references

Do not store large binary assets directly inside PostgreSQL.

---

# 21. Search

Initial search:

* PostgreSQL full-text search
* pgvector semantic search

Future options may include dedicated search infrastructure if required by scale.

---

# 22. API design

Use REST initially.

Potential endpoints:

```text
/projects
/projects/{id}

/projects/{id}/chapters
/chapters/{id}

/projects/{id}/characters
/characters/{id}

/projects/{id}/documents
/documents/{id}

/projects/{id}/ai/assist
/projects/{id}/ai/brainstorm
/projects/{id}/ai/continuity

/projects/{id}/assets
/assets/{id}

/stories
/stories/{id}

/stories/{id}/ratings
/stories/{id}/comments
```

API design should evolve based on actual requirements.

---

# 23. Frontend architecture

Use Angular with feature-based organization.

Example:

```text
src/app/

core/
shared/

features/

  studio/
  editor/
  chapters/
  characters/
  timeline/
  ai/
  visual/
  manga-editor/

  reader/
  discovery/
  creator-profile/
```

Avoid putting business logic into components.

Use services/facades where appropriate.

---

# 24. UI architecture

The Studio and Reader should be different experiences.

Studio:

* Dense
* Productive
* Keyboard-friendly
* Contextual tools

Reader:

* Minimal
* Immersive
* Content-focused

---

# 25. Persistence and versioning

Creator content must support version history.

At minimum:

* Current version
* Previous versions
* Created timestamp
* Author
* Change metadata

AI-generated proposals should also be recoverable when useful.

---

# 26. Events

Use domain events internally where they improve decoupling.

Examples:

```text
DocumentUpdated
ChapterPublished
CharacterUpdated
EntityDetected
AIProposalCreated
AssetGenerated
StoryPublished
```

Do not introduce a distributed event system prematurely.

---

# 27. Deployment

Development:

Docker Compose

Production initially:

* Managed PostgreSQL
* Object storage
* Containerized Spring Boot
* Angular static hosting/CDN
* HTTPS
* Domain
* Monitoring

The deployment strategy should allow horizontal scaling later.

---

# 28. Testing

Backend:

* Unit tests
* Integration tests
* Repository tests
* API tests

Frontend:

* Component tests
* Service tests
* Critical end-to-end tests

AI:

* Prompt regression tests
* Structured-output validation
* Canon consistency tests
* Retrieval tests

AI behavior must be testable independently from UI.

---

# 29. Observability

Track:

* API latency
* Errors
* AI latency
* AI failures
* Token usage
* Generation costs
* Storage usage
* Queue status

Do not log sensitive creator content unnecessarily.

---

# 30. Architectural rule

Prefer simple solutions until complexity is justified.

The first production version should be a modular monolith.

Extract services only when:

* scaling requires it
* deployment independence is valuable
* team boundaries justify it
* operational requirements justify it

Do not build microservices for architectural fashion.
