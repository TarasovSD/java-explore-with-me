package ru.practicum.explorewithme.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HitDto {

    private Long id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}
