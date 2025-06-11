package dsrl.mariatitianu.chat.service.impl;

import dsrl.mariatitianu.chat.dto.MessageDTO;
import dsrl.mariatitianu.chat.dto.NotificationDTO;
import dsrl.mariatitianu.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendMessageToUser(String user, MessageDTO message) {
        simpMessagingTemplate.convertAndSend("/chat/" + user, message);
    }

    @Override
    public void sendNotificationToUser(String user, NotificationDTO notification) {
        simpMessagingTemplate.convertAndSend("/chat/" + user + "/notifications", notification);
    }
}
