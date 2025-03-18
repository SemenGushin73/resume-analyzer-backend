package com.example.kafkadto.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO класс для корректной передачи компонента резюме "Education" через kafka
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EducationDto implements Serializable {

    private UUID id;
    private String description;
    private String position;
    private String fromYear;
    private String toYear;
    private String name;

}
