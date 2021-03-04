package com.hfhk.check.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(
	basePackages = {"com.hfhk.**.client"}
)
@EnableDiscoveryClient

@Configuration
public class CloudConfig {
}
