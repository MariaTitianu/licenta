package dsrl.mariatitianu.monitoring.service;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import dsrl.mariatitianu.monitoring.dto.device.DeviceMeasurementDTO;
import dsrl.mariatitianu.monitoring.entity.Device;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import dsrl.mariatitianu.monitoring.entity.RabbitMeasurement;
import dsrl.mariatitianu.monitoring.mapper.HourlyMeasurementMapper;
import dsrl.mariatitianu.monitoring.mapper.RabbitMeasurementMapper;
import dsrl.mariatitianu.monitoring.repository.DeviceRepository;
import dsrl.mariatitianu.monitoring.repository.HourlyMeasurementRepository;
import dsrl.mariatitianu.monitoring.repository.RabbitMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceMeasurementListener {
    private final Gson gson;

    private final DeviceRepository deviceRepository;

    private final RabbitMeasurementRepository rabbitMeasurementRepository;
    private final RabbitMeasurementMapper rabbitMeasurementMapper;

    private final HourlyMeasurementRepository hourlyMeasurementRepository;
    private final HourlyMeasurementMapper hourlyMeasurementMapper;

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.simulator.queue}")
    public void onDeviceMeasurementReceived(Message message, Channel channel) {

        System.out.println("Received message: " + new String(message.getBody()));
        DeviceMeasurementDTO dto = gson.fromJson(new String(message.getBody()), DeviceMeasurementDTO.class);
        System.out.println("Parsed DTO: " + dto);

        Optional<Device> deviceOptional = deviceRepository.findById(dto.getDeviceId());
        if (deviceOptional.isEmpty()) {
            System.out.println("Device with UUID " + dto.getDeviceId() + " not found, exiting...");
            return;
        }
        Device device = deviceOptional.get();

        RabbitMeasurement rabbitMeasurement = rabbitMeasurementMapper.toRabbitMeasurement(dto);
        System.out.println("Mapped RabbitMeasurement: " + rabbitMeasurement);

        if (!rabbitMeasurementRepository.existsByDeviceUuid(dto.getDeviceId())) {
            rabbitMeasurementRepository.save(rabbitMeasurement);
            System.out.println("Inserted " + rabbitMeasurement + " into Rabbit DB (which is empty), exiting...");
            return;
        }

        LocalDateTime rabbitMeasurementTime = rabbitMeasurement.getDateTime();
        System.out.println("RabbitMeasurement time: " + rabbitMeasurementTime);

        LocalDateTime startCurrentInterval = rabbitMeasurementTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime endCurrentInterval = startCurrentInterval.plusHours(1).minusNanos(1);
        System.out.println("Current interval: " + startCurrentInterval + " to " + endCurrentInterval);

        LinkedList<RabbitMeasurement> currentRabbitMeasurements = rabbitMeasurementRepository
                .findAllByDeviceUuidAndDateTimeBetweenOrderByDateTime(
                        rabbitMeasurement.getDeviceUuid(),
                        startCurrentInterval,
                        endCurrentInterval
                );
        System.out.println("Current interval measurements: " + currentRabbitMeasurements);

        rabbitMeasurementRepository.save(rabbitMeasurement);
        System.out.println("Inserted " + rabbitMeasurement + " into Rabbit DB");

        if (!currentRabbitMeasurements.isEmpty()) {
            System.out.println("Measurements found for the current interval. Exiting...");
            return;
        }
        System.out.println("No measurements found for the current interval. Calculating hourly measurement...");

        LocalDateTime startLastInterval = startCurrentInterval.minusHours(1);
        LocalDateTime endLastInterval = startLastInterval.plusHours(1).minusNanos(1);
        System.out.println("Last interval: " + startLastInterval + " to " + endLastInterval);


        LinkedList<RabbitMeasurement> lastRabbitMeasurements = rabbitMeasurementRepository
                .findAllByDeviceUuidAndDateTimeBetweenOrderByDateTime(
                        rabbitMeasurement.getDeviceUuid(),
                        startLastInterval,
                        endLastInterval
                );
        System.out.println("Last interval measurements: " + lastRabbitMeasurements);

        double measurementDifference =
                rabbitMeasurement.getMeasurement() - lastRabbitMeasurements.getFirst().getMeasurement();
        System.out.println("Calculated measurement (current - first): " + measurementDifference);
        HourlyMeasurement hourlyMeasurement = rabbitMeasurementMapper.toHourlyMeasurement(rabbitMeasurement, measurementDifference);

        hourlyMeasurementRepository.save(hourlyMeasurement);
        System.out.println("Inserted " + hourlyMeasurement + " into Hourly DB");

        if(device.getMaxHour() < measurementDifference){
            System.out.println("Device limit " + device.getMaxHour() + " exceeded, sending notification...");
            notificationService.sendNotification(device.getUserUuid(), hourlyMeasurementMapper.toHourlyMeasurementDTO(hourlyMeasurement));
        }
    }
}
