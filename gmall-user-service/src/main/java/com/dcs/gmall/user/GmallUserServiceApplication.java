package com.dcs.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.dcs.gmall.user.mapper")
@ComponentScan("com.dcs.gmall")
public class GmallUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserServiceApplication.class, args);
	}

}
