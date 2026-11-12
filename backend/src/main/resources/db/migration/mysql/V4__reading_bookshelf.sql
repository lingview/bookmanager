ALTER TABLE `book`
    ADD COLUMN `isbn` varchar(32) CHARACTER SET utf8mb4 NULL COMMENT 'ISBN' AFTER `description`,
    ADD COLUMN `publisher` varchar(100) CHARACTER SET utf8mb4 NULL COMMENT '出版社' AFTER `isbn`,
    ADD COLUMN `publish_year` int NULL COMMENT '出版年' AFTER `publisher`,
    ADD COLUMN `source_url` varchar(500) CHARACTER SET utf8mb4 NULL COMMENT '来源链接' AFTER `publish_year`;

CREATE TABLE IF NOT EXISTS `book_user_shelf` (
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `status` varchar(16) CHARACTER SET utf8mb4 NOT NULL COMMENT 'want想读，reading在读，finished读过',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_uuid`, `book_id`) USING BTREE,
    INDEX `idx_shelf_book`(`book_id` ASC) USING BTREE,
    INDEX `idx_shelf_status`(`user_uuid` ASC, `status` ASC) USING BTREE,
    CONSTRAINT `fk_shelf_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_shelf_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '用户书架状态' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_user_review` (
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `rating` tinyint NULL COMMENT '评分1-5',
    `review` varchar(1000) CHARACTER SET utf8mb4 NULL COMMENT '短评',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_uuid`, `book_id`) USING BTREE,
    CONSTRAINT `fk_review_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_review_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '用户评分短评' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_bookmark` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `chapter_id` int NOT NULL COMMENT '章节ID',
    `scroll_percent` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '章节内位置百分比',
    `note` varchar(200) CHARACTER SET utf8mb4 NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_bookmark_user_book`(`user_uuid` ASC, `book_id` ASC) USING BTREE,
    CONSTRAINT `fk_bookmark_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_bookmark_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_bookmark_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `book_chapter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '阅读书签' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_reading_goal` (
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `target_year` int NOT NULL COMMENT '目标年份',
    `target_count` int NOT NULL COMMENT '目标本数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_uuid`, `target_year`) USING BTREE,
    CONSTRAINT `fk_goal_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '年度阅读目标' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_tag` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '标签名',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_book_tag_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '图书标签' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_tag_rel` (
    `book_id` int NOT NULL COMMENT '图书ID',
    `tag_id` int NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`book_id`, `tag_id`) USING BTREE,
    INDEX `idx_tag_rel_tag`(`tag_id` ASC) USING BTREE,
    CONSTRAINT `fk_tag_rel_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tag_rel_tag` FOREIGN KEY (`tag_id`) REFERENCES `book_tag` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '图书标签关联' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_note` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `chapter_id` int NOT NULL COMMENT '章节ID',
    `quote_text` varchar(2000) CHARACTER SET utf8mb4 NULL COMMENT '划线文本',
    `note_text` varchar(2000) CHARACTER SET utf8mb4 NULL COMMENT '个人想法',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_note_user_book`(`user_uuid` ASC, `book_id` ASC) USING BTREE,
    CONSTRAINT `fk_note_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `book_chapter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '阅读划线笔记' ROW_FORMAT = DYNAMIC;
