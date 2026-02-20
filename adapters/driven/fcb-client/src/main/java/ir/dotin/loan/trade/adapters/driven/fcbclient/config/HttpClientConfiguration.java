package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import ir.dotin.loan.trade.adapters.driven.fcbclient.client.FcbHttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!activemq")
@RequiredArgsConstructor
@EnableConfigurationProperties(FcbConfiguration.class)
public class HttpClientConfiguration {

    private final FcbConfiguration fcbConfiguration;

    @Bean
    public FcbHttpClient fcbHttpClient(ClientHttpRequestFactory requestFactory) {
        RestClient restClient = RestClient.builder()
                .baseUrl(fcbConfiguration.integration().baseUrl())
                .requestFactory(requestFactory)
                .configureMessageConverters(builder -> builder.addCustomConverter(new FormHttpMessageConverter())
                        .addCustomConverter(new StringHttpMessageConverter(StandardCharsets.UTF_8)))
                .requestInterceptor((request, body, execution) -> {
                    log.debug("=== FCB Request ===");
                    log.debug("Method: {}", request.getMethod());
                    log.debug("URL: {}", request.getURI());
                    log.debug("Headers: {}", request.getHeaders());
                    if (body.length > 0) {
                        log.debug("Body: {}", new String(body, StandardCharsets.UTF_8));
                    }
                    return execution.execute(request, body);
                })
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(FcbHttpClient.class);
    }

    @Bean
    @Profile({"dev", "stage"})
    public ClientHttpRequestFactory insecureRequestFactory() {
        try {
            var sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();

            var tlsStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setTlsSocketStrategy(tlsStrategy)
                    .build();

            CloseableHttpClient httpClient =
                    HttpClients.custom().setConnectionManager(connectionManager).build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectionRequestTimeout(fcbConfiguration.integration().connectionTimeout());
            factory.setReadTimeout(fcbConfiguration.integration().readTimeout());

            return factory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure HTTP client", e);
        }
    }

    @Bean
    @Profile("!dev & !stage")
    public ClientHttpRequestFactory secureRequestFactory() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(fcbConfiguration.integration().connectionTimeout());
        factory.setReadTimeout(fcbConfiguration.integration().readTimeout());
        return factory;
    }
}
