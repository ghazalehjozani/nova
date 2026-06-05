package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationSourcePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(Config.class);

    @Test
    void bindsDefaults() {
        runner.run(context -> {
            ReconciliationSourceProperties props = context.getBean(ReconciliationSourceProperties.class);
            assertThat(props.getSourceBatchSize()).isEqualTo(200);
            assertThat(props.getDetectionSettle()).isEqualTo(Duration.ofMinutes(3));
        });
    }

    @Test
    void bindsOverrides() {
        runner.withPropertyValues("reconciliation.source-batch-size=50", "reconciliation.detection-settle=90s")
                .run(context -> {
                    ReconciliationSourceProperties props = context.getBean(ReconciliationSourceProperties.class);
                    assertThat(props.getSourceBatchSize()).isEqualTo(50);
                    assertThat(props.getDetectionSettle()).isEqualTo(Duration.ofSeconds(90));
                });
    }

    @EnableConfigurationProperties(ReconciliationSourceProperties.class)
    static class Config {}
}
