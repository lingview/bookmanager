-- SQLite 建表脚本，与 init.sql 保持同步
PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS user_information;
CREATE TABLE user_information (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL,
    username TEXT NOT NULL,
    avatar TEXT,
    phone TEXT,
    email TEXT,
    gender TEXT,
    password TEXT NOT NULL,
    birthday DATE,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    status INTEGER NOT NULL
);
CREATE UNIQUE INDEX uuid ON user_information (uuid);
CREATE UNIQUE INDEX username ON user_information (username);

INSERT INTO user_information VALUES (1, '075eb86f721743e3940f35869154a140175689381296899805858', 'admin', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A4%A9%E4%BE%9D/-1369486563.jpg?sign=7hdp5soxiEvVYpqvgiWxoJQ2UMJJCcoatm48bajEBkk=:0', NULL, 'official@astra.org', NULL, '$2a$10$hNfMxBf3egQkomuMql9LDeMJb2AC9IXkp904GgqX6DAxc8u9i1aAm', NULL, '2025-09-03 18:03:33', 1);

DROP TABLE IF EXISTS role;
CREATE TABLE role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1
);
CREATE UNIQUE INDEX idx_role_code ON role (code);

INSERT INTO role VALUES (1, 'ADMIN', '管理员', '系统超级管理员，拥有最高权限，可管理用户、角色、权限、站点配置等所有功能。', 1);

DROP TABLE IF EXISTS permission;
CREATE TABLE permission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    module TEXT NOT NULL,
    create_time DATETIME DEFAULT (datetime('now','localtime'))
);
CREATE UNIQUE INDEX idx_permission_code ON permission (code);

