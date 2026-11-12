ALTER TABLE book ADD COLUMN isbn TEXT;
ALTER TABLE book ADD COLUMN publisher TEXT;
ALTER TABLE book ADD COLUMN publish_year INTEGER;
ALTER TABLE book ADD COLUMN source_url TEXT;

CREATE TABLE IF NOT EXISTS book_user_shelf (
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    status TEXT NOT NULL,
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (user_uuid, book_id),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_shelf_book ON book_user_shelf (book_id);
CREATE INDEX IF NOT EXISTS idx_shelf_status ON book_user_shelf (user_uuid, status);

CREATE TABLE IF NOT EXISTS book_user_review (
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    rating INTEGER,
    review TEXT,
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (user_uuid, book_id),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS book_bookmark (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    scroll_percent NUMERIC NOT NULL DEFAULT 0.00,
    note TEXT,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES book_chapter (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bookmark_user_book ON book_bookmark (user_uuid, book_id);

CREATE TABLE IF NOT EXISTS book_reading_goal (
    user_uuid TEXT NOT NULL,
    target_year INTEGER NOT NULL,
    target_count INTEGER NOT NULL,
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (user_uuid, target_year),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS book_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime'))
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_book_tag_name ON book_tag (name);

CREATE TABLE IF NOT EXISTS book_tag_rel (
    book_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (book_id, tag_id),
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES book_tag (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_tag_rel_tag ON book_tag_rel (tag_id);

CREATE TABLE IF NOT EXISTS book_note (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    quote_text TEXT,
    note_text TEXT,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES book_chapter (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_note_user_book ON book_note (user_uuid, book_id);
