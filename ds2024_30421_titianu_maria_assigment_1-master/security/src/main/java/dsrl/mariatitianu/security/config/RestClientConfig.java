package dsrl.mariatitianu.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${usermicroservice.ip}")
    String userIp;
    @Value("${usermicroservice.port}")
    String userPort;
    @Value("${devicemicroservice.ip}")
    String deviceIp;
    @Value("${devicemicroservice.port}")
    String devicePort;

    @Bean(name = "userRestClient")
    RestClient userRestClient() {
        return RestClient.create("http://" + userIp + ":" + userPort + "/user-management");
    }

    @Bean(name = "deviceRestClient")
    RestClient deviceRestClient() {
        return RestClient.create("http://" + deviceIp + ":" + devicePort + "/device-management");
    }
}
