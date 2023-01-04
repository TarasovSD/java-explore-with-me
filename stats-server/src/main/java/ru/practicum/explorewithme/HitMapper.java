package ru.practicum.explorewithme;

import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {
    public static Hit toHit(HitDto hitDto, LocalDateTime timestamp) {
        return new Hit(hitDto.getId(),
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                timestamp);
    }

    public static HitDto toHitDto(Hit hit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = hit.getTimestamp().format(formatter);
        return new HitDto(hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                timestamp);
    }
}
