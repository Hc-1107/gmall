package com.dcs.gmall.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.dcs.gmall")
public class GmallBaseWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallBaseWebApplication.class, args);
	}

}
