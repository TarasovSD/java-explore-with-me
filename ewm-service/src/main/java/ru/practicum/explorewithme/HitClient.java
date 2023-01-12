package ru.practicum.explorewithme;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class HitClient {

    private final WebClient webClient;

    public HitClient(WebClient.Builder builder) {
        String basePath = "http://stats-server:9090/";
        this.webClient = builder.baseUrl(basePath).build();
    }

    public List<HitDto> hits(List<Event> events) {
        List<String> uris = new ArrayList<>();
        for (Event event : events) {
            String uri = "/events/" + event.getId();
            uris.add(uri);
        }
        String eventsStr = String.join(",", uris);
        return List.of(Objects.requireNonNull(webClient
                .get()
                .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris={eventsStr}&unique=false", eventsStr)
                .retrieve().bodyToMono(HitDto[].class)
                .block()));
    }
}
