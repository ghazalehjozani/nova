package ir.dotin.loan.trade.config.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;

@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule relationTypeModule() {
        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(RelationType.class, new RelationTypeKeyDeserializer());
        return module;
    }
}
