package ru.practicum.explorewithme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.HitMapper;
import ru.practicum.explorewithme.HitRepository;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    public HitServiceImpl(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Override
    public HitDto createHit(HitDto hitDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime timestamp = LocalDateTime.parse(hitDto.getTimestamp(), formatter);
        Hit hitToSave = HitMapper.toHit(hitDto, timestamp);
        return HitMapper.toHitDto(hitRepository.save(hitToSave));
    }

    @Override
    public List<ViewStatsDto> getHits(String start, String end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);
        List<HitDto> statsDtoList = new ArrayList<>();
        List<String> ipList = new ArrayList<>();
        List<String> uriList = new ArrayList<>();
        if (uris.isEmpty()) {
            List<Hit> hits = hitRepository.getHits(startTime, endTime);
            if (unique) {
                for (Hit hit : hits) {
                    if (!ipList.contains(hit.getIp())) {
                        ipList.add(hit.getIp());
                        statsDtoList.add(HitMapper.toHitDto(hit));
                        uriList.add(hit.getUri());
                    }
                }
            } else {
                for (Hit hit : hits) {
                    ipList.add(hit.getIp());
                    statsDtoList.add(HitMapper.toHitDto(hit));
                    uriList.add(hit.getUri());
                }
            }
        } else {
            List<Hit> hits = hitRepository.getHitsForUris(startTime, endTime, uris);
            if (unique) {
                for (Hit hit : hits) {
                    if (!ipList.contains(hit.getIp())) {
                        ipList.add(hit.getIp());
                        statsDtoList.add(HitMapper.toHitDto(hit));
                        uriList.add(hit.getUri());
                    }
                }
            } else {
                for (Hit hit : hits) {
                    ipList.add(hit.getIp());
                    statsDtoList.add(HitMapper.toHitDto(hit));
                    uriList.add(hit.getUri());
                }
            }
        }
        List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();
        for (String uri : uriList) {
            List<Hit> hits = hitRepository.findAllByUri(uri);
            Long hitsSize = (long) hits.size();
            ViewStatsDto viewStatsDto = new ViewStatsDto("ewm-service", uri, hitsSize);
            viewStatsDtoList.add(viewStatsDto);
        }
        return viewStatsDtoList;
    }
}
