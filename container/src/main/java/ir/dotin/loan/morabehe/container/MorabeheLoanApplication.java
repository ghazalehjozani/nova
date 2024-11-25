package ir.dotin.loan.morabehe.container;

import ir.dotin.loan.baseloan.container.config.ApplicationComponentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ApplicationComponentConfig.class)
public class MorabeheLoanApplication {


    public static void main(String[] args) {
        SpringApplication.run(MorabeheLoanApplication.class, args);
    }

}
