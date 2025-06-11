package dsrl.mariatitianu.devicemanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final Environment environment;

    @Bean
    public Queue createDeviceEventQueue(){
        return new Queue(Objects.requireNonNull(environment.getProperty("rabbitmq.device.queue")),true);
    }
}
