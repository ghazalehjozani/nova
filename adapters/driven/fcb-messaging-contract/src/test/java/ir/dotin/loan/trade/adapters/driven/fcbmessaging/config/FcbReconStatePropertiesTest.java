package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FcbReconStatePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(Config.class);

    @Test
    void bindsDefault() {
        runner.run(context -> {
            FcbReconStateProperties props = context.getBean(FcbReconStateProperties.class);
            assertThat(props.getReconStateTimeout()).isEqualTo(Duration.ofSeconds(10));
        });
    }

    @Test
    void bindsOverride() {
        runner.withPropertyValues("reconciliation.recon-state-timeout=20s").run(context -> {
            FcbReconStateProperties props = context.getBean(FcbReconStateProperties.class);
            assertThat(props.getReconStateTimeout()).isEqualTo(Duration.ofSeconds(20));
        });
    }

    @EnableConfigurationProperties(FcbReconStateProperties.class)
    static class Config {}
}
