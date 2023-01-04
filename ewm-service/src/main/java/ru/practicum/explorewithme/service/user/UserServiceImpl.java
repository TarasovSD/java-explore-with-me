package ru.practicum.explorewithme.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.UserDto;
import ru.practicum.explorewithme.exceptions.UserNameAlreadyExistException;
import ru.practicum.explorewithme.exceptions.UserNotFoundException;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public UserServiceImpl(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }


    @Override
    public List<UserDto> findUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for (User u : users) {
            userDtos.add(UserMapper.toUserDto(u));
        }
        return userDtos;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto, 1L);
        Optional<User> foundUserOpt = userRepository.findByName(userDto.getName());
        if (foundUserOpt.isPresent()) {
            throw new UserNameAlreadyExistException("Имя пользователя уже существует");
        }
        user.setEvents(eventRepository.findEventsByInitiatorId(user));
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void removeUserById(Long id) {
        User userToRemove = userRepository.findById(id).get();
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        userRepository.delete(userToRemove);
    }
}
