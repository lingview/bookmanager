ALTER TABLE `book`
    ADD COLUMN `pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：1是，0否' AFTER `status`,
    ADD COLUMN `pin_time` datetime NULL COMMENT '置顶时间' AFTER `pinned`;
