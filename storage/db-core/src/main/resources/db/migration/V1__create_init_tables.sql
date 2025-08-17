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

    `original_name`         VARCHAR(255)    NOT NULL COMMENT '원본 파일 이름',
    `file_key`              VARCHAR(255)    NOT NULL COMMENT '저장된 파일의 키',
    `size`                  BIGINT          NOT NULL COMMENT '파일 크기 (바이트 단위)',
    `mime_type`             VARCHAR(255)    NOT NULL COMMENT '파일의 MIME 타입',
    `status`                VARCHAR(32)     NOT NULL COMMENT '파일 상태 (예: PENDING, UPLOADED, FAILED)',
    `category`              VARCHAR(32)     NOT NULL COMMENT '파일 카테고리',
    `uploader_id`           BIGINT          NOT NULL COMMENT '업로더 ID',

    `created_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`            TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
     PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4;
