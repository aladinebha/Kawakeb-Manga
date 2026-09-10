# Manga Studio — AI Memory Architecture

## 1. Purpose

The AI memory system allows Manga Studio to understand a creator's project over time without requiring the creator to repeatedly explain the story.

The system must preserve creator authority.

Memory is used to improve AI assistance, not to replace creator content.

---

# 2. Fundamental rule

The original creator content is the source of truth.

AI memory is derived knowledge.

AI must never silently rewrite creator-authored content.

---

# 3. Memory layers

The system contains several memory layers.

## Layer 1 — Source Content

The actual creator-authored documents.

Examples:

* Chapters
* Notes
* Drafts
* Scenes
* Character descriptions
* Outlines

This is the highest-authority source.

---

## Layer 2 — Structured Knowledge

Information extracted from source content.

Examples:

```text
Character:
Aki

Age:
17

Brother:
Ren

Event:
Ren disappeared

Location:
Tokyo
```

Every extracted fact should maintain references to its source.

---

## Layer 3 — Canon

Canon represents information explicitly accepted or established by the creator.

Canon can originate from:

* Creator writing
* Creator edits
* Creator-approved AI suggestions

AI-generated information is not canon until accepted.

---

## Layer 4 — Semantic Memory

Embeddings representing meaningful portions of the project.

Examples:

* Chapter sections
* Scenes
* Notes
* Character descriptions
* Important events
* Dialogue
* World information

Semantic memory allows the AI to find conceptually relevant information.

---

## Layer 5 — Timeline Memory

Story events and relationships.

Example:

```text
Event A
  happened before
Event B
  happened before
Event C
```

The system must support uncertain ordering.

---

## Layer 6 — Relationship Memory

Relationships between entities.

Examples:

```text
Aki --friend_of--> Hana

Hana --sister_of--> Ren

Aki --enemy_of--> Dragon King
```

Relationships should have source references and confidence/status.

---

## Layer 7 — Visual Memory

Visual information used for consistent image generation.

Examples:

Character:

* Face reference
* Hair
* Eyes
* Clothing
* Accessories
* Body characteristics
* Expressions
* Poses
* Reference images

Location:

* Architecture
* Environment
* Lighting
* Color characteristics
* Reference images

---

# 4. Memory authority

Each piece of knowledge should have an authority level.

Suggested levels:

```text
SOURCE
CANON
CREATOR_APPROVED
DERIVED
AI_SUGGESTION
UNKNOWN
CONTRADICTED
ARCHIVED
```

SOURCE and CANON have higher authority than AI suggestions.

---

# 5. Provenance

Every important memory item should know where it came from.

Example:

```text
Fact:
Aki is 17.

Source:
Chapter 2

Location:
Paragraph 14

Created by:
Creator

Status:
CANON
```

This allows the AI to explain why it believes something.

---

# 6. Contradictions

Contradictions must not automatically be resolved.

Example:

Chapter 2:

"Aki is seventeen."

Chapter 12:

"Aki celebrated his twentieth birthday."

The system should identify the conflict.

Possible interpretation:

* Error
* Time skip
* Retcon
* Different version
* Intentional contradiction

The AI should ask or suggest.

The creator decides.

---

# 7. Temporal versions

Facts may change over time.

Example:

```text
Aki age:

Chapter 1:
17

Chapter 20:
19
```

This may be completely valid.

Memory must support temporal validity.

A fact should be able to specify:

* valid from
* valid until
* story time
* source chapter

---

# 8. Character memory

Each character should have a persistent identity.

Character identity includes:

```text
Identity
Appearance
Personality
History
Relationships
Abilities
Equipment
Clothing
Visual references
```

These properties can change over time.

---

# 9. Character states

A character can have different states.

Example:

```text
Aki

Base identity:
Black hair
Brown eyes

Chapter 1:
Normal clothes

Chapter 10:
Battle armor

Chapter 20:
Damaged armor
```

The identity remains stable while the state changes.

---

# 10. Visual identity

Visual generation should distinguish:

### Identity

What makes the character recognizable.

### State

What changes between scenes.

State includes:

* Pose
* Expression
* Clothing
* Injuries
* Equipment
* Environment

The image generation context combines both.

---

# 11. Character reference system

A character may have multiple visual references:

```text
Front
Side
Back
Face close-up
Happy
Angry
Sad
Fighting
Normal outfit
Battle outfit
```

References should be versioned.

The creator can mark references as preferred.

---

# 12. Scene memory

A scene contains contextual information:

```text
Characters
Location
Time
Weather
Action
Emotion
Camera
Lighting
Objects
Visual references
```

When generating a scene, the system retrieves relevant information from each category.

---

# 13. Context retrieval

Do not send the entire project to the AI for every request.

Instead:

```text
User request
      |
      v
Identify task
      |
      v
Identify relevant entities
      |
      v
Retrieve canon
      |
      v
Retrieve relevant source content
      |
      v
Retrieve timeline context
      |
      v
Retrieve visual context
      |
      v
Construct context
      |
      v
AI
```

---

# 14. Retrieval priorities

