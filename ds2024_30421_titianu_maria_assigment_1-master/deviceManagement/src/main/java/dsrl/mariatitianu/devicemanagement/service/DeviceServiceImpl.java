package dsrl.mariatitianu.devicemanagement.service;

import com.google.gson.Gson;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.MonitoringMessageType;
import dsrl.mariatitianu.devicemanagement.entity.Device;
import dsrl.mariatitianu.devicemanagement.entity.User;
import dsrl.mariatitianu.devicemanagement.mapper.DeviceMapper;
import dsrl.mariatitianu.devicemanagement.repository.DeviceRepository;
import dsrl.mariatitianu.devicemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceMapper deviceMapper;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    private final RabbitTemplate rabbitTemplate;
    private final Gson gson;
    @Value("${rabbitmq.device.queue}")
    private String deviceEventQueue;



    @Override
    public Optional<DeviceDTO> create(DeviceCreateDTO dto) {
        Optional<User> userOptional = userRepository.findById(dto.getUserUuid());
        if(userOptional.isEmpty()){
            return Optional.empty();
        }
        Device device = deviceRepository.save(deviceMapper.toDevice(dto, userOptional.get()));
        rabbitTemplate.convertAndSend(deviceEventQueue,
                gson.toJson(deviceMapper.toMonitoringDeviceDTO(device)),
                m -> {
                    m.getMessageProperties().setType(MonitoringMessageType.CREATE.name());
                    return m;
                }
        );
        return Optional.of(deviceMapper.toDeviceDTO(device));
    }

    @Override
    public Optional<DeviceDTO> update(UUID uuid, DeviceUpdateDTO dto) {
        Optional<User> userOptional = Optional.empty();
        if(dto.getUserUuid() != null){
            userOptional = userRepository.findById(dto.getUserUuid());
        }
        if(userOptional.isEmpty() && dto.getUserUuid() != null){
            return Optional.empty();
        }
        Optional<Device> oldDevice = deviceRepository.findById(uuid);
        if (oldDevice.isEmpty()) {
            return Optional.empty();
        }
        Device newDevice = deviceRepository.save(deviceMapper.updateDevice(oldDevice.get(), dto, userOptional));
        rabbitTemplate.convertAndSend(deviceEventQueue,
                gson.toJson(deviceMapper.toMonitoringDeviceDTO(newDevice)),
                m -> {
                    m.getMessageProperties().setType(MonitoringMessageType.UPDATE.name());
                    return m;
                }
        );
        return Optional.of(deviceMapper.toDeviceDTO(newDevice));
    }

    @Override
    public List<DeviceDTO> findAll() {
        return deviceMapper.toDeviceDTOs(deviceRepository.findAll());
    }


    @Override
    public Optional<DeviceDTO> find(UUID uuid) {
        Optional<Device> userOptional = deviceRepository.findById(uuid);
        return userOptional.map(deviceMapper::toDeviceDTO);
    }

    @Override
    public Optional<DeviceDTO> delete(UUID uuid) {
        Optional<Device> device = deviceRepository.findById(uuid);
        if (device.isEmpty()) {
            return Optional.empty();
        }
        rabbitTemplate.convertAndSend(deviceEventQueue,
                gson.toJson(deviceMapper.toMonitoringDeviceDTO(device.get())),
                m -> {
                    m.getMessageProperties().setType(MonitoringMessageType.DELETE.name());
                    return m;
                }
        );
        deviceRepository.deleteById(device.get().getId());
        return Optional.of(deviceMapper.toDeviceDTO(device.get()));
    }

    @Override
    public Optional<List<DeviceDTO>> deleteDevicesAndUser(UUID userUuid){
        if(userRepository.findById(userUuid).isEmpty()){
            return Optional.empty();
        }
        List<Device> deviceList = deviceRepository.findAllByUserId(userUuid);
        userRepository.deleteById(userUuid);
        deviceRepository.deleteAll(deviceList);
        deviceList.forEach(device -> rabbitTemplate.convertAndSend(deviceEventQueue,
                gson.toJson(deviceMapper.toMonitoringDeviceDTO(device)),
                m -> {
                    m.getMessageProperties().setType(MonitoringMessageType.DELETE.name());
                    return m;
                }
        ));
        return Optional.of(deviceMapper.toDeviceDTOs(deviceList));
    }
}
