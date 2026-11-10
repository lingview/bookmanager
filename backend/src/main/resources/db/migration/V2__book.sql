CREATE TABLE IF NOT EXISTS `book_category` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(100) CHARACTER SET utf8mb4 NOT NULL COMMENT '分类名称',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_book_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '图书分类' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '书名',
    `author` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '作者',
    `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
    `cover_url` varchar(500) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '封面地址',
    `description` text CHARACTER SET utf8mb4 NULL COMMENT '简介',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_book_category`(`category_id` ASC) USING BTREE,
    INDEX `idx_book_status`(`status` ASC) USING BTREE,
    INDEX `idx_book_title`(`title` ASC) USING BTREE,
    CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `book_category` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '图书' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_chapter` (
    `id` int NOT NULL AUTO_INCREMENT,
    `book_id` int NOT NULL COMMENT '所属图书',
    `title` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '章节标题',
    `content` mediumtext CHARACTER SET utf8mb4 NOT NULL COMMENT '章节正文',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_chapter_book`(`book_id` ASC, `status` ASC, `sort_order` ASC) USING BTREE,
    CONSTRAINT `fk_chapter_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '图书章节' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `book_reading_progress` (
    `user_uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户uuid',
    `book_id` int NOT NULL COMMENT '图书ID',
    `chapter_id` int NOT NULL COMMENT '章节ID',
    `scroll_percent` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '阅读位置百分比',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_uuid`, `book_id`) USING BTREE,
    INDEX `idx_progress_book`(`book_id` ASC) USING BTREE,
    INDEX `idx_progress_chapter`(`chapter_id` ASC) USING BTREE,
    CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_progress_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_progress_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `book_chapter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '阅读进度' ROW_FORMAT = DYNAMIC;

INSERT IGNORE INTO `book_category` (`name`, `sort_order`) VALUES ('文学', 10), ('计算机', 20), ('历史', 30);

INSERT INTO `permission` (`code`, `name`, `module`, `create_time`)
SELECT 'book:management', '图书管理', 'book', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `permission` WHERE `code` = 'book:management');

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `permission` WHERE `code` = 'book:management';

INSERT INTO `dashboard_menu` (`title`, `icon`, `path`, `parent_id`, `permission`, `sort_order`, `type`)
SELECT '图书管理', 'book', '/books', NULL, 'book:management', 30, 'sidebar' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `dashboard_menu` WHERE `path` = '/books');
