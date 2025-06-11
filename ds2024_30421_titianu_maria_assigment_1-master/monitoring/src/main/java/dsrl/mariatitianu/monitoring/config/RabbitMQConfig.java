package dsrl.mariatitianu.monitoring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RabbitMQConfig {
    @Value("${rabbitmq.simulator.queue}")
    private String simulatorQueueName;
    @Value("${rabbitmq.device.queue}")
    private String deviceQueueName;

    @Bean
    public Queue createSimulatorQueue() {
        return new Queue(simulatorQueueName, true);
    }

    @Bean
    public Queue createDeviceQueue() {
        return new Queue(deviceQueueName, true);
    }
}