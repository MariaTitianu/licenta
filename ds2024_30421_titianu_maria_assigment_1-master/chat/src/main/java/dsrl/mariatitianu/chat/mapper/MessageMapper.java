package dsrl.mariatitianu.chat.mapper;

import dsrl.mariatitianu.chat.dto.MessageDTO;
import dsrl.mariatitianu.chat.dto.SentMessageDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageMapper {
    public MessageDTO toMessageDTO(SentMessageDTO message){
        return MessageDTO.builder()
                .sender(message.getSender())
                .receiver(message.getReceiver())
                .timestamp(LocalDateTime.now())
                .content(message.getContent())
                .build();
    }
}
