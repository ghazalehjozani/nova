package ir.dotin.loan.trade.config.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule relationTypeModule() {
        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(RelationType.class, new RelationTypeKeyDeserializer());
        return module;
    }
}
