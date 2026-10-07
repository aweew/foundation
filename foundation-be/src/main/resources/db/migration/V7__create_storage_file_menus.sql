INSERT IGNORE INTO sys_menu
(name, title, code, parent_id, type, status, level, sort, icon, path, component, is_frame, is_cache, is_visible, remark,
 create_time, update_time, is_delete)
VALUES
('storageFile', '文件管理', 'sys:storage:file', 0, 1, 1, 1, 60, 'Files', '/system/storage-file', 'system/StorageFileView',
 0, 0, 1, '云存储文件管理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT IGNORE INTO sys_menu
(name, title, code, parent_id, type, status, level, sort, is_frame, is_cache, is_visible, remark, create_time, update_time, is_delete)
SELECT 'storageFileList', '文件列表', 'sys:storage:file:list', t1.id, 2, 1, 2, 1, 0, 0, 1, '文件管理查询权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM sys_menu t1
WHERE t1.code = 'sys:storage:file';

INSERT IGNORE INTO sys_menu
(name, title, code, parent_id, type, status, level, sort, is_frame, is_cache, is_visible, remark, create_time, update_time, is_delete)
SELECT 'storageFileView', '文件详情', 'sys:storage:file:view', t1.id, 2, 1, 2, 2, 0, 0, 1, '文件详情查询权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM sys_menu t1
WHERE t1.code = 'sys:storage:file';

INSERT IGNORE INTO sys_menu
(name, title, code, parent_id, type, status, level, sort, is_frame, is_cache, is_visible, remark, create_time, update_time, is_delete)
SELECT 'storageFileDelete', '文件删除', 'sys:storage:file:delete', t1.id, 2, 1, 2, 3, 0, 0, 1, '文件删除权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM sys_menu t1
WHERE t1.code = 'sys:storage:file';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id, create_time, update_time, is_delete)
SELECT t1.id, t2.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM sys_role t1
INNER JOIN sys_menu t2 ON t2.code IN ('sys:storage:file', 'sys:storage:file:list', 'sys:storage:file:view', 'sys:storage:file:delete')
WHERE t1.code = 'SUPER_ADMIN';
