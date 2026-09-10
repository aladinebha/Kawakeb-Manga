import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, HostListener, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

export type User = { id: string; email: string; displayName: string };
export type Project = { id: string; title: string; description: string };
export type Chapter = { id: string; title: string; position: number };
export type Document = {
  id: string;
  chapterId?: string;
  title: string;
  type: string;
  content: string;
  revision: number;
  updatedAt: string;
};
export type StoryEntity = {
  id: string;
  type: string;
  name: string;
  description: string;
  status: string;
  sourceDocumentId?: string;
  sourceExcerpt?: string;
  attributes?: string;
};
export type StoryRelationship = {
  id: string;
  sourceEntityId: string;
  targetEntityId: string;
  relationshipType: string;
  description: string;
  status: string;
  sourceDocumentId?: string;
};
export type TimelineEvent = {
  id: string;
  title: string;
  description: string;
  storyTime: string;
  orderIndex: number;
  sourceDocumentId?: string;
  status: string;
  entityIds: string[];
};
export type Workspace = { project: Project; chapters: Chapter[]; documents: Document[] };
export type Version = { id: string; revision: number; content: string; createdAt: string };
export type Proposal = { content: string; authority: string; note: string };

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private saveTimer?: ReturnType<typeof setTimeout>;
  private selectedText = '';

  user?: User;
  authMode: 'login' | 'register' = 'register';
  name = '';
  email = '';
  password = '';
  authError = '';

  projects: Project[] = [];
  workspace?: Workspace;
  active?: Document;
  versions: Version[] = [];
  proposal?: Proposal;
  saving = false;
  saved = '';

  // Panels: assistant | atlas | timeline | history
  panel: 'assistant' | 'atlas' | 'timeline' | 'history' = 'assistant';

  // Story Atlas State
  entities: StoryEntity[] = [];
  relationships: StoryRelationship[] = [];
  selectedEntity?: StoryEntity;
  atlasFilter: string = 'ALL';
  atlasSearch: string = '';

  characterCount(): number {
    return this.entities.filter(e => e.type === 'CHARACTER').length;
  }
  locationCount(): number {
    return this.entities.filter(e => e.type === 'LOCATION').length;
  }
  organizationCount(): number {
    return this.entities.filter(e => e.type === 'ORGANIZATION').length;
  }
  objectCount(): number {
    return this.entities.filter(e => e.type === 'OBJECT' || e.type === 'CONCEPT').length;
  }
  entityAttributes = {
    role: '',
    appearance: '',
    personality: '',
    abilities: '',
    equipment: '',
    notes: ''
  };

  // Relationship Form
  relationshipTarget = '';
  relationshipType = '';
  relationshipStatus = 'CANON';

  // Timeline State
  timelineEvents: TimelineEvent[] = [];
  editingEvent?: TimelineEvent;
  eventModalOpen = false;
  eventForm: {
    title: string;
    description: string;
    storyTime: string;
    orderIndex: number;
    sourceDocumentId?: string;
    status: string;
    entityIds: string[];
  } = {
    title: '',
    description: '',
    storyTime: '',
    orderIndex: 0,
    status: 'CANON',
    entityIds: []
  };

  ngOnInit() {
    this.http.get<User>('/api/auth/me').subscribe({
      next: user => {
        this.user = user;
        this.loadProjects();
      },
      error: () => undefined
    });
  }

  ngOnDestroy() {
    if (this.saveTimer) clearTimeout(this.saveTimer);
  }

  loadProjects() {
    this.http.get<Project[]>('/api/projects').subscribe(p => {
      this.projects = p;
      if (p[0]) this.open(p[0]);
    });
  }

  authenticate() {
    this.authError = '';
    const request =
      this.authMode === 'register'
        ? this.http.post<User>('/api/auth/register', {
            email: this.email,
            password: this.password,
            displayName: this.name
          })
        : this.http.post<User>('/api/auth/login', {
            email: this.email,
            password: this.password
          });
    request.subscribe({
      next: user => {
        this.user = user;
        this.password = '';
        this.loadProjects();
      },
      error: error => (this.authError = error.error?.message || 'Unable to sign in.')
    });
  }

  logout() {
    this.http.post('/api/auth/logout', {}).subscribe(() => {
      this.user = undefined;
      this.workspace = undefined;
      this.projects = [];
      this.active = undefined;
      this.email = '';
      this.password = '';
    });
  }

  createProject() {
    const title = prompt('Project title');
    if (!title?.trim()) return;
    this.http.post<Project>('/api/projects', { title, description: '' }).subscribe(p => {
      this.projects = [p, ...this.projects];
      this.open(p);
    });
  }

  open(p: Project) {
    this.http.get<Workspace>(`/api/projects/${p.id}/workspace`).subscribe(w => {
      this.workspace = w;
      this.active = w.documents[0];
      this.history();
      this.loadEntities();
      this.loadRelationships();
      this.loadTimeline();
      this.proposal = undefined;
      this.selectedEntity = undefined;
    });
  }

  select(d: Document) {
    this.flushSave();
    this.active = d;
    this.history();
    this.proposal = undefined;
    this.selectedText = '';
  }

  addChapter() {
    if (!this.workspace) return;
    const title = prompt('Chapter title', `Chapter ${this.workspace.chapters.length + 1}`);
    if (!title) return;
    this.http
      .post<Chapter>(`/api/projects/${this.workspace.project.id}/chapters`, { title })
      .subscribe(() => this.open(this.workspace!.project));
  }

  addDocument(type: string) {
    if (!this.workspace) return;
    const title = prompt(`${type[0] + type.slice(1).toLowerCase()} title`);
    if (!title?.trim()) return;
    this.http
      .post<Document>(`/api/projects/${this.workspace.project.id}/documents`, { title, type })
      .subscribe(document => {
        this.workspace!.documents = [document, ...this.workspace!.documents];
        this.select(document);
      });
  }

  otherDocuments() {
    return this.workspace?.documents.filter(d => !d.chapterId) ?? [];
  }

  // --- Story Atlas Management ---

  loadEntities() {
    if (this.workspace) {
      this.http
        .get<StoryEntity[]>(`/api/projects/${this.workspace.project.id}/entities`)
        .subscribe(items => {
          this.entities = items;
          if (this.selectedEntity) {
            const updated = items.find(x => x.id === this.selectedEntity?.id);
            if (updated) this.editEntity(updated);
          }
        });
    }
  }

  loadRelationships() {
    if (this.workspace) {
      this.http
        .get<StoryRelationship[]>(`/api/projects/${this.workspace.project.id}/relationships`)
        .subscribe(items => (this.relationships = items));
    }
  }

  filteredEntities(): StoryEntity[] {
    return this.entities.filter(entity => {
      const matchesFilter =
        this.atlasFilter === 'ALL' || entity.type.toUpperCase() === this.atlasFilter.toUpperCase();
      const matchesSearch =
        !this.atlasSearch ||
        entity.name.toLowerCase().includes(this.atlasSearch.toLowerCase()) ||
        entity.description.toLowerCase().includes(this.atlasSearch.toLowerCase()) ||
        (entity.attributes && entity.attributes.toLowerCase().includes(this.atlasSearch.toLowerCase()));
      return matchesFilter && matchesSearch;
    });
  }

  addEntity(type: string) {
    if (!this.workspace) return;
    const name = prompt(`${type[0] + type.slice(1).toLowerCase()} name`);
    if (!name?.trim()) return;
    this.http
      .post<StoryEntity>(`/api/projects/${this.workspace.project.id}/entities`, {
        type,
        name: name.trim(),
        description: '',
        status: 'DRAFT',
        sourceExcerpt: '',
        attributes: '{}'
      })
      .subscribe(entity => {
        this.entities = [entity, ...this.entities];
        this.editEntity(entity);
        this.panel = 'atlas';
      });
  }

  editEntity(entity: StoryEntity) {
    this.selectedEntity = { ...entity };
    this.parseAttributes(entity.attributes);
    this.panel = 'atlas';
  }

  parseAttributes(rawAttributes?: string) {
    this.entityAttributes = {
      role: '',
      appearance: '',
      personality: '',
      abilities: '',
      equipment: '',
      notes: ''
    };
    if (rawAttributes) {
      try {
        const parsed = JSON.parse(rawAttributes);
        this.entityAttributes = {
          role: parsed.role || '',
          appearance: parsed.appearance || '',
          personality: parsed.personality || '',
          abilities: parsed.abilities || '',
          equipment: parsed.equipment || '',
          notes: parsed.notes || ''
        };
      } catch (e) {
        // preserve unparsed content if any
      }
    }
  }

  saveEntity() {
    if (!this.workspace || !this.selectedEntity) return;
    const attributesJson = JSON.stringify(this.entityAttributes);
    const payload = {
      ...this.selectedEntity,
      attributes: attributesJson
    };
    this.http
      .put<StoryEntity>(
        `/api/projects/${this.workspace.project.id}/entities/${this.selectedEntity.id}`,
        payload
      )
      .subscribe(entity => {
        this.entities = this.entities.map(item => (item.id === entity.id ? entity : item));
        this.selectedEntity = { ...entity };
        this.parseAttributes(entity.attributes);
      });
  }

  deleteEntity(entity: StoryEntity) {
    if (!this.workspace) return;
    if (!confirm(`Delete story entry "${entity.name}"? Linked relationships and event links will be removed.`))
      return;
    this.http
      .delete(`/api/projects/${this.workspace.project.id}/entities/${entity.id}`)
      .subscribe(() => {
        this.entities = this.entities.filter(e => e.id !== entity.id);
        if (this.selectedEntity?.id === entity.id) {
          this.selectedEntity = undefined;
        }
        this.loadRelationships();
        this.loadTimeline();
      });
  }

  addRelationship() {
    if (!this.workspace || !this.selectedEntity || !this.relationshipTarget || !this.relationshipType.trim())
      return;
    this.http
      .post<StoryRelationship>(`/api/projects/${this.workspace.project.id}/relationships`, {
        sourceEntityId: this.selectedEntity.id,
        targetEntityId: this.relationshipTarget,
        relationshipType: this.relationshipType.trim(),
        status: this.relationshipStatus || 'CANON',
        description: '',
        sourceDocumentId: this.selectedEntity.sourceDocumentId
      })
      .subscribe(relationship => {
        this.relationships = [relationship, ...this.relationships];
        this.relationshipTarget = '';
        this.relationshipType = '';
      });
  }

  deleteRelationship(id: string) {
    if (!this.workspace) return;
    this.http
      .delete(`/api/projects/${this.workspace.project.id}/relationships/${id}`)
      .subscribe(() => {
        this.relationships = this.relationships.filter(r => r.id !== id);
      });
  }

  entityName(id: string) {
    return this.entities.find(entity => entity.id === id)?.name || 'Unknown entry';
  }

  relatedTo(entity: StoryEntity): StoryRelationship[] {
    return this.relationships.filter(
      r => r.sourceEntityId === entity.id || r.targetEntityId === entity.id
    );
  }

  partnerEntityName(relationship: StoryRelationship, currentEntity: StoryEntity): string {
    const partnerId =
      relationship.sourceEntityId === currentEntity.id
        ? relationship.targetEntityId
        : relationship.sourceEntityId;
    return this.entityName(partnerId);
  }

  documentTitle(docId?: string): string {
    if (!docId) return 'No source linked';
    const doc = this.workspace?.documents.find(d => d.id === docId);
    return doc ? doc.title : 'External source';
  }

  // --- Timeline Management ---

  loadTimeline() {
    if (this.workspace) {
      this.http
        .get<TimelineEvent[]>(`/api/projects/${this.workspace.project.id}/events`)
        .subscribe(items => (this.timelineEvents = items));
    }
  }

  eventsForEntity(entityId: string): TimelineEvent[] {
    return this.timelineEvents.filter(ev => ev.entityIds && ev.entityIds.includes(entityId));
  }

  openEventModal(event?: TimelineEvent) {
    this.editingEvent = event;
    if (event) {
      this.eventForm = {
        title: event.title,
        description: event.description,
        storyTime: event.storyTime,
        orderIndex: event.orderIndex,
        sourceDocumentId: event.sourceDocumentId,
        status: event.status,
        entityIds: [...(event.entityIds || [])]
      };
    } else {
      this.eventForm = {
        title: '',
        description: '',
        storyTime: '',
        orderIndex: this.timelineEvents.length + 1,
        sourceDocumentId: this.active?.id,
        status: 'CANON',
        entityIds: this.selectedEntity ? [this.selectedEntity.id] : []
      };
    }
    this.eventModalOpen = true;
  }

  closeEventModal() {
    this.eventModalOpen = false;
    this.editingEvent = undefined;
  }

  toggleEventEntity(entityId: string) {
    const idx = this.eventForm.entityIds.indexOf(entityId);
    if (idx >= 0) {
      this.eventForm.entityIds.splice(idx, 1);
    } else {
      this.eventForm.entityIds.push(entityId);
    }
  }

  isEntityLinkedToForm(entityId: string): boolean {
    return this.eventForm.entityIds.includes(entityId);
  }

  saveTimelineEvent() {
    if (!this.workspace || !this.eventForm.title.trim()) return;
    if (this.editingEvent) {
      this.http
        .put<TimelineEvent>(
          `/api/projects/${this.workspace.project.id}/events/${this.editingEvent.id}`,
          this.eventForm
        )
        .subscribe(updated => {
          this.timelineEvents = this.timelineEvents.map(e => (e.id === updated.id ? updated : e));
          this.closeEventModal();
        });
    } else {
      this.http
        .post<TimelineEvent>(
          `/api/projects/${this.workspace.project.id}/events`,
          this.eventForm
        )
        .subscribe(created => {
          this.timelineEvents = [...this.timelineEvents, created].sort(
            (a, b) => a.orderIndex - b.orderIndex
          );
          this.closeEventModal();
        });
    }
  }

  deleteTimelineEvent(id: string) {
    if (!this.workspace) return;
    if (!confirm('Delete this story event?')) return;
    this.http
      .delete(`/api/projects/${this.workspace.project.id}/events/${id}`)
      .subscribe(() => {
        this.timelineEvents = this.timelineEvents.filter(e => e.id !== id);
      });
  }

  jumpToEntity(entityId: string) {
    const entity = this.entities.find(e => e.id === entityId);
    if (entity) {
      this.editEntity(entity);
    }
  }

  // --- Document & Writing Operations ---

  queueSave() {
    if (this.saveTimer) clearTimeout(this.saveTimer);
    this.saving = true;
    this.saveTimer = setTimeout(() => this.save(), 700);
  }

  flushSave() {
    if (this.saveTimer) {
      clearTimeout(this.saveTimer);
      this.saveTimer = undefined;
      this.save();
    }
  }

  save() {
    if (!this.workspace || !this.active) return;
    this.saving = true;
    this.http
      .put<Document>(
        `/api/projects/${this.workspace.project.id}/documents/${this.active.id}`,
        { title: this.active.title, content: this.active.content }
      )
      .subscribe({
        next: d => {
          this.active = d;
          const i = this.workspace!.documents.findIndex(x => x.id === d.id);
          this.workspace!.documents[i] = d;
          this.saved =
            'Saved ' +
            new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          this.saving = false;
          this.history();
        },
        error: () => {
          this.saved = 'Could not save — retrying';
          this.saving = false;
        }
      });
  }

  history() {
    if (this.workspace && this.active) {
      this.http
        .get<Version[]>(
          `/api/projects/${this.workspace.project.id}/documents/${this.active.id}/versions`
        )
        .subscribe(v => (this.versions = v));
    }
  }

  captureSelection(event: Event) {
    const area = event.target as HTMLTextAreaElement;
    this.selectedText = area.value.slice(area.selectionStart, area.selectionEnd);
  }

  ask(action: string) {
    if (!this.workspace || !this.active) return;
    this.proposal = undefined;
    const context = this.selectedText || this.active.content.slice(-800);
    this.http
      .post<Proposal>(`/api/projects/${this.workspace.project.id}/ai/proposals`, {
        action,
        selectedText: context,
        instruction: ''
      })
      .subscribe(x => (this.proposal = x));
  }

  applyProposal() {
    if (!this.active || !this.proposal) return;
    const addition = this.proposal.content.replace(/^.*?:\n\n/, '');
    this.active.content = this.selectedText
      ? this.active.content.replace(this.selectedText, addition)
      : `${this.active.content}${this.active.content ? '\n\n' : ''}${addition}`;
    this.selectedText = '';
    this.proposal = undefined;
    this.queueSave();
  }

  restore(v: Version) {
    if (!this.active) return;
    this.active.content = v.content;
    this.save();
  }

  words() {
    return this.active?.content.trim() ? this.active.content.trim().split(/\s+/).length : 0;
  }

  @HostListener('window:keydown', ['$event'])
  shortcut(event: KeyboardEvent) {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
      event.preventDefault();
      this.flushSave();
    }
  }
}
