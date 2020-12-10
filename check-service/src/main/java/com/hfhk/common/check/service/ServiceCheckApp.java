package com.hfhk.common.check.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
	basePackages = {"com.hfhk.cairo.*.client"}
)
@EnableDiscoveryClient
public class ServiceCheckApp {

	public static void main(String[] args) {
		SpringApplication.run(ServiceCheckApp.class, args);
	}

}
