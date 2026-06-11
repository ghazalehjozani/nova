package ir.dotin.loan.trade.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.pangaea.servicelayer.starter.transaction.RemoteIoBoundaryGuard;
import ir.dotin.platform.pangaea.servicelayer.starter.transaction.WriteHandlerRemotePortVerifier;
import ir.dotin.platform.pangaea.servicelayer.transaction.TransactionRunner;

@Configuration(proxyBeanMethods = false)
class RemoteIoGuardConfiguration {

    @Bean
    static RemoteIoBoundaryGuard remoteIoBoundaryGuard(ObjectProvider<TransactionRunner> transactionRunner) {
        return new RemoteIoBoundaryGuard(transactionRunner);
    }

    @Bean
    static WriteHandlerRemotePortVerifier writeHandlerRemotePortVerifier(ConfigurableListableBeanFactory beanFactory) {
        return new WriteHandlerRemotePortVerifier(beanFactory);
    }
}
