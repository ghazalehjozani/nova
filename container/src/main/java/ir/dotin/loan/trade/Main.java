package ir.dotin.loan.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.platform.commons.domain.annotation.DomainFactory;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import org.springframework.scheduling.annotation.EnableScheduling;

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
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
