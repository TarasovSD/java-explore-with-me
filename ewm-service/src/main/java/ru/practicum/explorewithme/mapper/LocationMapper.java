package ru.practicum.explorewithme.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.model.Location;

@UtilityClass
public class LocationMapper {
    public Location toLocation(EventDto.LocationDto locationDto) {
        return new Location(locationDto.getLat(), locationDto.getLon());
    }
}
