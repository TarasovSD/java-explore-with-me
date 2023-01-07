package ru.practicum.explorewithme.admin.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.dto.UserDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@Validated
public class UserAdminController {

    private final WebClient webClient;

    public UserAdminController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @GetMapping()
    public UserDto[] get(@RequestParam(required = false) List<Long> ids,
                         @PositiveOrZero @RequestParam(defaultValue = "0") Long from,
                         @Positive @RequestParam(defaultValue = "10") Long size) {
        log.info("Запрос пользователей");
        String idsStr;
        String uri;
        if (ids != null) {
            if (ids.size() == 0) {
                idsStr = "";
            } else {
                idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            }
            uri = "/admin/users?ids=" + idsStr + "&from=" + from + "&size=" + size;
        } else {
            uri = "/admin/users?&from=" + from + "&size=" + size;
        }
        UserDto[] userDto = webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(UserDto[].class)
                .block();
        return userDto;
    }

    @PostMapping()
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
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
    public String removeById(@PathVariable Long id) {
        log.info("Удаление пользователя");
        return webClient
                .delete()
                .uri("/admin/users/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
