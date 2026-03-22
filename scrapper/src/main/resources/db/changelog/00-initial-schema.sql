CREATE TABLE IF NOT EXISTS chats (
                                     id BIGINT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS tracked_links (
                                             id SERIAL PRIMARY KEY,
                                             chat_id BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    tags TEXT[] DEFAULT '{}',
    filters TEXT[] DEFAULT '{}',
    last_checked_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (chat_id, url)
    );
