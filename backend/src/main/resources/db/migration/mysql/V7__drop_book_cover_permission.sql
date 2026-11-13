DELETE rp FROM `role_permission` rp
JOIN `permission` p ON p.id = rp.permission_id
WHERE p.code = 'book:cover';

DELETE FROM `permission` WHERE `code` = 'book:cover';
