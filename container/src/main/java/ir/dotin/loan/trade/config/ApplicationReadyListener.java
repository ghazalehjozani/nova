package ir.dotin.loan.trade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationReadyListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationReadyListener.class);

    private final ApplicationContext applicationContext;

    public ApplicationReadyListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready - publishing availability state changes");
        AvailabilityChangeEvent.publish(applicationContext, LivenessState.CORRECT);
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC);
        log.info("Application is now LIVE and READY to accept traffic");
    }
}
