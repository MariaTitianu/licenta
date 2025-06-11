package dsrl.mariatitianu.usermanagement.mapper;

import dsrl.mariatitianu.usermanagement.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import dsrl.mariatitianu.usermanagement.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toUser(UserCreateDTO dto) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(dto.getUsername())
                .name(dto.getName())
                .build();
    }

    public UserExtraDTO toUserExtraDTO(User user) {
        return UserExtraDTO.builder()
                .uuid(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    public User updateUser(User user, UserUpdateDTO dto) {
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            user.setName(dto.getName());
        }
        return user;
    }

    public List<UserExtraDTO> toUserExtraDTOs(Collection<User> users) {
        return users.stream()
                .map(this::toUserExtraDTO)
                .toList();
    }

    public UserCreateUuidDTO toUserCreateUuidDTO(User user) {
        return UserCreateUuidDTO.builder()
                .uuid(user.getId())
                .build();
    }
}
