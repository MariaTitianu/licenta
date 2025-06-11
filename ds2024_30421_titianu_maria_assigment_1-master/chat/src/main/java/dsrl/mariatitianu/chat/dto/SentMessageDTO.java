package dsrl.mariatitianu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentMessageDTO {
    private String sender;
    private String receiver;
    private String content;
}
