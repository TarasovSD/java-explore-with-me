package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final HitService hitService;

    @PostMapping("/hit")
    public HitDto create(@RequestBody HitDto hitDto) {
        log.info("Данные сохранены в БД статистики");
        return hitService.create(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> get(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                      @RequestParam(defaultValue = "") List<String> uris,
                                      @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Данные получены из БД статистики");
        List<ViewStatsDto> viewStatsDtoList = hitService.get(start, end, uris, unique);
        return viewStatsDtoList;
    }
}
