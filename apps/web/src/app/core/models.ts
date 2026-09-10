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
export type ContinuityNotice = {
  id: string;
  type: string;
  severity: string;
  title: string;
  description: string;
  entityId?: string;
  relationshipId?: string;
  eventId?: string;
  documentId?: string;
  suggestedResolutions: string[];
};
export type MemoryProposal = {
  id: string;
  projectId: string;
  sourceDocumentId: string;
  targetType: string;
  name: string;
  suggestedType: string;
  suggestedDetails: string;
  sourceExcerpt: string;
  confidence: number;
  status: string;
  createdAt: string;
};
export type SemanticMemoryChunk = {
  id: string;
  projectId: string;
  sourceDocumentId?: string;
  chunkType: string;
  title: string;
  content: string;
  tags: string;
  authority: string;
};
export type Workspace = { project: Project; chapters: Chapter[]; documents: Document[] };
export type Version = { id: string; revision: number; content: string; createdAt: string };
export type Proposal = { content: string; authority: string; note: string };
export type VisualReference = {
  id: string;
  projectId: string;
  entityId: string;
  filePath: string;
  caption: string;
  type: string;
  status: string;
  createdAt: string;
};
export type Scene = {
  id: string;
  chapterId: string;
  orderIndex: number;
  description: string;
  createdAt: string;
};
export type PanelProposal = {
  id: string;
  sceneId: string;
  orderIndex: number;
  visualPrompt: string;
  dialogue: string;
  status: string;
  createdAt: string;
};
export type MangaPage = {
  id: string;
  chapterId: string;
  pageNumber: number;
  layoutType: string;
  status: string;
  createdAt: string;
};
export type MangaAsset = {
  id: string;
  pageId: string;
  assetType: string;
  fileUrl: string;
  posX: number;
  posY: number;
  width: number;
  height: number;
  zIndex: number;
  createdAt: string;
};
export type CreatorProfile = {
  penName: string;
  bio: string;
  avatarUrl: string;
};
export type ProjectPublishing = {
  visibility: string;
  tags: string;
};
export type ChapterRelease = {
  status: string;
  publishedAt?: string;
  scheduledFor?: string;
};
export type ChapterAnalytics = {
  views: number;
  likes: number;
  commentsCount: number;
};
export type ChapterComment = {
  id: string;
  chapterId: string;
  userId: string;
  content: string;
  createdAt: string;
};
export type ReaderBookmark = {
  userId: string;
  projectId: string;
  createdAt: string;
};
