CREATE TABLE IF NOT EXISTS book_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime'))
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_book_category_name ON book_category (name);

CREATE TABLE IF NOT EXISTS book (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT,
    category_id INTEGER,
    cover_url TEXT,
    description TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (category_id) REFERENCES book_category (id) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_book_category ON book (category_id);
CREATE INDEX IF NOT EXISTS idx_book_status ON book (status);
CREATE INDEX IF NOT EXISTS idx_book_title ON book (title);

CREATE TABLE IF NOT EXISTS book_chapter (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_chapter_book ON book_chapter (book_id, status, sort_order);

CREATE TABLE IF NOT EXISTS book_reading_progress (
    user_uuid TEXT NOT NULL,
    book_id INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    scroll_percent NUMERIC NOT NULL DEFAULT 0.00,
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (user_uuid, book_id),
    FOREIGN KEY (user_uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES book_chapter (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_progress_book ON book_reading_progress (book_id);
CREATE INDEX IF NOT EXISTS idx_progress_chapter ON book_reading_progress (chapter_id);

INSERT OR IGNORE INTO book_category (name, sort_order) VALUES ('文学', 10), ('计算机', 20), ('历史', 30);

INSERT INTO permission (code, name, module, create_time)
SELECT 'book:management', '图书管理', 'book', datetime('now','localtime')
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE code = 'book:management');

INSERT OR IGNORE INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE code = 'book:management';

INSERT INTO dashboard_menu (title, icon, path, parent_id, permission, sort_order, type)
SELECT '图书管理', 'book', '/books', NULL, 'book:management', 30, 'sidebar'
WHERE NOT EXISTS (SELECT 1 FROM dashboard_menu WHERE path = '/books');
