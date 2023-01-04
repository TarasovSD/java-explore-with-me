package ru.practicum.explorewithme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.service.HitService;

import java.util.List;

@RestController
@RequestMapping()
@Slf4j
public class StatsController {

    private final HitService hitService;

    public StatsController(HitService hitService) {
        this.hitService = hitService;
    }


    @PostMapping("/hit")
    public HitDto createHit(@RequestBody HitDto hitDto) {
        log.info("Данные сохранены в БД статистики");
        return hitService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getHits(@RequestParam String start,
                                      @RequestParam String end,
                                      @RequestParam(defaultValue = "") List<String> uris,
                                      @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Данные получены из БД статистики");
        List<ViewStatsDto> viewStatsDtoList = hitService.getHits(start, end, uris, unique);
        return viewStatsDtoList;
    }
}
