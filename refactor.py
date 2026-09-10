import re

html_lines = open('apps/web/src/app/app.component.html').readlines()

# Extract Studio Platform (Lines 14 to 844 approx, let's just find the indices)
studio_start = next(i for i, line in enumerate(html_lines) if '<main class="studio"' in line)
studio_end = next(i for i, line in enumerate(html_lines) if '<!-- READER PLATFORM -->' in line)

reader_start = next(i for i, line in enumerate(html_lines) if '<main class="reader-platform"' in line)
reader_end = next(i for i, line in enumerate(html_lines) if '</main>' in line and i > reader_start)

studio_html = "".join(html_lines[studio_start:studio_end])
reader_html = "".join(html_lines[reader_start:reader_end+1])
welcome_auth_html = "".join(html_lines[studio_end:reader_start]) # Actually we don't need this, we have auth

# State fields to prefix with state.
fields = [
    'workspace', 'user', 'viewMode', 'authMode', 'name', 'email', 'password', 'authError', 
    'projects', 'active', 'versions', 'proposal', 'saving', 'saved', 'panel', 'entities', 
    'relationships', 'selectedEntity', 'atlasFilter', 'atlasSearch', 'relationshipTarget', 
    'relationshipType', 'relationshipStatus', 'timelineEvents', 'editingEvent', 'eventModalOpen', 
    'eventForm', 'continuityNotices', 'checkingContinuity', 'memoryProposals', 'extractingMemory', 
    'visualReferences', 'scenes', 'panelsByScene', 'mangaPages', 'mangaAssetsByPage', 'activePage', 
    'selectedAsset', 'creatorProfile', 'projectPublishing', 'chapterRelease', 'chapterAnalytics', 
    'publicProjects', 'activeReaderProject', 'readerChapters', 'activeReaderChapter', 'readerPages', 
    'readerAssets', 'readerComments', 'bookmarkedProjects'
]

methods = [
    'authenticate', 'logout', 'createProject', 'openProject', 'loadActiveDocument', 
    'createDocument', 'queueSave', 'generateAIProposal', 'applyProposal', 'discardProposal',
    'createEntity', 'loadAtlas', 'deleteEntity', 'createRelationship', 'deleteRelationship',
    'createTimelineEvent', 'updateTimelineEvent', 'deleteTimelineEvent', 'openEventModal', 'closeEventModal',
    'isEntityLinkedToForm', 'toggleEntityLink', 'runContinuityCheck', 'applyContinuityFix', 'extractMemories',
    'acceptMemory', 'rejectMemory', 'requestVisualReference', 'proposePanels', 'createPage', 'addAsset',
    'selectAsset', 'updateAssetPos', 'publishChapter', 'toggleViewMode', 'loadDiscoverProjects',
    'loadBookmarks', 'toggleBookmark', 'isBookmarked', 'openReaderProject', 'readChapter', 'loadComments', 'addComment'
]

def add_state_prefix(html_content):
    # This is a basic regex approach; we will just replace exact words that aren't already prefixed or inside quotes as strings if they are variables.
    # Actually, a simpler way is to replace " field" with " state.field" in angular bindings, but there are many edges.
    # Let's do a naive replace for methods:
    for m in methods:
        html_content = re.sub(r'\b' + m + r'\(', 'state.' + m + '(', html_content)
    
    # For fields, we look for them in curly braces {{field}} or in bindings like *ngIf="field" or [class.active]="field === 'x'"
    # It's tricky to do a perfect AST replace in python. We'll do our best.
    for f in fields:
        # replace in {{ field... }}
        html_content = re.sub(r'\{\{\s*' + f + r'\b', '{{ state.' + f, html_content)
        # replace in *ngIf="field..."
        html_content = re.sub(r'\*ngIf="' + f + r'\b', '*ngIf="state.' + f, html_content)
        # replace in *ngFor="let x of field"
        html_content = re.sub(r'\*ngFor="let ([a-zA-Z0-9_]+) of ' + f + r'\b', r'*ngFor="let \1 of state.' + f, html_content)
        # replace in [hidden]="!field"
        html_content = re.sub(r'="!?' + f + r'\b', lambda match: '="' + match.group(0)[2:].replace(f, 'state.' + f) if match.group(0).startswith('="!') else '="state.' + f, html_content)
        # replace in (ngModel)="field"
        html_content = re.sub(r'\[\(ngModel\)\]="' + f + r'\b', '[(ngModel)]="state.' + f, html_content)
        # replace isolated field references in templates like *ngIf="!workspace"
        html_content = re.sub(r'\*ngIf="!' + f + r'\b', '*ngIf="!state.' + f, html_content)

    return html_content

open('apps/web/src/app/components/studio-platform/studio-platform.component.html', 'w').write(add_state_prefix(studio_html))
open('apps/web/src/app/components/reader-platform/reader-platform.component.html', 'w').write(add_state_prefix(reader_html))
print("Extraction complete")
