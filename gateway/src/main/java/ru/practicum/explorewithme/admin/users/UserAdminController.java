package ru.practicum.explorewithme.admin.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@Validated
public class UserAdminController {

    private final WebClient webClient;

    public UserAdminController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9092/").build();
    }

    @GetMapping()
    public List<UserDto> getUsers() {
        log.info("Запрос пользователей");
        List<UserDto> userDto = webClient
                .get()
                .uri("/admin/users")
                .retrieve()
                .bodyToMono(List.class)
                .block();
        return userDto;
    }

    @PostMapping()
    public UserDto createUser(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("Создание нового пользователя");
        return webClient
                .post()
                .uri("/admin/users")
                .body(Mono.just(userDto), UserDto.class)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    @DeleteMapping("/{id}")
    public String removeUserById(@PathVariable Long id) {
        log.info("Удаление пользователя");
        return webClient
                .delete()
                .uri("/admin/users/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
