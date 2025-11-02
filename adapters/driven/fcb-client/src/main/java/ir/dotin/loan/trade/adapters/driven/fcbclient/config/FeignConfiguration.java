package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;

import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbClientErrorDecoder;

import feign.Client;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.FormEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = FcbConfiguration.class)
public class FeignConfiguration {

    private final FcbConfiguration fcbConfiguration;

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                fcbConfiguration.integration().connectionTimeout(),
                fcbConfiguration.integration().readTimeout());
    }

    @Bean
    @ConditionalOnMissingBean
    public Client feignClient() {
        BasicCookieStore cookieStore = new BasicCookieStore();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(cookieStore)
                .build();

        return new feign.httpclient.ApacheHttpClient(httpClient);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Encoder formEncoder() {
        return new FormEncoder(new SpringEncoder(
                () -> new HttpMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))));
    }

    @Bean
    public Decoder xmlDecoder() {
        return new XmlDecoder();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FcbClientErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            log.debug("=== FCB Request ===");
            log.debug("Method: {}", template.method());
            log.debug("URL: {}", template.url());
            log.debug("Headers: {}", template.headers());

            if (template.body() != null) {
                String body = new String(template.body(), StandardCharsets.UTF_8);
                log.debug("Body: {}", body);
                log.debug("Body length: {} bytes", template.body().length);
            } else {
                log.debug("Body: null");
            }

            if (template.headers().containsKey("Content-Type")) {
                log.debug("Content-Type: {}", template.headers().get("Content-Type"));
            } else {
                log.warn("Content-Type header not set!");
            }
        };
    }

    public static class XmlDecoder implements Decoder {

        private final XStream xstream;

        public XmlDecoder() {
            this.xstream = FcbXStreamFactory.createXStream();
        }

        @Override
        public Object decode(Response response, Type type) throws DecodeException {

            if (response.body() == null || response.body().length() == 0) {
                return null;
            }

            try (InputStream inputStream = response.body().asInputStream()) {
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                if (content.trim().startsWith("<!DOCTYPE") || content.trim().startsWith("<html")) {
                    log.error(
                            "Received HTML response instead of XML. Content start: {}",
                            content.substring(0, Math.min(200, content.length())));
                    throw new DecodeException(
                            response.status(),
                            "Received HTML response instead of XML. Possible authentication failure or redirect to login page.",
                            response.request());
                }

                if (isXStreamCompatibleType(type)) {
                    return xstream.fromXML(content);
                } else {
                    JAXBContext jaxbContext = JAXBContext.newInstance((Class<?>) type);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    return unmarshaller.unmarshal(new StringReader(content));
                }

            } catch (Exception e) {
                log.error("Error decoding XML response", e);
                throw new DecodeException(
                        response.status(), "Error decoding XML response: " + e.getMessage(), response.request());
            }
        }

        private boolean isXStreamCompatibleType(Type type) {
            return type instanceof Class
                    && (((Class<?>) type).getSimpleName().toLowerCase().contains("response")
                            || ((Class<?>) type).getSimpleName().toLowerCase().contains("request"));
        }
    }
}
