package ir.dotin.loan.trade.adapters.driven.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "ir.dotin.loan.trade.adapters.driven.persistence")
@EntityScan(basePackages = "ir.dotin.loan.trade.adapters.driven.persistence")
public class LoanPersistenceConfiguration {
}
