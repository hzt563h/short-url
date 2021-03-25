
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb_short_url
-- ----------------------------
DROP TABLE IF EXISTS `tb_short_url`;
CREATE TABLE `tb_short_url` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `hash_value` varchar(32) NOT NULL,
  `url` varchar(2048) NOT NULL,
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否删除 1 删除 0 未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique` (`hash_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
