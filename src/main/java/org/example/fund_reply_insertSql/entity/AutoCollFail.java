package org.example.fund_reply_insertSql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("AUTO_COLL_FAIL")
public class AutoCollFail {

	/**
	 * 唯一主键编号
	 */
	@TableId(value = "PKID", type = IdType.ASSIGN_ID)
	private String pkid;

	/**
	 * 全局流水
	 */
	@TableField("TRACEID")
	private String traceid;

	/**
	 * 采集请求报文
	 */
	@TableField("COLLREQ")
	private String collreq;

	/**
	 * 采集返回报文
	 */
	@TableField("COLLRES")
	private String collres;

	/**
	 * 接口码
	 */
	@TableField("TXNCODE")
	private String txncode;

	/**
	 * 采集时间
	 */
	@TableField("COLLTIME")
	private String colltime;

	/**
	 * 回放状态
	 */
	@TableField("STATUS")
	private String status;

	/**
	 * 请求报文头
	 */
	@TableField("COLLHEADER")
	private String collheader;

	/**
	 * 接口方法
	 */
	@TableField("METHOD")
	private String method;

	/**
	 * 微服务名称
	 */
	@TableField("SERVICENAME")
	private String servicename;
}
