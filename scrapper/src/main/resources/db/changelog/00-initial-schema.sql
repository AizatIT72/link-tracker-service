CREATE TABLE chats (
    id BIGINT PRIMARY KEY
);

CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    last_checked_at TIMESTAMPTZ,
    last_updated_at TIMESTAMPTZ
);

CREATE TABLE link_tags
(
    id BIGSERIAL PRIMARY KEY,
    link_id BIGINT NOT NULL REFERENCES links (id) ON DELETE CASCADE,
    tag TEXT NOT NULL
);


CREATE INDEX idx_links_chat_id ON links (chat_id);
CREATE INDEX idx_links_url ON links (url);
CREATE INDEX idx_link_tags_link_id ON link_tags (link_id);

ALTER TABLE links ADD CONSTRAINT unique_chat_url UNIQUE (chat_id, url);
