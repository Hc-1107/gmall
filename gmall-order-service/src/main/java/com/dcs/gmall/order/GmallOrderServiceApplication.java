package com.dcs.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.dcs.gmall.order.mapper")
@ComponentScan(basePackages = "com.dcs.gmall")
@EnableTransactionManagement
public class GmallOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallOrderServiceApplication.class, args);
	}

}
