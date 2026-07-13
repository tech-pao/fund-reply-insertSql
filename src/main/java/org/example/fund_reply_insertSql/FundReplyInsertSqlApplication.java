package org.example.fund_reply_insertSql;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.fund_reply_insertSql.mapper")
public class FundReplyInsertSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundReplyInsertSqlApplication.class, args);
	}

}
