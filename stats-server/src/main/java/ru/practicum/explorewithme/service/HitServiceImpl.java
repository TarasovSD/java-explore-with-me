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
    public HitDto create(HitDto hitDto) {
        LocalDateTime timestamp = hitDto.getTimestamp();
        Hit hitToSave = HitMapper.toHit(hitDto, timestamp);
        return HitMapper.toHitDto(hitRepository.save(hitToSave));
    }

    @Override
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<HitDto> statsDtoList = new ArrayList<>();
        List<String> ipList = new ArrayList<>();
        List<String> uriList = new ArrayList<>();
        if (uris.isEmpty()) {
            List<Hit> hits = hitRepository.getHits(start, end);
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
            List<Hit> hits = hitRepository.getHitsForUris(start, end, uris);
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
        List<Hit> hits = hitRepository.findAllByUris(uris);
        for (String uri : uriList) {
            List<Hit> hitsCount = new ArrayList<>();
            for (Hit hit : hits) {
                if (hit.getUri().equals(uri)) {
                    hitsCount.add(hit);
                }
            }
            Long hitsSize = (long) hitsCount.size();
            String appName = hitsCount.get(0).getApp();
            ViewStatsDto viewStatsDto = new ViewStatsDto(appName, uri, hitsSize);
            viewStatsDtoList.add(viewStatsDto);
        }
        return viewStatsDtoList;
    }
}
