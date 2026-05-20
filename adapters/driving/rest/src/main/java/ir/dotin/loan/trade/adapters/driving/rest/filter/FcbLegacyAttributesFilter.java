package ir.dotin.loan.trade.adapters.driving.rest.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@Component
public class FcbLegacyAttributesFilter extends OncePerRequestFilter {

    private static final String HEADER_X_FCB_ORIGIN = "X-FCB-Origin";
    private static final String HEADER_CORRELATION_TRACEPARENT = "correlation-traceparent";
    private static final String FCB_ORIGIN_VALUE = "fcb";
    private static final String LEGACY_PEER_SERVICE = "fcb-legacy";
    private static final String ATTR_PEER_SERVICE = "peer.service";
    private static final String ATTR_CORRELATION_TRACE_ID = "correlation.trace.id";
    private static final String ATTR_CORRELATION_SPAN_ID = "correlation.span.id";
    private static final int TRACEPARENT_LENGTH = 55;

    private static final Logger LOG = LoggerFactory.getLogger(FcbLegacyAttributesFilter.class);

    @Nullable
    private final Tracer tracer;

    public FcbLegacyAttributesFilter(@Nullable Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        applyAttributes(request);
        chain.doFilter(request, response);
    }

    private void applyAttributes(HttpServletRequest request) {
        if (tracer == null) {
            return;
        }
        if (!FCB_ORIGIN_VALUE.equals(request.getHeader(HEADER_X_FCB_ORIGIN))) {
            return;
        }
        Span current = tracer.currentSpan();
        if (current == null) {
            return;
        }
        current.tag(ATTR_PEER_SERVICE, LEGACY_PEER_SERVICE);
        String correlation = request.getHeader(HEADER_CORRELATION_TRACEPARENT);
        if (correlation == null) {
            return;
        }
        String trimmed = correlation.trim();
        if (trimmed.length() != TRACEPARENT_LENGTH) {
            LOG.debug("FCB-LEGACY: ignoring malformed correlation-traceparent length={}", trimmed.length());
            return;
        }
        String[] parts = trimmed.split("-");
        if (parts.length != 4
                || parts[1].length() != 32
                || parts[2].length() != 16
                || !isHex(parts[1])
                || !isHex(parts[2])) {
            LOG.debug("FCB-LEGACY: ignoring malformed correlation-traceparent shape");
            return;
        }
        current.tag(ATTR_CORRELATION_TRACE_ID, parts[1]);
        current.tag(ATTR_CORRELATION_SPAN_ID, parts[2]);
    }

    private static boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }
}
