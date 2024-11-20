package ir.dotin.loan.morabehe.container;

import ir.dotin.platform.ddd.common.annotation.DomainComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = DomainComponent.class
), basePackages = {"ir.dotin.loan"})
public class MorabeheLoanApplication {


    public static void main(String[] args) {
        SpringApplication.run(MorabeheLoanApplication.class, args);
    }

}
