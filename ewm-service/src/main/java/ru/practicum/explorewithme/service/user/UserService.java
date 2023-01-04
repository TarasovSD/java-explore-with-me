package ru.practicum.explorewithme.service.user;

import ru.practicum.explorewithme.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findUsers();

    UserDto createUser(UserDto userDto);

    void removeUserById(Long id);
}
