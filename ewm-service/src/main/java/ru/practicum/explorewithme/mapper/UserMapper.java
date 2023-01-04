package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.UserDto;
import ru.practicum.explorewithme.model.User;

public class UserMapper {
    public static User toUser(UserDto userDto, Long id) {
        return new User(id, userDto.getName(), userDto.getEmail());
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
