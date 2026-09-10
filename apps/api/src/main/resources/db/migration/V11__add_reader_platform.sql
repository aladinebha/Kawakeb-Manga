CREATE TABLE chapter_comment (
    id UUID PRIMARY KEY,
    chapter_id UUID NOT NULL REFERENCES chapter(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE reader_bookmark (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, project_id)
);

CREATE TABLE reader_history (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    chapter_id UUID NOT NULL REFERENCES chapter(id) ON DELETE CASCADE,
    last_read_page INTEGER NOT NULL DEFAULT 1,
    read_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, chapter_id)
);
