INSERT OR IGNORE INTO permission (code, name, module, create_time) VALUES
('book:upload', '图书上传（新增图书、导入正文）', 'book', CURRENT_TIMESTAMP),
('book:edit', '图书编辑（信息、封面、章节）', 'book', CURRENT_TIMESTAMP),
('book:delete', '图书删除', 'book', CURRENT_TIMESTAMP),
('book:batch', '图书批量操作', 'book', CURRENT_TIMESTAMP),
('book:cover', '图书封面管理', 'book', CURRENT_TIMESTAMP),
('category:manage', '分类管理', 'book', CURRENT_TIMESTAMP),
('user:view', '查看用户', 'user', CURRENT_TIMESTAMP),
('user:manage', '用户管理（删除、分配角色、注册设置）', 'user', CURRENT_TIMESTAMP),
('role:view', '查看角色', 'role', CURRENT_TIMESTAMP),
('role:manage', '角色与权限管理', 'role', CURRENT_TIMESTAMP);

UPDATE permission SET name = '分类管理菜单', module = 'menus' WHERE code = 'content:menus';
UPDATE permission SET name = '用户管理菜单', module = 'menus' WHERE code = 'user:menus';
UPDATE permission SET name = '角色管理菜单', module = 'menus' WHERE code = 'role:menus';

INSERT OR IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r JOIN permission p
WHERE r.code = 'ADMIN' AND p.code IN
('book:upload', 'book:edit', 'book:delete', 'book:batch', 'book:cover', 'category:manage',
 'user:view', 'user:manage', 'role:view', 'role:manage', 'user:menus', 'role:menus', 'content:menus');

INSERT OR IGNORE INTO role_permission (role_id, permission_id)
SELECT DISTINCT rp.role_id, np.id
FROM role_permission rp
JOIN permission op ON op.id = rp.permission_id
JOIN permission np
WHERE (op.code = 'book:management' AND np.code IN ('book:upload', 'book:edit', 'book:delete', 'book:batch', 'book:cover'))
   OR (op.code = 'system:user:management' AND np.code IN ('user:view', 'user:manage'))
   OR (op.code = 'system:role:management' AND np.code IN ('role:view', 'role:manage'))
   OR (op.code IN ('content:menus', 'book:management') AND np.code = 'category:manage');
