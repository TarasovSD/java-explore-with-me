package ru.practicum.explorewithme.admin.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.UserDto;
import ru.practicum.explorewithme.service.user.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserDto> find(@RequestParam(required = false) List<Long> ids,
                              @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                              @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Выполнен запрос findUsers");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return userService.find(ids, pageRequest);
    }

    @PostMapping()
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Пользователь создан");
        return userService.create(userDto);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable Long id) {
        log.info("Запрос на удаление юзера с ID {}", id);
        userService.removeById(id);
    }
}
