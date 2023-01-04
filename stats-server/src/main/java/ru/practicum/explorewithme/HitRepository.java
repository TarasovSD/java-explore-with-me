package ru.practicum.explorewithme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query("select h from Hit h where h.timestamp > :startTime and h.timestamp < :endTime")
    List<Hit> getHits(LocalDateTime startTime, LocalDateTime endTime);

    @Query("select h from Hit h where h.timestamp > :startTime and h.timestamp < :endTime and h.uri in :uris")
    List<Hit> getHitsForUris(LocalDateTime startTime, LocalDateTime endTime, List<String> uris);

    List<Hit> findAllByUri(String uri);
}
