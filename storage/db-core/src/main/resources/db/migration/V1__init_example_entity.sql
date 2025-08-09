CREATE TABLE IF NOT EXISTS `example_entity` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT,
    `created_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) on update CURRENT_TIMESTAMP(6),
    `example_column`        VARCHAR(255)    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4;