package ir.dotin.loan.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.platform.commons.domain.annotation.DomainFactory;
import ir.dotin.platform.commons.domain.annotation.DomainService;

/**
 * Nova service entry point.
 *
 * <p>Configuration source of truth: HashiCorp Consul KV (managed by the {@code nova-config} GitOps repository). The
 * only local configuration is {@code bootstrap.yml}, which holds nothing but the Consul connection details and service
 * identity.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {"ir.dotin.loan.trade", "ir.dotin.loan.baseloan.core.domain"},
        includeFilters =
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {DomainComponent.class, DomainService.class, DomainFactory.class}))
@EnableDiscoveryClient
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class NovaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaApplication.class, args);
    }
}
