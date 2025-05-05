package ru.kata.project.resumeDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * DTO класс для корректной передачи резюме через kafka.
 * Необходим класс mapper в модуле, который передает резюме в Kafka, для преобразования entity в DTO.
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для передачи данных между API Gateway и Resume-Analyzer")
public class ResumeDto implements Serializable {

    @Schema(hidden = true)
    private UUID id;

    @Schema(
            description = "Персональные данные",
            example = """
                    {
                        "id": "5f285f64-1717-4562-b3fc-2c963f36afa6",
                        "fullName": "Иван Иванов",
                        "address": "г. Москва, ул. Примерная, д. 123",
                        "bio": "Опытный разработчик с 5-летним стажем",
                        "position": "Senior Java Developer",
                        "phne": "79123456789",
                        "website": "https://example.com",
                        "email": "ivan@example.com"
                    }
                    """
    )
    private PersonalDataDto personalData;

    @Schema(
            description = "Образование",
            example = """
                    [
                    {
                    "id": "5f285f64-1717-4562-b3fc-2c363f66afa6",
                    "description": "Факультет компьютерных наук",
                    "position": "Студент",
                    "fromYear": "2010",
                    "toYear": "2015",
                    "name": "Московский государственный университет (МГУ)"
                    }
                    ]
                    """
    )
    private Set<EducationDto> educations;

    @Schema(
            description = "Опыт работы",
            example = """
                    [
                    {
                    "id": "5f285164-5717-4562-b3fc-2c363f66afa6",
                    "description": "Разработка корпоративных приложений на Java",
                    "position": "Java Developer",
                    "fromYear": "2015",
                    "toYear": "2020",
                    "name": "Яндекс"
                    }
                    ]
                    """
    )
    private Set<ExperienceDto> experiences;

    @Schema(
            description = "Социальные связи",
            example = """
                    [
                    {
                    "id": "5f215f64-5717-4562-b3fc-3c963f66afa6",
                    "link": "https://linkedin.com/in/ivanov",
                    "name": "LinkedIn"
                    }
                    ]
                    """
    )
    private Set<SocialLinkDto> socialLinks;

    @Schema(
            description = "Хобби",
            example = """
                    [
                    {
                    "id": "5f285f14-5717-4562-b3fc-3c963f66afa6",
                    "hobby": "Программирование"
                    }
                    ]
                    """
    )
    private Set<HobbyDto> hobbies;
}