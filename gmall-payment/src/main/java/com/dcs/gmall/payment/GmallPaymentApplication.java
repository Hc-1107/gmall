package com.dcs.gmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.dcs.gmall.payment.mapper")
@ComponentScan("com.dcs.gmall")
public class GmallPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPaymentApplication.class, args);
	}

}
