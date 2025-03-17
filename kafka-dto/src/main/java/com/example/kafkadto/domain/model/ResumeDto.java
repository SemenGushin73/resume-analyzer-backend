package com.example.kafkadto.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

import java.util.Set;
import java.util.UUID;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeDto implements Serializable {

    private UUID id;
    private PersonalDataDto personalData;
    private Set<EducationDto> educations;
    private Set<ExperienceDto> experiences;
    private Set<SocialLinkDto> socialLinks;
    private Set<HobbyDto> hobbies;

}