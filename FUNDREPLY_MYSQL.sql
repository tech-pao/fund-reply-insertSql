-- MySQL initialization script for fundreply
-- Source DDL used quoted names inconsistently: "AUTO COLL" vs AUTOCOLL.
-- This script uses AUTOCOLL as the final table name to match the indexes and comments.

CREATE DATABASE IF NOT EXISTS fundreply
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'fundreply'@'%' IDENTIFIED BY 'fundreply';

GRANT ALL PRIVILEGES ON fundreply.* TO 'fundreply'@'%';

FLUSH PRIVILEGES;

USE fundreply;

CREATE TABLE IF NOT EXISTS AUTOCOLL (
    PKID VARCHAR(200) NOT NULL COMMENT '唯一主键编号',
    TRACEID VARCHAR(200) NOT NULL COMMENT '全局流水',
    COLLREQ LONGTEXT COMMENT '采集请求报文',
    COLLRES LONGTEXT COMMENT '采集返回报文',
    TXNCODE VARCHAR(100) COMMENT '接口码',
    COLLTIME VARCHAR(60) COMMENT '采集时间',
    STATUS VARCHAR(1) COMMENT '回放状态',
    COLLHEADER VARCHAR(4000) COMMENT '请求报文头',
    METHOD VARCHAR(500) COMMENT '接口方法',
    SERVICENAME VARCHAR(500) COMMENT '微服务名称',
    PRIMARY KEY (PKID),
    INDEX fundreplyAUTOCOLLMIX (COLLTIME, PKID, TRACEID),
    INDEX fundreplyIDXTRACEID (TRACEID)
) COMMENT='流量采集表';

-- Optional index: create only after data import if you still need this access path.
-- CREATE INDEX fundreplyIDXTXNCODE ON AUTOCOLL (TXNCODE);