package ir.dotin.loan.trade.adapters.driving.rest.config;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ApiVersionResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DEFAULT_VERSION = "1";
    private static final Set<String> SUPPORTED_VERSIONS = Set.of("1");

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useVersionResolver(new SmartPathApiVersionResolver(DEFAULT_VERSION, SUPPORTED_VERSIONS))
                .addSupportedVersions(SUPPORTED_VERSIONS.toArray(String[]::new))
                .setVersionRequired(true);
        //        .setDeprecationHandler(new StandardApiVersionDeprecationHandler()
        //                .deprecate("1.0") // Mark 1.0 as deprecated
        //                .setEffectiveDate("1.0", "2025-12-31") // When it becomes invalid
        //                .setInfoUrl("1.0", "http://site.local/migration-guide") // Help link
    }

    /**
     * Resolves the API version from the <em>first</em> path segment per the inter-service standard URL form
     * {@code /v{version}/{resource}} — e.g. {@code /v1/loan-types} → {@code "1"}. The parsed version is honoured only
     * when it is a supported version; anything else (including infra paths whose first segment merely looks versioned,
     * such as springdoc's {@code /v3/api-docs}, plus {@code /swagger-ui}, {@code /actuator}, dev endpoints) falls back
     * to the default version so those endpoints keep working.
     */
    static class SmartPathApiVersionResolver implements ApiVersionResolver {
        private static final Pattern LEADING_VERSION = Pattern.compile("/v(\\d+)(?:/.*)?");
        private final String fallbackVersion;
        private final Set<String> supportedVersions;

        public SmartPathApiVersionResolver(String fallbackVersion, Set<String> supportedVersions) {
            this.fallbackVersion = fallbackVersion;
            this.supportedVersions = supportedVersions;
        }

        @Override
        public String resolveVersion(HttpServletRequest request) {
            String requestUri = request.getRequestURI();
            if (requestUri != null) {
                Matcher matcher = LEADING_VERSION.matcher(requestUri);
                if (matcher.matches() && supportedVersions.contains(matcher.group(1))) {
                    return matcher.group(1);
                }
            }
            return this.fallbackVersion;
        }
    }
}
