package ir.dotin.loan.trade.adapters.driving.rest.base.advice;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import ir.dotin.loan.trade.adapters.driving.rest.base.response.ServiceResponse;

@RestControllerAdvice
public class ServiceResponseAdvice implements ResponseBodyAdvice<ServiceResponse<?>> {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_TTL = "Idempotency-Key-TTL";
    private static final String X_REQUEST_DATETIME = "X-Request-DateTime";
    private static final String X_RESPONSE_DATETIME = "X-Response-DateTime";
    private static final String X_IDEMPOTENCY_REPLAYED = "X-Idempotency-Replayed";

    private static final String TRACEPARENT = "traceparent";
    private static final String TRACESTATE = "tracestate";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ServiceResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ServiceResponse<?> beforeBodyWrite(
            @Nullable ServiceResponse<?> body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null) {
            return null;
        }

        response.setStatusCode(body.httpStatus());

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            addStandardResponseHeaders(httpRequest, response);
            addTraceContextHeaders(httpRequest, response);
        }

        return body;
    }

    private void addStandardResponseHeaders(HttpServletRequest request, ServerHttpResponse response) {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            response.getHeaders().set(IDEMPOTENCY_KEY, idempotencyKey);
        }

        String requestDateTime = request.getHeader(X_REQUEST_DATETIME);
        if (requestDateTime != null && !requestDateTime.isBlank()) {
            response.getHeaders().set(X_REQUEST_DATETIME, requestDateTime);
        }

        response.getHeaders().set(X_RESPONSE_DATETIME, Instant.now().toString());

        boolean isReplayed = determineIfReplayed(request);
        response.getHeaders().set(X_IDEMPOTENCY_REPLAYED, String.valueOf(isReplayed));
    }

    private void addTraceContextHeaders(HttpServletRequest request, ServerHttpResponse response) {
        String traceparent = request.getHeader(TRACEPARENT);
        if (traceparent != null && !traceparent.isBlank()) {
            response.getHeaders().set(TRACEPARENT, traceparent);
        }

        String tracestate = request.getHeader(TRACESTATE);
        if (tracestate != null && !tracestate.isBlank()) {
            response.getHeaders().set(TRACESTATE, tracestate);
        }
    }

    private boolean determineIfReplayed(HttpServletRequest request) {
        Object replayedAttribute = request.getAttribute("X-Idempotency-Replayed");
        if (replayedAttribute instanceof Boolean replayed) {
            return replayed;
        }
        return false;
    }
}
