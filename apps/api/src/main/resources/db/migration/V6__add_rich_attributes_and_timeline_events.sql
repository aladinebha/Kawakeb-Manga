ALTER TABLE story_entity ADD COLUMN source_excerpt TEXT NOT NULL DEFAULT '';
ALTER TABLE story_entity ADD COLUMN attributes TEXT NOT NULL DEFAULT '{}';

ALTER TABLE story_relationship ADD COLUMN source_document_id UUID REFERENCES document(id) ON DELETE SET NULL;

CREATE TABLE timeline_event (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL DEFAULT '',
  story_time VARCHAR(100) NOT NULL DEFAULT '',
  order_index INTEGER NOT NULL DEFAULT 0,
  source_document_id UUID REFERENCES document(id) ON DELETE SET NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_timeline_event_project ON timeline_event(project_id);
CREATE INDEX idx_timeline_event_order ON timeline_event(project_id, order_index);

CREATE TABLE timeline_event_entity (
  event_id UUID NOT NULL REFERENCES timeline_event(id) ON DELETE CASCADE,
  entity_id UUID NOT NULL REFERENCES story_entity(id) ON DELETE CASCADE,
  role VARCHAR(50) NOT NULL DEFAULT 'PARTICIPANT',
  PRIMARY KEY (event_id, entity_id)
);

CREATE INDEX idx_timeline_event_entity_event ON timeline_event_entity(event_id);
CREATE INDEX idx_timeline_event_entity_entity ON timeline_event_entity(entity_id);
