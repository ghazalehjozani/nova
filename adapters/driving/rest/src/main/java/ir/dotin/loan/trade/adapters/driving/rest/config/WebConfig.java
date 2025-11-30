package ir.dotin.loan.trade.adapters.driving.rest.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ApiVersionResolver;
import org.springframework.web.accept.PathApiVersionResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useVersionResolver(new SmartPathApiVersionResolver(1, "/api", "1"))
                .addSupportedVersions("1")
                .setVersionRequired(true);
        //        .setDeprecationHandler(new StandardApiVersionDeprecationHandler()
        //                .deprecate("1.0") // Mark 1.0 as deprecated
        //                .setEffectiveDate("1.0", "2025-12-31") // When it becomes invalid
        //                .setInfoUrl("1.0", "http://site.local/migration-guide") // Help link
    }

    static class SmartPathApiVersionResolver implements ApiVersionResolver {
        private final PathApiVersionResolver delegate;
        private final String apiPrefix;
        private final String fallbackVersion;

        public SmartPathApiVersionResolver(int index, String apiPrefix, String fallbackVersion) {
            this.delegate = new PathApiVersionResolver(index);
            this.apiPrefix = apiPrefix;
            this.fallbackVersion = fallbackVersion;
        }

        @Override
        public String resolveVersion(HttpServletRequest request) {
            String requestUri = request.getRequestURI();

            if (requestUri != null && requestUri.startsWith(this.apiPrefix)) {
                return delegate.resolveVersion(request);
            }

            return this.fallbackVersion;
        }
    }
}
