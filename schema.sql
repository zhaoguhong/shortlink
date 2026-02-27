-- 数据库：shortlink

CREATE DATABASE IF NOT EXISTS `shortlink`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `shortlink`;

-- 短链接主表：short_link

CREATE TABLE IF NOT EXISTS `short_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(32) NOT NULL COMMENT '短链码',
  `original_url` VARCHAR(1024) NOT NULL COMMENT '原始长链接',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='短链接主表';


-- 访问统计表：short_link_stats

CREATE TABLE IF NOT EXISTS `short_link_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `link_id` BIGINT NOT NULL COMMENT 'short_link.id',
  `total_pv` BIGINT NOT NULL DEFAULT 0 COMMENT '累计PV',
  `total_uv` BIGINT NOT NULL DEFAULT 0 COMMENT '累计UV',
  `recent_access_time` DATETIME NULL DEFAULT NULL COMMENT '最近访问时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_link_id` (`link_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='短链接访问统计表';

