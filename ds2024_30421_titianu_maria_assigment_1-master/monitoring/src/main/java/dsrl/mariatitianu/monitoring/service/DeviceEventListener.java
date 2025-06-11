package dsrl.mariatitianu.monitoring.service;

import com.google.gson.Gson;
import dsrl.mariatitianu.monitoring.dto.device.DeviceDTO;
import dsrl.mariatitianu.monitoring.dto.device.MonitoringDeviceDTO;
import dsrl.mariatitianu.monitoring.enums.MonitoringMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DeviceEventListener {
    private final Gson gson;
    private final DeviceService deviceService;

    @RabbitListener(queues = "${rabbitmq.device.queue}")
    public void onDeviceEventReceived(final Message message) {
        System.out.println(message);
        System.out.println(gson.fromJson(new String(message.getBody()), MonitoringDeviceDTO.class));
        MonitoringDeviceDTO monitoringDeviceDTO = gson.fromJson(new String(message.getBody()), MonitoringDeviceDTO.class);
        switch (MonitoringMessageType.valueOf(message.getMessageProperties().getType())) {
            case CREATE -> {
                DeviceDTO deviceDTO = deviceService.create(monitoringDeviceDTO);
                System.out.println("Created device " + deviceDTO);
            }
            case UPDATE -> {
                Optional<DeviceDTO> deviceDTOOptional = deviceService.update(monitoringDeviceDTO.getDeviceUuid(), monitoringDeviceDTO);
                if (deviceDTOOptional.isPresent()) {
                    System.out.println("Updated device " + deviceDTOOptional.get());
                }
                else{
                    System.out.println("Device not found");
                }
            }
            case DELETE -> {
                Optional<DeviceDTO> deviceDTOOptional = deviceService.delete(monitoringDeviceDTO.getDeviceUuid());
                if (deviceDTOOptional.isPresent()) {
                    System.out.println("Deleted device " + deviceDTOOptional.get());
                }
                else{
                    System.out.println("Device not found");
                }
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + MonitoringMessageType.valueOf(message.getMessageProperties().getType()));
            }
        }
    }
}
