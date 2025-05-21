package com.ProjectGraduation.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
public class EventDto {



    private String name;
    private String description;
    private String media;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;

}
