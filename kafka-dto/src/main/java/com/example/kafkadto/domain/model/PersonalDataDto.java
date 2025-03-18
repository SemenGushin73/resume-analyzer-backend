package com.example.kafkadto.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO класс для корректной передачи компонента резюме "PersonalData" через kafka
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalDataDto implements Serializable {

    private UUID id;
    private String fullName;
    private String address;
    private String bio;
    private String position;
    private Long phone;
    private String website;
    private String email;

}
