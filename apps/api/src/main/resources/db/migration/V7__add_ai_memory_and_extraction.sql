CREATE TABLE memory_proposal (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  source_document_id UUID REFERENCES document(id) ON DELETE CASCADE,
  target_type VARCHAR(30) NOT NULL,
  name VARCHAR(180) NOT NULL,
  suggested_type VARCHAR(50) NOT NULL,
  suggested_details TEXT NOT NULL DEFAULT '',
  source_excerpt TEXT NOT NULL DEFAULT '',
  confidence NUMERIC(3,2) NOT NULL DEFAULT 0.85,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_memory_proposal_project ON memory_proposal(project_id);
CREATE INDEX idx_memory_proposal_doc ON memory_proposal(source_document_id);
CREATE INDEX idx_memory_proposal_status ON memory_proposal(project_id, status);

CREATE TABLE semantic_memory_chunk (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
  source_document_id UUID REFERENCES document(id) ON DELETE CASCADE,
  entity_id UUID REFERENCES story_entity(id) ON DELETE SET NULL,
  chunk_type VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  tags VARCHAR(255) NOT NULL DEFAULT '',
  authority VARCHAR(30) NOT NULL DEFAULT 'CANON',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_semantic_chunk_project ON semantic_memory_chunk(project_id);
CREATE INDEX idx_semantic_chunk_doc ON semantic_memory_chunk(source_document_id);
