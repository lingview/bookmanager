DELETE FROM `role_permission`
WHERE `permission_id` IN (SELECT `id` FROM `permission` WHERE `code` = 'book:cover');

DELETE FROM `permission` WHERE `code` = 'book:cover';
