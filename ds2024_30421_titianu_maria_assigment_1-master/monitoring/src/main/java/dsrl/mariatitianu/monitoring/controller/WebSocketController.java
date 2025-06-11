package dsrl.mariatitianu.monitoring.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import dsrl.mariatitianu.monitoring.dto.graph.GraphRequestDTO;
import dsrl.mariatitianu.monitoring.service.HourlyMeasurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Configuration
@Controller
@RequiredArgsConstructor
@Log
public class WebSocketController {
    private final HourlyMeasurementService hourlyMeasurementService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();

    @MessageMapping("/graph")
    public void handleGraphDataRequest(@Payload GraphRequestDTO dto){
        List<HourlyMeasurementDTO> hourlyMeasurementDTOs = hourlyMeasurementService.getDeviceHourlyMeasurements(dto);
        messagingTemplate.convertAndSend(
                "/topic/graphData",
                gson.toJson(hourlyMeasurementDTOs),
                Map.of("userUuid", dto.getUserUuid())
        );
        System.out.println("Graph data for user UUID " + dto.getUserUuid() + " with payload " + hourlyMeasurementDTOs + " sent!");
    }
}
