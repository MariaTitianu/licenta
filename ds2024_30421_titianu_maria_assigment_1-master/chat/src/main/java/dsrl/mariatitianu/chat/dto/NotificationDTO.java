package dsrl.mariatitianu.chat.dto;

import dsrl.mariatitianu.chat.enums.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String sender;
    private String receiver;
    private Notification notification;
}
