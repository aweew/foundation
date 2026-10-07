CREATE TABLE IF NOT EXISTS sys_user
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    phone           VARCHAR(32)  NOT NULL,
    password        VARCHAR(128) NOT NULL,
    nick_name       VARCHAR(64),
    real_name       VARCHAR(64),
    sex             TINYINT,
    email           VARCHAR(128),
    avatar          VARCHAR(512),
    register_time   DATETIME,
    last_login_ip   VARCHAR(64),
    last_login_time DATETIME,
    status          TINYINT      NOT NULL DEFAULT 1,
    remark          VARCHAR(512),
    create_user_id  BIGINT,
    update_user_id  BIGINT,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_status_delete (status, is_delete)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表';

CREATE TABLE IF NOT EXISTS sys_role
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    code           VARCHAR(64) NOT NULL,
    name           VARCHAR(64) NOT NULL,
    is_system      TINYINT     NOT NULL DEFAULT 0,
    status         TINYINT     NOT NULL DEFAULT 1,
    remark         VARCHAR(512),
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete      TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_status_delete (status, is_delete)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表';

CREATE TABLE IF NOT EXISTS sys_menu
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(64)  NOT NULL,
    title          VARCHAR(128) NOT NULL,
    code           VARCHAR(128) NOT NULL,
    parent_id      BIGINT       NOT NULL DEFAULT 0,
    type           TINYINT      NOT NULL,
    status         TINYINT      NOT NULL DEFAULT 1,
    level          INT          NOT NULL DEFAULT 1,
    sort           INT          NOT NULL DEFAULT 0,
    url            VARCHAR(512),
    icon           VARCHAR(128),
    open_type      TINYINT,
    path           VARCHAR(512),
    component      VARCHAR(512),
    query_param    VARCHAR(1024),
    is_frame       TINYINT      NOT NULL DEFAULT 0,
    is_cache       TINYINT      NOT NULL DEFAULT 0,
    is_visible     TINYINT      NOT NULL DEFAULT 1,
    remark         VARCHAR(512),
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (code),
    KEY idx_sys_menu_parent (parent_id),
    KEY idx_sys_menu_status_delete (status, is_delete)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限菜单表';

CREATE TABLE IF NOT EXISTS sys_user_role
(
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    user_id        BIGINT   NOT NULL,
    role_id        BIGINT   NOT NULL,
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete      TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu
(
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    role_id        BIGINT   NOT NULL,
    menu_id        BIGINT   NOT NULL,
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete      TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表';

CREATE TABLE IF NOT EXISTS sys_operation_log
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT,
    username        VARCHAR(128),
    log_type        VARCHAR(32) NOT NULL,
    operation_name  VARCHAR(128),
    request_method  VARCHAR(16),
    request_path    VARCHAR(512),
    request_ip      VARCHAR(64),
    client_type     VARCHAR(64),
    user_agent      VARCHAR(512),
    request_params  VARCHAR(4000),
    response_status INT,
    result_code     INT,
    result_message  VARCHAR(512),
    duration_ms     BIGINT,
    trace_id        VARCHAR(128),
    error_type      VARCHAR(256),
    error_message   VARCHAR(1000),
    archived        TINYINT     NOT NULL DEFAULT 0,
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_time   DATETIME,
    PRIMARY KEY (id),
    KEY idx_sys_operation_log_user_time (user_id, create_time),
    KEY idx_sys_operation_log_type_time (log_type, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统操作审计日志表';

INSERT IGNORE INTO sys_role (code, name, is_system, status, remark, create_time, update_time, is_delete)
VALUES ('SUPER_ADMIN', '超管', 1, 1, '系统超级管理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT IGNORE INTO sys_user (phone, password, nick_name, status, register_time, create_time, update_time, is_delete)
VALUES ('18949538661', '$2a$10$dUjVmWGCuGe0UMNjla4zqOjMZGkdctxXMPYF3hXaPF9Eh.lDZ7wPa', '超管', 1,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time, update_time, is_delete)
SELECT t1.id, t2.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM sys_user t1
     INNER JOIN sys_role t2 ON t2.code = 'SUPER_ADMIN'
WHERE t1.phone = '18949538661';
