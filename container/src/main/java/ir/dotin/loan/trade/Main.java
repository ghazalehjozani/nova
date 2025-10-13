package ir.dotin.loan.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.platform.commons.domain.annotation.DomainFactory;
import ir.dotin.platform.commons.domain.annotation.DomainService;

@SpringBootApplication
@ComponentScan(
        basePackages = "ir.dotin.loan",
        includeFilters =
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {DomainComponent.class, DomainService.class, DomainFactory.class}))
@RefreshScope
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients
@ConfigurationPropertiesScan
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
