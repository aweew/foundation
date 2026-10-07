CREATE TABLE IF NOT EXISTS storage_file
(
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    provider_code     VARCHAR(32)   NOT NULL,
    provider_config_id BIGINT       NOT NULL,
    original_name     VARCHAR(255)  NOT NULL,
    object_key        VARCHAR(512)  NOT NULL,
    content_type      VARCHAR(128),
    file_size         BIGINT        NOT NULL,
    extension         VARCHAR(32),
    business_type     VARCHAR(64),
    business_id       VARCHAR(64),
    status            VARCHAR(16)   NOT NULL,
    create_user_id    BIGINT,
    update_user_id    BIGINT,
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_storage_file_object_key (object_key),
    KEY idx_storage_file_business (business_type, business_id, is_delete)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '云存储文件元数据表';
