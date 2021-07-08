package com.dcs.gmall.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.dcs.gmall.base.mapper")
@EnableTransactionManagement
@ComponentScan("com.dcs.gmall")
public class GmallBaseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallBaseServiceApplication.class, args);
	}

}
