package ru.practicum.explorewithme.service.user;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> find(List<Long> ids, PageRequest pageRequest);

    UserDto create(UserDto userDto);

    void removeById(Long id);
}
