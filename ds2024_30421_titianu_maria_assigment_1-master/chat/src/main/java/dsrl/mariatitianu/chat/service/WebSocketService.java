package dsrl.mariatitianu.chat.service;


import dsrl.mariatitianu.chat.dto.MessageDTO;
import dsrl.mariatitianu.chat.dto.NotificationDTO;

public interface WebSocketService {
    void sendMessageToUser(String user, MessageDTO message);
    void sendNotificationToUser(String user, NotificationDTO notification);
}
