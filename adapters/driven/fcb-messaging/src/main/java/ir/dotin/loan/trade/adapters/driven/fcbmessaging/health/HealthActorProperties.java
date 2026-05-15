package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = HealthActorProperties.PREFIX)
public record HealthActorProperties(
        @NotBlank @DefaultValue("admin") String sub,
        @NotBlank @DefaultValue("1") String branchCode) {

    public static final String PREFIX = "platform.health-probe.actor";
}
