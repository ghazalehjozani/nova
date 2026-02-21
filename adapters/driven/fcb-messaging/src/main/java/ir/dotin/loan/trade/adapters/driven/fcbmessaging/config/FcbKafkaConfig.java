package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties(FcbKafkaProperties.class)
public class FcbKafkaConfig {}
