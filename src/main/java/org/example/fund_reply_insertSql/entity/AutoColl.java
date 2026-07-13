package org.example.fund_reply_insertSql.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("AUTOCOLL")
public class AutoColl {

	@TableId("PKID")
	private String pkid;

	@TableField("TRACEID")
	private String traceid;

	@TableField("COLLREQ")
	private String collreq;

	@TableField("COLLRES")
	private String collres;

	@TableField("TXNCODE")
	private String txncode;

	@TableField("COLLTIME")
	private String colltime;

	@TableField("STATUS")
	private String status;

	@TableField("COLLHEADER")
	private String collheader;

	@TableField("METHOD")
	private String method;

	@TableField("SERVICENAME")
	private String servicename;
}