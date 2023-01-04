package ru.practicum.explorewithme.admin.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.UserDto;
import ru.practicum.explorewithme.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserDto> findUsers() {
        log.info("Выполнен запрос findUsers");
        return userService.findUsers();
    }

    @PostMapping()
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Пользователь создан");
        return userService.createUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        log.info("Запрос на удаление юзера с ID:" + id);
        userService.removeUserById(id);
    }
}
