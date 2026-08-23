package ir.dotin.loan.trade.e2e.security;

import java.util.Optional;

import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.security.api.AuthenticationToken;
import ir.dotin.platform.pangaea.security.api.UserContext;
import ir.dotin.platform.pangaea.security.api.UserInfo;

/**
 * Fixed banking context for the E2E profile, which runs with {@code pangaea.security.enabled: false} and therefore has
 * no authenticated principal. Command controllers call {@code branchCode().orElseThrow()} and {@code userIdOrThrow()},
 * so without this they fail before reaching the dispatcher. Branch {@code "1"} matches the fixtures.
 */
public class E2EAuthenticationContextHolder implements AuthenticationContextHolder {

    public static final String BRANCH_CODE = "1";
    private static final String USER_ID = "e2e-user";
    private static final String CLIENT_ID = "e2e-client";
    private static final String IP_ADDRESS = "127.0.0.1";

    @Override
    public Optional<AuthenticationToken> authentication() {
        return Optional.empty();
    }

    @Override
    public Optional<UserContext> userContext() {
        return Optional.empty();
    }

    @Override
    public Optional<String> userId() {
        return Optional.of(USER_ID);
    }

    @Override
    public String userIdOrThrow() {
        return USER_ID;
    }

    @Override
    public Optional<String> branchCode() {
        return Optional.of(BRANCH_CODE);
    }

    @Override
    public Optional<UserInfo> userInfo() {
        return Optional.empty();
    }

    @Override
    public Optional<String> ipAddress() {
        return Optional.of(IP_ADDRESS);
    }

    @Override
    public Optional<String> clientId() {
        return Optional.of(CLIENT_ID);
    }

    @Override
    public boolean hasScope(String scope) {
        return true;
    }

    @Override
    public boolean hasAnyScope(String... scopes) {
        return true;
    }

    @Override
    public boolean hasAllScopes(String... scopes) {
        return true;
    }

    @Override
    public void clear() {
        // no-op: the context is fixed for the whole E2E run
    }
}
