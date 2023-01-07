package ru.practicum.explorewithme.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.UserDto;
import ru.practicum.explorewithme.exceptions.UserNotFoundException;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public List<UserDto> find(List<Long> ids, PageRequest pageRequest) {
        List<UserDto> userDtos;
        if (ids == null || ids.isEmpty()) {
            userDtos = userRepository.findAll(pageRequest).stream().map(UserMapper::toUserDto).collect(Collectors.toList());
        } else {
            userDtos = userRepository.getUsersById(ids, pageRequest).stream().map(UserMapper::toUserDto).collect(Collectors.toList());
        }
        return userDtos;
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        User userForDelete = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        userRepository.delete(userForDelete);
    }
}
