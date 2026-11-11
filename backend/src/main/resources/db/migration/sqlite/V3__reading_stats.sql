ALTER TABLE book_chapter ADD COLUMN word_count INTEGER NOT NULL DEFAULT 0;
UPDATE book_chapter SET word_count = length(content);

CREATE TABLE IF NOT EXISTS book_reading_time (
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    stat_date DATE NOT NULL,
    seconds INTEGER NOT NULL DEFAULT 0,
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (user_uuid, book_id, stat_date),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_reading_time_book ON book_reading_time (book_id, stat_date);
CREATE INDEX IF NOT EXISTS idx_reading_time_date ON book_reading_time (stat_date);

CREATE TABLE IF NOT EXISTS book_reading_event (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    event_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_event_book_chapter ON book_reading_event (book_id, chapter_id);
CREATE INDEX IF NOT EXISTS idx_event_user_book ON book_reading_event (user_uuid, book_id);
CREATE INDEX IF NOT EXISTS idx_event_time ON book_reading_event (event_time);
