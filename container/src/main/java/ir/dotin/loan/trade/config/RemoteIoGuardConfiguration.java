package ir.dotin.loan.trade.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.pangaea.servicelayer.starter.transaction.RemoteIoBoundaryGuard;
import ir.dotin.platform.pangaea.servicelayer.starter.transaction.WriteHandlerRemotePortVerifier;
import ir.dotin.platform.pangaea.servicelayer.transaction.TransactionRunner;

@Configuration(proxyBeanMethods = false)
class RemoteIoGuardConfiguration {

    private static final List<String> REMOTE_PORT_PACKAGES =
            List.of("ir.dotin.loan.trade.core.application.ports.outbound.client");

    @Bean
    static RemoteIoBoundaryGuard remoteIoBoundaryGuard(ObjectProvider<TransactionRunner> transactionRunner) {
        return new RemoteIoBoundaryGuard(transactionRunner, REMOTE_PORT_PACKAGES);
    }

    @Bean
    static WriteHandlerRemotePortVerifier writeHandlerRemotePortVerifier(ConfigurableListableBeanFactory beanFactory) {
        return new WriteHandlerRemotePortVerifier(beanFactory, REMOTE_PORT_PACKAGES);
    }
}
