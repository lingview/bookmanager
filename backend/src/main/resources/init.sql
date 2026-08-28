/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : astra_framework

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 13/07/2026 12:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_information
-- ----------------------------
DROP TABLE IF EXISTS `user_information`;
CREATE TABLE `user_information`  (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '用户创建顺序',
                                     `uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '唯一用户id',
                                     `username` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户名',
                                     `avatar` text CHARACTER SET utf8mb4 NULL COMMENT '用户头像',
                                     `phone` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '手机号',
                                     `email` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '邮箱',
                                     `gender` enum('male','female','other') CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '性别',
                                     `password` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '密码',
                                     `birthday` date NULL DEFAULT NULL COMMENT '生日',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户创建时间',
                                     `status` tinyint NOT NULL COMMENT '用户状态：0=删除, 1=正常, 2=封禁',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uuid`(`uuid` ASC) USING BTREE,
                                     UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_information
-- ----------------------------
INSERT INTO `user_information` VALUES (1, '075eb86f721743e3940f35869154a140175689381296899805858', 'admin', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A4%A9%E4%BE%9D/-1369486563.jpg?sign=7hdp5soxiEvVYpqvgiWxoJQ2UMJJCcoatm48bajEBkk=:0', NULL, 'official@astra.org', NULL, '$2a$10$hNfMxBf3egQkomuMql9LDeMJb2AC9IXkp904GgqX6DAxc8u9i1aAm', NULL, '2025-09-03 18:03:33', 1);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `code` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '角色编码，如 ADMIN',
                         `name` varchar(100) CHARACTER SET utf8mb4 NOT NULL COMMENT '角色名称',
                         `description` text CHARACTER SET utf8mb4 NULL COMMENT '描述',
                         `status` tinyint NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'ADMIN', '管理员', '系统超级管理员，拥有最高权限，可管理用户、角色、权限、站点配置等所有功能。', 1);

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
                               `id` int NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                               `code` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '权限码，如 post:view',
                               `name` varchar(100) CHARACTER SET utf8mb4 NOT NULL COMMENT '权限名称，如 查看文章',
                               `module` varchar(50) CHARACTER SET utf8mb4 NOT NULL COMMENT '所属模块，如 post, user, system',
                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 'system:attachment:view', '查看任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (2, 'system:attachment:delete', '删除任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (3, 'system:attachment:undelete', '撤销删除任意附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (4, 'system:attachment:management', '系统附件管理完整操作', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (5, 'attachment:add', '添加附件', 'attachment', '2026-03-19 22:16:25');
INSERT INTO `permission` VALUES (6, 'attachment:view', '查看自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (7, 'attachment:delete', '删除自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (8, 'attachment:undelete', '撤销删除自己附件', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (9, 'attachment:edit', '管理自己附件（完整操作）', 'attachment', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (35, 'system:menus:management', '系统菜单管理', 'menu', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (37, 'system:role:management', '系统角色管理', 'role', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (38, 'system:config:management', '系统配置管理', 'config', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (41, 'system:update:management', '系统更新管理', 'update', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (42, 'system:user:management', '管理用户信息', 'user', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (43, 'content:menus', '内容管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (44, 'user:menus', '用户管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (54, 'menus:menus', '菜单管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (55, 'settings:menus', '系统设置菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (56, 'config:menus', '配置管理菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (57, 'update:menus', '系统更新菜单', 'menus', '2025-08-27 09:22:27');
INSERT INTO `permission` VALUES (59, 'role:menus', '角色管理菜单', 'menus', '2026-03-19 20:11:55');

-- ----------------------------
-- Table structure for dashboard_menu
-- ----------------------------
DROP TABLE IF EXISTS `dashboard_menu`;
CREATE TABLE `dashboard_menu`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '菜单标题',
  `icon` varchar(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '菜单图标',
  `path` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '菜单路径',
  `parent_id` int NULL DEFAULT NULL COMMENT '父菜单ID',
  `permission` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '权限码',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `type` enum('sidebar','quick_action') CHARACTER SET utf8mb4 NOT NULL DEFAULT 'sidebar' COMMENT '菜单类型',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_menu_parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `fk_menu_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `dashboard_menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dashboard_menu
-- ----------------------------
INSERT INTO `dashboard_menu` VALUES (1, '仪表盘', 'dashboard', '/dashboard', NULL, NULL, 10, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (2, '个人中心', 'user', '/dashboard/profile', NULL, NULL, 20, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (5, '设置', 'settings', NULL, NULL, 'settings:menus', 50, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (43, '站点信息', 'info', '/dashboard/settings', 5, 'config:menus', 10, '2025-09-13 11:12:42', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (46, '更新', 'update', '/dashboard/update', 5, 'update:menus', 30, '2025-12-04 21:42:15', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (110, '角色编辑', 'permission', '/dashboard/rbac-editor', 5, 'role:menus', 20, '2026-03-19 20:10:22', 'sidebar');
INSERT INTO `dashboard_menu` VALUES (101, '个人中心', 'user', '/dashboard/profile', NULL, NULL, 10, '2025-09-13 11:12:42', 'quick_action');
INSERT INTO `dashboard_menu` VALUES (104, '用户', 'users', '/dashboard/users', NULL, 'user:menus', 30, '2025-09-13 11:12:42', 'quick_action');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
                              `user_id` int NOT NULL COMMENT '用户 ID',
                              `role_id` int NOT NULL COMMENT '角色 ID',
                              PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
                              INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
                              CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                              CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `user_information` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1);

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
                                    `role_id` int NOT NULL COMMENT '角色ID',
                                    `permission_id` int NOT NULL COMMENT '权限ID',
                                    PRIMARY KEY (`role_id`, `permission_id`) USING BTREE,
                                    INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE,
                                    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 4);
INSERT INTO `role_permission` VALUES (1, 5);
INSERT INTO `role_permission` VALUES (1, 9);
INSERT INTO `role_permission` VALUES (1, 35);
INSERT INTO `role_permission` VALUES (1, 37);
INSERT INTO `role_permission` VALUES (1, 38);
INSERT INTO `role_permission` VALUES (1, 41);
INSERT INTO `role_permission` VALUES (1, 42);
INSERT INTO `role_permission` VALUES (1, 43);
INSERT INTO `role_permission` VALUES (1, 44);
INSERT INTO `role_permission` VALUES (1, 54);
INSERT INTO `role_permission` VALUES (1, 55);
INSERT INTO `role_permission` VALUES (1, 56);
INSERT INTO `role_permission` VALUES (1, 57);
INSERT INTO `role_permission` VALUES (1, 59);
INSERT INTO `role_permission` VALUES (2, 4);
INSERT INTO `role_permission` VALUES (2, 35);
INSERT INTO `role_permission` VALUES (2, 41);
INSERT INTO `role_permission` VALUES (2, 43);
INSERT INTO `role_permission` VALUES (2, 54);
INSERT INTO `role_permission` VALUES (2, 55);
INSERT INTO `role_permission` VALUES (2, 57);

-- ----------------------------
-- Table structure for attachment
-- ----------------------------
DROP TABLE IF EXISTS `attachment`;
CREATE TABLE `attachment`  (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '上传顺序',
    `uuid` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户唯一id',
    `attachment_id` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '附件唯一id',
    `original_filename` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '原始文件名',
    `attachment_path` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '附件路径',
    `access_key` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '文件访问键',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `status` tinyint NOT NULL COMMENT '附件状态：0=软删除, 1=正常, 2=物理文件已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `attachment_id`(`attachment_id` ASC) USING BTREE,
    UNIQUE INDEX `attachment_path`(`attachment_path` ASC) USING BTREE,
    UNIQUE INDEX `access_key`(`access_key` ASC) USING BTREE,
    INDEX `idx_uuid`(`uuid` ASC) USING BTREE,
    CONSTRAINT `fk_attachment_user` FOREIGN KEY (`uuid`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '附件上传记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of attachment
-- ----------------------------

-- ----------------------------
-- Table structure for menus
-- ----------------------------
DROP TABLE IF EXISTS `menus`;
CREATE TABLE `menus`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `menus_id` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '菜单id',
  `user_id` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '创建该菜单的用户',
  `menus_name` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '菜单名称',
  `menus_url` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '菜单url',
  `sort_order` int NOT NULL COMMENT '菜单排序',
  `status` int NOT NULL COMMENT '0为删除、1为正常',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `menus_id`(`menus_id` ASC) USING BTREE,
  INDEX `menus_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `menus_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_information` (`uuid`) ON DELETE SET NULL ON UPDATE SET NULL
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menus
-- ----------------------------

-- ----------------------------
-- Table structure for api_key
-- ----------------------------
DROP TABLE IF EXISTS `api_key`;
CREATE TABLE `api_key`  (
                            `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
                            `user_id` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '所属用户UUID',
                            `key_hash` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT 'SHA-256哈希，替代原api_key明文',
                            `description` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '备注描述',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `status` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uk_key_hash`(`key_hash` ASC) USING BTREE,
                            INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
                            CONSTRAINT `api_key_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_information` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = 'API Key表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of api_key
-- ----------------------------

-- ----------------------------
-- Table structure for site_config
-- ----------------------------
DROP TABLE IF EXISTS `site_config`;
CREATE TABLE `site_config`  (
                                `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `site_name` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '站点名称',
                                `register_user_permission` int NULL DEFAULT NULL COMMENT '注册用户默认角色id',
                                `copyright` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '版权信息',
                                `hero_image` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '首页头图',
                                `hero_title` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '首页标题',
                                `hero_subtitle` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '首页副标题',
                                `site_icon` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '站点图标',
                                `site_theme` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '站点主题',
                                `enable_notification` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用通知系统（1：启用，0：禁用）',
                                `smtp_host` varchar(100) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT 'SMTP服务器地址',
                                `smtp_port` int NULL DEFAULT NULL COMMENT 'SMTP端口',
                                `mail_sender_email` varchar(100) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '发件人邮箱',
                                `mail_sender_name` varchar(50) CHARACTER SET utf8mb4 NULL DEFAULT '系统通知' COMMENT '发件人名称',
                                `mail_username` varchar(100) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '邮箱登录账号',
                                `mail_password` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '邮箱授权码（应加密存储）',
                                `mail_protocol` varchar(10) CHARACTER SET utf8mb4 NULL DEFAULT 'smtp' COMMENT '协议类型：smtp/smtps',
                                `mail_enable_tls` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用TLS',
                                `mail_enable_ssl` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用SSL',
                                `mail_default_encoding` varchar(10) CHARACTER SET utf8mb4 NULL DEFAULT 'UTF-8' COMMENT '编码格式',
                                `icp_record_number` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT 'ICP备案号',
                                `mps_record_number` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '公安联网备案号',
                                `enable_register` int NULL DEFAULT NULL COMMENT '是否启用用户注册（1：启用，0：禁用）',
                                `enable_llm` int NULL DEFAULT NULL COMMENT '是否启用系统大模型接入（1：启用，0：禁用）',
                                `global_head_code` mediumtext CHARACTER SET utf8mb4 NULL COMMENT '全局Head标签代码注入',
                                `content_head_code` mediumtext CHARACTER SET utf8mb4 NULL COMMENT '内容页Head标签代码注入',
                                `footer_code` mediumtext CHARACTER SET utf8mb4 NULL COMMENT '页脚代码注入',
                                `enable_image_compression` int NULL DEFAULT 0 COMMENT '是否启用图片压缩（1：启用，0：禁用）',
                                `image_compression_threads` int NULL DEFAULT 5 COMMENT '图片压缩处理线程数',
                                `proxy_resource_download` int NULL DEFAULT 0 COMMENT '是否启用代理资源下载（1：启用，0：禁用）',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `register_user_permission`(`register_user_permission` ASC) USING BTREE,
                                CONSTRAINT `register_user_permission` FOREIGN KEY (`register_user_permission`) REFERENCES `role` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COMMENT = '站点基础设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of site_config
-- ----------------------------
INSERT INTO `site_config` VALUES (1, 'Astra Framework', NULL, '© Astra Framework. All rights reserved.', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A3%81%E7%BA%B8/mmexport1769432250836.jpg?sign=KFQ9u-fY83wR52PdfpesYywMJi_0M4Uc66vz7A9Tal0=:0', 'Welcome to Astra Framework', 'A modern, lightweight, and secure Java web application framework.', 'https://pan.lingview.xyz/d/%E9%9B%A8%E4%BA%91%E8%8A%82%E7%82%B9/%E5%9B%BE%E5%BA%93/%E5%A4%A9%E4%BE%9D/Image_1721230292906.png?sign=JU30z6z_RsZ3Vv7HB_5D3msYRneiga5NLjhN3EpL-3w=:0', 'default', 0, '', NULL, '', '系统通知', '', NULL, 'smtp', 0, 1, 'UTF-8', '', '', 1, 0, '', '', '', 0, 5, 0);

-- ----------------------------
-- Table structure for systematic_notification
-- ----------------------------
DROP TABLE IF EXISTS `systematic_notification`;
CREATE TABLE `systematic_notification`  (
                                            `id` int NOT NULL AUTO_INCREMENT,
                                            `uuid` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '所属用户的uuid',
                                            `content` mediumtext CHARACTER SET utf8mb4 NOT NULL COMMENT '通知内容',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `type` varchar(50) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '通知类型',
                                            `status` int NOT NULL COMMENT '通知状态2为未读，1为已读，0为删除',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            INDEX `fk_systematic_notification_user_uuid`(`uuid` ASC) USING BTREE,
                                            CONSTRAINT `fk_systematic_notification_user_uuid` FOREIGN KEY (`uuid`) REFERENCES `user_information` (`uuid`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of systematic_notification
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;