ALTER TABLE story_entity ADD COLUMN source_document_id UUID REFERENCES document(id) ON DELETE SET NULL;

CREATE TABLE story_relationship (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  source_entity_id UUID NOT NULL REFERENCES story_entity(id) ON DELETE CASCADE,
  target_entity_id UUID NOT NULL REFERENCES story_entity(id) ON DELETE CASCADE,
  relationship_type VARCHAR(100) NOT NULL,
  description TEXT NOT NULL DEFAULT '',
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (source_entity_id <> target_entity_id)
);
CREATE INDEX idx_story_relationship_project ON story_relationship(project_id);
CREATE INDEX idx_story_relationship_source ON story_relationship(source_entity_id);
CREATE INDEX idx_story_relationship_target ON story_relationship(target_entity_id);
