package ir.dotin.loan.trade.adapters.driving.rest.config;

import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.adapter.rest.config.BaseWebMvcConfig;
import ir.dotin.platform.adapter.rest.headers.HeaderValidationInterceptor;

@Configuration
public class WebConfig extends BaseWebMvcConfig {

    public WebConfig(HeaderValidationInterceptor headerValidationInterceptor) {
        super(headerValidationInterceptor);
    }

    @Override
    protected String getApiPathPattern() {
        return "/v1/**";
    }
}
