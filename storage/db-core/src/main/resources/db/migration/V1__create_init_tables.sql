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
    `file_key`              VARCHAR(1024)   NOT NULL COMMENT '저장된 파일의 키',
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

CREATE TABLE IF NOT EXISTS feeds (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT,

    `content`       TEXT            NOT NULL COMMENT '피드 내용',
    `is_public`     BOOLEAN         NOT NULL COMMENT '공개 여부',
    `category`      VARCHAR(32)     NOT NULL COMMENT '피드 카테고리 (예: NEWS',
    `author_id`     BIGINT          NOT NULL COMMENT '작성자 ID',
    `status`        VARCHAR(32)     NOT NULL COMMENT '피드 상태 (예: ACTIVE, HIDDEN, DELETED)',

    `created_at`    TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`    TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4;