When retrieving context, prioritize:

1. Current selected text
2. Current scene
3. Current chapter
4. Related characters
5. Related locations
6. Relevant canon
7. Timeline context
8. Relevant previous chapters
9. Semantic project memory

Avoid irrelevant context.

---

# 15. Embeddings

Embeddings should be generated for meaningful chunks.

Potential chunks:

* Scene
* Chapter section
* Character description
* Location description
* Important event
* Note

Do not blindly embed every database row.

Embeddings should contain metadata such as:

* projectId
* documentId
* chapterId
* entityId
* memoryType
* authority
* timestamp

---

# 16. Structured retrieval

Use database queries for facts such as:

* Who is Aki?
* What is Aki's current age?
* Who is Aki's brother?
* Where is the Dragon King?
* What happened before Chapter 10?

Do not depend on vector search for exact structured facts.

---

# 17. Semantic retrieval

Use vector search for questions such as:

"What scenes involve Aki feeling guilty about his brother?"

or:

"Find previous scenes with the same emotional theme."

---

# 18. Hybrid retrieval

Most important AI requests should use both.

Example:

```text
Structured:
Aki
Age: 19
Location: Tokyo
Relationship: brother of Ren

Semantic:
Previous scenes involving Aki and Ren

Timeline:
Ren disappeared 8 years ago

Current:
Chapter 12, Scene 3
```

Then construct the AI context.

---

# 19. Memory updates

After creator content changes:

```text
Document updated
      |
      v
Extract possible changes
      |
      v
Compare with existing knowledge
      |
      v
Create/update derived knowledge
      |
      v
Detect contradictions
      |
      v
Update embeddings
```

Updates should be asynchronous when possible.

---

# 20. AI-generated content

AI-generated content should initially have:

```text
AI_SUGGESTION
```

When the creator accepts it:

```text
CREATOR_APPROVED
```

If inserted into a chapter and confirmed as part of the story:

```text
CANON
```

The exact transition rules should be explicit.

---

# 21. Memory editing

Creators must be able to inspect and modify important memory.

For example:

```text
Character: Aki

Age
19
[Edit]

Brother
Ren
[Edit]

Ability
Unknown
[Add]

Visual reference
[Change]
```

The creator should not need to understand embeddings or vector databases.

---

# 22. Memory explanations

When appropriate, the AI should be able to answer:

"Why do you think Aki hates the ocean?"

Response should reference relevant creator content.

The UI may show:

"Based on Chapter 3 and Chapter 8."

This increases trust.

---

# 23. Retcons

Creators may intentionally rewrite established information.

A retcon should not be treated as an error automatically.

Example:

Creator changes:

Aki's brother disappeared at age 10.

to:

Aki's brother disappeared at age 12.

The system should preserve the old version and mark the current version as authoritative.

---

# 24. Memory deletion

Deleting a source document should not necessarily immediately delete all memory.

The system should identify derived knowledge affected by deletion.

Potential states:

* Active
* Invalidated
* Orphaned
* Archived

The system should avoid silently preserving information that no longer has a valid source.

---

# 25. Memory quality

Memory extraction should be treated as probabilistic.

Every derived fact should potentially contain:

* Confidence
* Source
* Extraction timestamp
* Model
* Authority
* Status

Low-confidence information should not be treated as canon.

---

# 26. Memory security

A creator's private story data must remain private.

AI providers should receive only the context necessary for the requested operation.

Do not expose another creator's project information through retrieval.

Every memory query must be scoped by project ownership/authorization.

---

# 27. Visual memory generation

For image generation, construct a visual context:

```text
Character identity
+
Character state
+
Clothing
+
Pose
+
Expression
+
Location
+
Objects
+
Scene action
+
Camera
+
Lighting
+
Art direction
+
Preferred reference images
```

The visual context should be generated dynamically for each scene.

---

# 28. Consistency checking

Before generating a visual scene:

Check:

* Character identity
* Current clothing
* Relevant equipment
* Location
* Timeline
* Character state

If an important conflict exists, notify the creator.

Example:

"Your current scene places Aki in his school uniform, but the previous scene says he changed into battle armor."

Allow:

* Fix
* Ignore
* Continue anyway

---

# 29. Memory should remain invisible by default

The creator should not constantly see:

"Vector retrieval successful."

The complexity belongs to the system.

The creator should see useful results:

"AI remembers that..."

or:

"Potential continuity issue."

---

# 30. Long-term memory architecture

The initial implementation can use:

PostgreSQL
+
pgvector
+
structured tables

Later, if necessary, introduce:

* dedicated vector database
* graph database
* knowledge graph
* specialized retrieval service

Do not introduce these technologies until actual scale or requirements justify them.

---

# 31. Ultimate goal

The AI should eventually feel like it has read and understood the creator's entire work.

The creator should be able to ask:

"What would Aki do here?"

and receive an answer based on:

* Aki's personality
* Previous actions
* Relationships
* Current situation
* Story history
* Established canon

without the creator having to explain Aki again.

The system should understand the story while always recognizing that the creator has final authority.
