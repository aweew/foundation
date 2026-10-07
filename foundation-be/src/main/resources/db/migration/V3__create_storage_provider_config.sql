CREATE TABLE IF NOT EXISTS storage_provider_config
(
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    provider_code     VARCHAR(32)   NOT NULL,
    provider_name     VARCHAR(64)   NOT NULL,
    enabled           TINYINT       NOT NULL DEFAULT 1,
    is_default        TINYINT       NOT NULL DEFAULT 0,
    endpoint          VARCHAR(512),
    service_name      VARCHAR(128),
    access_domain     VARCHAR(512),
    base_path         VARCHAR(512),
    private_bucket    TINYINT       NOT NULL DEFAULT 1,
    credential_config VARCHAR(4000),
    extra_config      VARCHAR(4000),
    config_version    INT           NOT NULL DEFAULT 1,
    remark            VARCHAR(512),
    create_user_id    BIGINT,
    update_user_id    BIGINT,
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete         TINYINT       NOT NULL DEFAULT 0,
    version           INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_storage_provider_code (provider_code),
    KEY idx_storage_provider_default (is_default, enabled, is_delete)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '云存储厂商配置表';
