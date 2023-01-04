package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;

import java.util.List;

public interface HitService {
    HitDto createHit(HitDto hitDto);

    List<ViewStatsDto> getHits(String start, String end, List<String> uris, Boolean unique);
}
