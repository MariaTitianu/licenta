package dsrl.mariatitianu.chat.controller;

import dsrl.mariatitianu.chat.dto.MessageDTO;
import dsrl.mariatitianu.chat.dto.NotificationDTO;
import dsrl.mariatitianu.chat.dto.SentMessageDTO;
import dsrl.mariatitianu.chat.mapper.MessageMapper;
import dsrl.mariatitianu.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Configuration
@Controller
@RequiredArgsConstructor
@Log
public class WebSocketController {
    private final MessageMapper messageMapper;
    private final WebSocketService webSocketService;


    @MessageMapping("/send")
    public void handleSentMessage(@Payload SentMessageDTO dto){
        MessageDTO messageDTO = messageMapper.toMessageDTO(dto);
        webSocketService.sendMessageToUser(messageDTO.getSender(), messageDTO);
        if(!messageDTO.getSender().equals(messageDTO.getReceiver())) {
            webSocketService.sendMessageToUser(messageDTO.getReceiver(), messageDTO);
        }
    }

    @MessageMapping("/sendAdminBroadcast")
    public void handleSentAdminBroadcast(@Payload SentMessageDTO dto){
        MessageDTO messageDTO = messageMapper.toMessageDTO(dto);
        webSocketService.sendMessageToUser("adminBroadcast", messageDTO);
    }

    @MessageMapping("/notification")
    public void handleNotification(@Payload NotificationDTO dto){
        webSocketService.sendNotificationToUser(dto.getReceiver(), dto);
    }
}
