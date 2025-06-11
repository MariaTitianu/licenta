package dsrl.mariatitianu.security.mapper;

import dsrl.mariatitianu.security.dto.user.auth.UserAuthDTO;
import dsrl.mariatitianu.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserAuthMapper {
    public UserAuthDTO toUserAuthDTO(User user){
        return UserAuthDTO.builder()
                .username(user.getUsername())
                .userRole(user.getRole())
                .build();
    }

    public List<UserAuthDTO> toUserAuthDTOList(List<User> userList){
        return userList.stream()
                .map(this::toUserAuthDTO)
                .toList();
    }

    public Map<String, UserAuthDTO> toUserDTOMap(List<User> userList) {
        return this.toUserAuthDTOList(userList).stream()
                .collect(Collectors.toMap(UserAuthDTO::getUsername, Function.identity()));
    }
}
