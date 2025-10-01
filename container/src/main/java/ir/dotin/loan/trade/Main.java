package ir.dotin.loan.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"ir.dotin.platform.adapter.persistence.convert", "ir.dotin.loan.trade"
        }) // TODO: Remove scan
public class Main {
    void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
