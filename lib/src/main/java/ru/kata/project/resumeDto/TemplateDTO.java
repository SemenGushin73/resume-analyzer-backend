package ru.kata.project.resumeDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.kata.project.model.Department;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачи данных о шаблоне резюме.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateDTO {

    private UUID id;

    @NotNull(message = "Имя шаблона не может быть пустым.")
    @Size(min = 3, message = "Имя шаблона должно содержать 3 и более символов.")
    private String name;

    @NotNull(message = "Описание шаблона не может быть пустым.")
    @Size(max = 500, message = "Содержание должно содержать не более 500 символов.")
    private String description;

    @NotNull(message = "Контент шаблона не может быть пустым")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer version;

    private Department department;
}