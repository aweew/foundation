ALTER TABLE storage_provider_config
    ADD COLUMN region VARCHAR(64) NULL COMMENT 'S3 签名区域' AFTER endpoint;
