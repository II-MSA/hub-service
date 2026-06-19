package org.iimsa.hub_service;

import org.iimsa.hub_service.hubroute.infrastructure.external.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients
@EnableConfigurationProperties(KakaoProperties.class)
@SpringBootApplication
@EntityScan(basePackages = {"org.ticketing", "org.iimsa"})
@EnableJpaRepositories(basePackages = {"org.ticketing", "org.iimsa"})
public class HubServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubServiceApplication.class, args);
	}

}
