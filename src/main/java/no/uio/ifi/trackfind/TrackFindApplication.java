package no.uio.ifi.trackfind;

import de.invesdwin.instrument.DynamicInstrumentationLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving.ENABLED;

/**
 * Spring Boot application's main class with some configuration and some beans defined.
 *
 * @author Dmytro Titov
 */
@Slf4j
@SpringBootApplication
@EnableSpringConfigured
@EnableLoadTimeWeaving(aspectjWeaving = ENABLED)
@EnableCaching(mode = AdviceMode.ASPECTJ)
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableHystrix
@EnableHystrixDashboard
public class TrackFindApplication {

    public static void main(String[] args) {
        DynamicInstrumentationLoader.waitForInitialized(); //dynamically attach java agent to jvm if not already present
        DynamicInstrumentationLoader.initLoadTimeWeavingContext(); //weave all classes before they are loaded as beans
        SpringApplication.run(TrackFindApplication.class, args);
    }

}
