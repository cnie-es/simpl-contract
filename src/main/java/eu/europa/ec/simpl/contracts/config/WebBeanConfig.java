package eu.europa.ec.simpl.contracts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class WebBeanConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/contract/v1/health",
                "/contract/v1/health/liveness",
                "/contract/v1/health/readiness"
        );
    }
}
