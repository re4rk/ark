CREATE TABLE IF NOT EXISTS `example_entity` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT,

    `example_column`        VARCHAR(255)    NOT NULL,

    `created_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) on update CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4;

CREATE TABLE IF NOT EXISTS files (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT,

    `original_name`         VARCHAR(255)    NOT NULL,
    `s3_key`                VARCHAR(255)    NOT NULL,
    `size`                  BIGINT          NOT NULL,
    `mime_type`             VARCHAR(255)    NOT NULL,
    `status`                VARCHAR(32)     NOT NULL,

    `created_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
     PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4;