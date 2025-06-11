package dsrl.mariatitianu.devicemanagement.mapper;

import dsrl.mariatitianu.devicemanagement.dto.user.UserDTO;
import dsrl.mariatitianu.devicemanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final DeviceMapper deviceMapper;
    public User toUser(UserDTO dto) {
        return User.builder()
                .id(dto.getUuid())
                .devices(new ArrayList<>())
                .build();
    }

    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .uuid(user.getId())
                .devices(deviceMapper.toDeviceDTOs(user.getDevices()))
                .build();
    }
}
