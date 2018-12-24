use matrix_publish;
CREATE TABLE `beta_delpoy_record` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '测试环境部署记录id',
  `project_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目ID',
  `project_task_id` int(11) NOT NULL DEFAULT '0' COMMENT '应用发布工单ID',
  `app_code` varchar(32) NOT NULL DEFAULT '' COMMENT '应用标识',
  `app_name` varchar(64) NOT NULL DEFAULT '' COMMENT '应用名称',
  `app_branch` varchar(64) NOT NULL DEFAULT '' COMMENT '应用发布分支',
  `app_btag` varchar(64) NOT NULL DEFAULT '' COMMENT '生成的btag',
  `deploy_status` smallint(4) NOT NULL DEFAULT '0' COMMENT '发布状态',
  `service_ips` varchar(2000) NOT NULL DEFAULT '' COMMENT '应用ip',
  `log_path` varchar(200) NOT NULL DEFAULT '' COMMENT '日志地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
  `del_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:正常|0:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试环境部署记录表';


CREATE TABLE `project_task` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '应用id',
  `project_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目ID',
  `sequenece` int(2) NOT NULL DEFAULT '0' COMMENT '发布顺序',
  `app_code` varchar(32) NOT NULL DEFAULT '' COMMENT '应用标识',
  `app_branch` varchar(64) NOT NULL DEFAULT '' COMMENT '应用分支',
  `app_btag` varchar(64) NOT NULL DEFAULT '' COMMENT '要发布的btag',
  `app_dev_owner` varchar(64) NOT NULL DEFAULT '' COMMENT '应用开发人',
  `app_rtag` varchar(64) NOT NULL DEFAULT '' COMMENT '发布完成的rtag',
  `task_status` smallint(4) NOT NULL DEFAULT '0' COMMENT '应用状态',
  `service_ips` varchar(2000) NOT NULL DEFAULT '' COMMENT '应用ip',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
  `del_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:正常|0:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用发布工单表';

CREATE TABLE `project_task_batch` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '批次id',
  `project_task_id` int(11) NOT NULL DEFAULT '0' COMMENT '应用ID',
  `sequenece` int(2) NOT NULL DEFAULT '0' COMMENT '发布顺序',
  `deploy_status` smallint(4) NOT NULL DEFAULT '0' COMMENT '发布状态',
  `machine_count` smallint(4) NOT NULL DEFAULT '0' COMMENT '机器数',
  `app_group` varchar(20) NOT NULL DEFAULT '' COMMENT '发布组信息',
  `wait_time` smallint(4) NOT NULL DEFAULT '0' COMMENT '等待时间',
  `service_ips` varchar(256) NOT NULL DEFAULT '' COMMENT '应用ip',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:正常|0:删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用发布工单表';


CREATE TABLE `project_task_batch_record` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '批次记录id',
  `project_task_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目ID',
  `project_task_batch_id` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '批次id',
  `deploy_record_id` int(11) NOT NULL DEFAULT '0' COMMENT '发布记录ID',
  `forward_batch_id` int(11) NOT NULL DEFAULT '0' COMMENT '上一发布批次ID',
  `sequenece` int(2) NOT NULL DEFAULT '0' COMMENT '发布顺序',
  `deploy_status` smallint(4) NOT NULL DEFAULT '0' COMMENT '发布状态',
  `machine_count` smallint(4) NOT NULL DEFAULT '0' COMMENT '机器数',
  `app_group` varchar(20) NOT NULL DEFAULT '' COMMENT '发布组信息',
  `wait_time` smallint(4) NOT NULL DEFAULT '0' COMMENT '等待时间',
  `service_ips` varchar(256) NOT NULL DEFAULT '' COMMENT '应用ip',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:正常|0:删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用发布工单表';

CREATE TABLE `release_delpoy_record` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '生产环境部署记录id',
  `project_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目ID',
  `project_task_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目发布工单ID',
  `forward_record_id` int(11) NOT NULL DEFAULT '0' COMMENT '依赖的上一条发布',
  `app_code` varchar(32) NOT NULL DEFAULT '' COMMENT '应用标识',
  `app_name` varchar(64) NOT NULL DEFAULT '' COMMENT '应用名称',
  `app_branch` varchar(64) NOT NULL DEFAULT '' COMMENT '应用分支',
  `app_btag` varchar(64) NOT NULL DEFAULT '' COMMENT '要发布的btag',
  `app_rtag` varchar(64) NOT NULL DEFAULT '' COMMENT '发布完成的rtag',
  `deploy_status` smallint(4) NOT NULL DEFAULT '0' COMMENT '应用状态',
  `service_ips` varchar(2000) NOT NULL DEFAULT '' COMMENT '应用ip',
  `log_path` varchar(200) NOT NULL DEFAULT '' COMMENT '日志地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
  `del_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:正常|0:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='生产环境部署记录表';

CREATE TABLE ulb_info (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
`ip` varchar(64) NOT NULL DEFAULT '' COMMENT '应用服务器IP',
`backend_port` int(6) NOT NULL DEFAULT '0' COMMENT 'backend端口',
`ulb_id` varchar(32) NOT NULL DEFAULT '' COMMENT 'ULBID',
`backend_id` varchar(32) NOT NULL DEFAULT '' COMMENT 'ULB的BackendID',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' ,
PRIMARY KEY (`id`),
UNIQUE KEY `uq_backend_id`(`backend_id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ULB 信息';

CREATE TABLE app_start_result (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
`result_code` varchar(8) NOT NULL DEFAULT '' COMMENT '返回编码',
`message` varchar(2000) NOT NULL DEFAULT '' COMMENT '成功/失败消息',
`build_type` varchar(32) NOT NULL DEFAULT '' COMMENT '发布类型',
`ip` varchar(32) NOT NULL DEFAULT '' COMMENT '应用机器IP',
`record_id` varchar(32) NOT NULL DEFAULT '' COMMENT '操作记录ID',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用启动结果';