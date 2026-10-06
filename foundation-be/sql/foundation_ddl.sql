CREATE DATABASE IF NOT EXISTS foundation
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE foundation;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    phone VARCHAR(32) NOT NULL COMMENT '电话',
    password VARCHAR(128) NOT NULL COMMENT '用户密码',
    nick_name VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    sex TINYINT DEFAULT NULL COMMENT '性别（1男，2女）',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
    register_time DATETIME DEFAULT NULL COMMENT '注册时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最近登录IP',
    last_login_time DATETIME DEFAULT NULL COMMENT '最近登录时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_status_delete (status, is_delete)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(64) NOT NULL COMMENT '角色编码',
    name VARCHAR(64) NOT NULL COMMENT '角色名称',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置角色',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
    remark VARCHAR(512) DEFAULT NULL COMMENT '角色描述',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_status_delete (status, is_delete)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '权限名称',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    code VARCHAR(128) NOT NULL COMMENT '权限编码',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限ID',
    type TINYINT NOT NULL COMMENT '权限类型（1菜单，2按钮，3页面）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
    level INT NOT NULL DEFAULT 1 COMMENT '层级',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
    url VARCHAR(512) DEFAULT NULL COMMENT '菜单或按钮链接地址',
    icon VARCHAR(128) DEFAULT NULL COMMENT '菜单或按钮图标',
    open_type TINYINT DEFAULT NULL COMMENT '打开类型（1当前页，2新标签）',
    path VARCHAR(512) DEFAULT NULL COMMENT '路由地址',
    component VARCHAR(512) DEFAULT NULL COMMENT '组件路径',
    query_param VARCHAR(1024) DEFAULT NULL COMMENT '路由参数',
    is_frame TINYINT NOT NULL DEFAULT 0 COMMENT '是否外链',
    is_cache TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存',
    is_visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (code),
    KEY idx_sys_menu_parent (parent_id),
    KEY idx_sys_menu_status_delete (status, is_delete)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限菜单表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role (role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '权限ID',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表';

INSERT IGNORE INTO sys_role (
    code,
    name,
    is_system,
    status,
    remark,
    create_time,
    update_time,
    is_delete
) VALUES (
    'SUPER_ADMIN',
    '超管',
    1,
    1,
    '系统超级管理员',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

INSERT IGNORE INTO sys_user (
    phone,
    password,
    nick_name,
    status,
    register_time,
    create_time,
    update_time,
    is_delete
) VALUES (
    '18949538661',
    '',
    '超管',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

INSERT IGNORE INTO sys_user_role (
    user_id,
    role_id,
    create_time,
    update_time,
    is_delete
)
SELECT t1.id,
       t2.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       0
FROM sys_user t1
         INNER JOIN sys_role t2 ON t2.code = 'SUPER_ADMIN'
WHERE t1.phone = '18949538661';
