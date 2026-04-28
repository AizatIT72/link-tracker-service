package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.ScrapperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient scrapperRestClient(ScrapperProperties scrapperProperties) {
        return RestClient.builder()
                .baseUrl(scrapperProperties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
