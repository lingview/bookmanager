ALTER TABLE `book_chapter` ADD COLUMN `word_count` int NOT NULL DEFAULT 0 COMMENT '正文字数' AFTER `content`;
UPDATE `book_chapter` SET `word_count` = CHAR_LENGTH(`content`);

CREATE TABLE IF NOT EXISTS `book_reading_time` (
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `seconds` int NOT NULL DEFAULT 0 COMMENT '当天阅读秒数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_uuid`, `book_id`, `stat_date`) USING BTREE,
    INDEX `idx_reading_time_book`(`book_id` ASC, `stat_date` ASC) USING BTREE,
    INDEX `idx_reading_time_date`(`stat_date` ASC) USING BTREE,
    CONSTRAINT `fk_reading_time_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reading_time_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '阅读时长统计' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_reading_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `chapter_id` int NOT NULL COMMENT '章节ID',
    `event_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打开时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_event_book_chapter`(`book_id` ASC, `chapter_id` ASC) USING BTREE,
    INDEX `idx_event_user_book`(`user_uuid` ASC, `book_id` ASC) USING BTREE,
    INDEX `idx_event_time`(`event_time` ASC) USING BTREE,
    CONSTRAINT `fk_event_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_event_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '阅读章节流水' ROW_FORMAT = DYNAMIC;
