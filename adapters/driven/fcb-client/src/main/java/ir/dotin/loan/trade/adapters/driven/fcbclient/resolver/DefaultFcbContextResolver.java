package ir.dotin.loan.trade.adapters.driven.fcbclient.resolver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultFcbContextResolver implements FcbContextResolver {

    @Value("${fcb.context.ip:127.0.0.1}")
    private String defaultIp;

    @Value("${fcb.context.branch:1}")
    private String defaultBranch;

    @Override
    public String resolveIpAddress() {
        // Implement logic to get IP from context (e.g., from security context, request, etc.)
        // For now, returning default
        return defaultIp;
    }

    @Override
    public String resolveCurrentBranch() {
        // Implement logic to get branch from context (e.g., from user session, JWT token, etc.)
        // For now, returning default
        return defaultBranch;
    }
}
