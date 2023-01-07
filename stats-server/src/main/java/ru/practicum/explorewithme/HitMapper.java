package ru.practicum.explorewithme;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;

@UtilityClass
public class HitMapper {
    public Hit toHit(HitDto hitDto, LocalDateTime timestamp) {
        return new Hit(hitDto.getId(),
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                timestamp);
    }

    public HitDto toHitDto(Hit hit) {
        return new HitDto(hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp());
    }
}
