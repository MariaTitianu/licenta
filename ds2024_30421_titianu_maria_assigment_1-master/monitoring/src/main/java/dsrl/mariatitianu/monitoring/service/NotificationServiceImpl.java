package dsrl.mariatitianu.monitoring.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();

    @Override
    public void sendNotification(UUID userUuid, HourlyMeasurementDTO hourlyMeasurementDTO) {
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                gson.toJson(hourlyMeasurementDTO),
                Map.of("userUuid", userUuid.toString())
        );
        System.out.println("Notification with user UUID " + userUuid + " and payload " + hourlyMeasurementDTO + " sent!");
    }
}