INSERT INTO permission VALUES (1, 'system:attachment:view', '查看任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (2, 'system:attachment:delete', '删除任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (3, 'system:attachment:undelete', '撤销删除任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (4, 'system:attachment:management', '系统附件管理完整操作', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (5, 'attachment:add', '添加附件', 'attachment', '2026-03-19 22:16:25');
INSERT INTO permission VALUES (6, 'attachment:view', '查看自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (7, 'attachment:delete', '删除自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (8, 'attachment:undelete', '撤销删除自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (9, 'attachment:edit', '管理自己附件（完整操作）', 'attachment', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (35, 'system:menus:management', '系统菜单管理', 'menu', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (37, 'system:role:management', '系统角色管理', 'role', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (38, 'system:config:management', '系统配置管理', 'config', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (41, 'system:update:management', '系统更新管理', 'update', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (42, 'system:user:management', '管理用户信息', 'user', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (43, 'content:menus', '内容管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (44, 'user:menus', '用户管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (54, 'menus:menus', '菜单管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (55, 'settings:menus', '系统设置菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (56, 'config:menus', '配置管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (57, 'update:menus', '系统更新菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO permission VALUES (59, 'role:menus', '角色管理菜单', 'menus', '2026-03-19 20:11:55');

DROP TABLE IF EXISTS dashboard_menu;
CREATE TABLE dashboard_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    icon TEXT,
    path TEXT,
    parent_id INTEGER,
    permission TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    type TEXT NOT NULL DEFAULT 'sidebar',
    FOREIGN KEY (parent_id) REFERENCES dashboard_menu (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX fk_menu_parent_id ON dashboard_menu (parent_id);

INSERT INTO dashboard_menu VALUES (1, '仪表盘', 'dashboard', '/dashboard', NULL, NULL, 10, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO dashboard_menu VALUES (2, '个人中心', 'user', '/dashboard/profile', NULL, NULL, 20, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO dashboard_menu VALUES (5, '设置', 'settings', NULL, NULL, 'settings:menus', 50, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO dashboard_menu VALUES (43, '站点信息', 'info', '/dashboard/settings', 5, 'config:menus', 10, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO dashboard_menu VALUES (46, '更新', 'update', '/dashboard/update', 5, 'update:menus', 30, '2025-12-04 21:42:15', 'sidebar');
INSERT INTO dashboard_menu VALUES (110, '角色编辑', 'permission', '/dashboard/rbac-editor', 5, 'role:menus', 20, '2026-03-19 20:10:22', 'sidebar');
INSERT INTO dashboard_menu VALUES (101, '个人中心', 'user', '/dashboard/profile', NULL, NULL, 10, '2025-09-13 11:12:42', 'quick_action');
INSERT INTO dashboard_menu VALUES (104, '用户', 'users', '/dashboard/users', NULL, 'user:menus', 30, '2025-09-13 11:12:42', 'quick_action');

DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES user_information (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX idx_role_id ON user_role (role_id);

INSERT INTO user_role VALUES (1, 1);

DROP TABLE IF EXISTS role_permission;
CREATE TABLE role_permission (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX idx_permission_id ON role_permission (permission_id);

INSERT INTO role_permission VALUES (1, 4);
INSERT INTO role_permission VALUES (1, 5);
INSERT INTO role_permission VALUES (1, 9);
INSERT INTO role_permission VALUES (1, 35);
INSERT INTO role_permission VALUES (1, 37);
INSERT INTO role_permission VALUES (1, 38);
INSERT INTO role_permission VALUES (1, 41);
INSERT INTO role_permission VALUES (1, 42);
INSERT INTO role_permission VALUES (1, 43);
INSERT INTO role_permission VALUES (1, 44);
INSERT INTO role_permission VALUES (1, 54);
INSERT INTO role_permission VALUES (1, 55);
INSERT INTO role_permission VALUES (1, 56);
INSERT INTO role_permission VALUES (1, 57);
INSERT INTO role_permission VALUES (1, 59);
INSERT INTO role_permission VALUES (2, 4);
INSERT INTO role_permission VALUES (2, 35);
INSERT INTO role_permission VALUES (2, 41);
INSERT INTO role_permission VALUES (2, 43);
INSERT INTO role_permission VALUES (2, 54);
INSERT INTO role_permission VALUES (2, 55);
INSERT INTO role_permission VALUES (2, 57);

DROP TABLE IF EXISTS attachment;
CREATE TABLE attachment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL,
    attachment_id TEXT NOT NULL,
    original_filename TEXT,
    attachment_path TEXT NOT NULL,
    access_key TEXT,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    deleted_time DATETIME,
    status INTEGER NOT NULL,
    FOREIGN KEY (uuid) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX attachment_id ON attachment (attachment_id);
CREATE UNIQUE INDEX attachment_path ON attachment (attachment_path);
CREATE UNIQUE INDEX access_key ON attachment (access_key);
CREATE INDEX idx_attachment_uuid ON attachment (uuid);

DROP TABLE IF EXISTS menus;
CREATE TABLE menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menus_id TEXT NOT NULL,
    user_id TEXT,
    menus_name TEXT NOT NULL,
    menus_url TEXT NOT NULL,
    sort_order INTEGER NOT NULL,
    status INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_information (uuid) ON DELETE SET NULL ON UPDATE SET NULL
);
CREATE UNIQUE INDEX menus_id ON menus (menus_id);
CREATE INDEX menus_user_id ON menus (user_id);

DROP TABLE IF EXISTS api_key;
CREATE TABLE api_key (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    key_hash TEXT NOT NULL,
    description TEXT,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    update_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    status INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES user_information (uuid) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX uk_key_hash ON api_key (key_hash);
CREATE INDEX idx_user_status ON api_key (user_id, status);

DROP TABLE IF EXISTS site_config;
CREATE TABLE site_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    site_name TEXT NOT NULL,
    register_user_permission INTEGER,
    copyright TEXT NOT NULL,
    hero_image TEXT NOT NULL,
    hero_title TEXT NOT NULL,
    hero_subtitle TEXT NOT NULL,
    site_icon TEXT NOT NULL,
    site_theme TEXT NOT NULL,
    enable_notification INTEGER DEFAULT 0,
    smtp_host TEXT,
    smtp_port INTEGER,
    mail_sender_email TEXT,
    mail_sender_name TEXT DEFAULT '系统通知',
    mail_username TEXT,
    mail_password TEXT,
    mail_protocol TEXT DEFAULT 'smtp',
    mail_enable_tls INTEGER DEFAULT 1,
    mail_enable_ssl INTEGER DEFAULT 0,
    mail_default_encoding TEXT DEFAULT 'UTF-8',
    icp_record_number TEXT,
    mps_record_number TEXT,
    enable_register INTEGER,
    enable_llm INTEGER,
    global_head_code TEXT,
    content_head_code TEXT,
    footer_code TEXT,
    enable_image_compression INTEGER DEFAULT 0,
    image_compression_threads INTEGER DEFAULT 5,
    proxy_resource_download INTEGER DEFAULT 0,
    FOREIGN KEY (register_user_permission) REFERENCES role (id) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX idx_site_register_user_permission ON site_config (register_user_permission);

INSERT INTO site_config VALUES (1, 'Astra Framework', NULL, '© Astra Framework. All rights reserved.', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A3%81%E7%BA%B8/mmexport1769432250836.jpg?sign=KFQ9u-fY83wR52PdfpesYywMJi_0M4Uc66vz7A9Tal0=:0', 'Welcome to Astra Framework', 'A modern, lightweight, and secure Java web application framework.', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A4%A9%E4%BE%9D/Image_1721230292906.png?sign=JU30z6z_RsZ3Vv7HB_5D3msYRneiga5NLjhN3EpL-3w=:0', 'default', 0, '', NULL, '', '系统通知', '', NULL, 'smtp', 0, 1, 'UTF-8', '', '', 1, 0, '', '', '', 0, 5, 0);

DROP TABLE IF EXISTS systematic_notification;
CREATE TABLE systematic_notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT,
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime')),
    type TEXT,
    status INTEGER NOT NULL,
    FOREIGN KEY (uuid) REFERENCES user_information (uuid) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX fk_systematic_notification_user_uuid ON systematic_notification (uuid);

DROP TABLE IF EXISTS book_category;
CREATE TABLE book_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT (datetime('now','localtime'))
);
CREATE UNIQUE INDEX uk_book_category_name ON book_category (name);

INSERT INTO book_category VALUES (1, '文学', 10, 1, datetime('now','localtime'));
INSERT INTO book_category VALUES (2, '计算机', 20, 1, datetime('now','localtime'));
INSERT INTO book_category VALUES (3, '历史', 30, 1, datetime('now','localtime'));

DROP TABLE IF EXISTS book;
CREATE TABLE book (
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
CREATE INDEX idx_book_category ON book (category_id);
CREATE INDEX idx_book_status ON book (status);
CREATE INDEX idx_book_title ON book (title);

DROP TABLE IF EXISTS book_chapter;
CREATE TABLE book_chapter (
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
CREATE INDEX idx_chapter_book ON book_chapter (book_id, status, sort_order);

DROP TABLE IF EXISTS book_reading_progress;
CREATE TABLE book_reading_progress (
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
CREATE INDEX idx_progress_book ON book_reading_progress (book_id);
CREATE INDEX idx_progress_chapter ON book_reading_progress (chapter_id);

INSERT INTO permission (code, name, module, create_time) VALUES ('book:management', '图书管理', 'book', datetime('now','localtime'));

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE code = 'book:management';

INSERT INTO dashboard_menu (title, icon, path, parent_id, permission, sort_order, type)
VALUES ('图书管理', 'book', '/books', NULL, 'book:management', 30, 'sidebar');

PRAGMA foreign_keys = ON;
