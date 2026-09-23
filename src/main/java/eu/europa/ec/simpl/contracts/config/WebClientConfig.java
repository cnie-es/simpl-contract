package eu.europa.ec.simpl.contracts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final int IN_MEMORY_SIZE = 32 * 1024 * 1024;
    private static final String SIGN_CONTRACT_ENDPOINT = "/v1/credential";
    @Value("${spring.external-services.urls.signer}")
    private String signerServiceHost;

    @Bean
    public WebClient.RequestBodySpec signerPostSpec() {
        return WebClient.builder()
                .exchangeStrategies(setExchangeStrategies())
                .baseUrl(signerServiceHost)
                .build()
                .post()
                .uri(SIGN_CONTRACT_ENDPOINT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private ExchangeStrategies setExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(IN_MEMORY_SIZE))
                .build();
    }
}
