package eu.europa.ec.simpl.contracts;

import eu.europa.ec.simpl.common.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class})
@EnableScheduling
public class SimplContractsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplContractsApplication.class, args);
    }

}
