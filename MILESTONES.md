# Manga Studio milestones

## 1. Studio foundation — complete

* Creator accounts with BCrypt-hashed passwords and creator-scoped sessions
* Projects, chapters, free-form documents, autosave, version history
* Explicit AI proposals and a provider-neutral AI gateway
* PostgreSQL migrations and Docker development stack

## 2. Story atlas & canon control — complete

* Editable Characters, Locations, Organizations, Objects, and Concepts
* Explicit `CANON`, `DRAFT`, `IDEA`, `AI_SUGGESTION`, `UNKNOWN`, and `CONTRADICTED` states
* Creator-controlled Atlas panel with category filters and text search
* Source provenance: linked manuscript documents and direct citation excerpts
* Entity-to-entity relationships with custom types, canon statuses, and full CRUD
* Rich character attributes (Role/Archetype, Appearance, Personality, Abilities, Equipment)
* Timeline event links with participating story entries and source documents

## 3. Timeline & continuity — in progress

Completed foundation:
* Story timeline events with relative ordering (`order_index`) and narrative era/time labels
* Source document provenance links on events
* Many-to-many participant entity links between timeline events and story entries
* Interactive Timeline stream and event creation/editing UI

Remaining: non-blocking contradiction review and automated continuity checks.

## 4. AI memory — planned

Structured extraction with creator approval, source provenance, retrieval-ready semantic memory, and continuity assistance.

## 5. Visual pre-production — planned

Persistent character identity, visual references, locations, scene planning, and panel proposals. No image-generation provider is introduced until this stage.

## 6. Manga creation — planned

Page and panel editor, composition workflow, and production assets.

## 7. Publishing & creator tools — planned

Stable releases, creator profiles, publishing controls, and analytics.

## 8. Reader platform — planned

Public reading, discovery, ratings, follows, comments, bookmarks, and reading history.

## 9. Hardening & delivery — continuous

Broader automated tests, CI/CD, observability, backups, and environment-specific deployment.
