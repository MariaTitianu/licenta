package dsrl.mariatitianu.usermanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${devicemicroservice.ip}")
    String microserviceIp;
    @Value("${devicemicroservice.port}")
    String microservicePort;

    @Bean
    RestClient restClient() {
        return RestClient.create("http://" + microserviceIp + ":" + microservicePort + "/device-management");
    }
}
