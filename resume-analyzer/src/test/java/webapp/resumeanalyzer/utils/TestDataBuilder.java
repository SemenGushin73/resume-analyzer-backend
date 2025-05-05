package webapp.resumeanalyzer.utils;

import ru.kata.project.resumeDto.EducationDto;
import ru.kata.project.resumeDto.ExperienceDto;
import ru.kata.project.resumeDto.HobbyDto;
import ru.kata.project.resumeDto.PersonalDataDto;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.SocialLinkDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestDataBuilder {

    public static ResumeDto createSampleResume() {
        return createCustomResume(
            "Иван Иванов",
            "Москва",
            "Опытный разработчик",
            "Java Developer",
            "МГУ",
            "Яндекс"
        );
    }

    public static ResumeDto createCustomResume(
        String fullName,
        String location,
        String summary,
        String position,
        String university,
        String company
    ) {
        return new ResumeDto(
            UUID.randomUUID(),
            new PersonalDataDto(
                UUID.randomUUID(),
                fullName,
                location,
                summary,
                position,
                79991234567L,
                "example.com",
                "ivan@example.com"
            ),
            Set.of(
                new EducationDto(
                    UUID.randomUUID(),
                    "Высшее образование",
                    "Студент",
                    "2010",
                    "2015",
                    university
                )
            ),
            Set.of(
                new ExperienceDto(
                    UUID.randomUUID(),
                    "Разработка backend",
                    "Senior Java Developer",
                    "2018",
                    "2023",
                    company
                )
            ),
            Set.of(
                new SocialLinkDto(
                    UUID.randomUUID(),
                    "https://github.com/ivanov",
                    "GitHub"
                )
            ),
            Set.of(
                new HobbyDto(
                    UUID.randomUUID(),
                    "Программирование"
                )
            )
        );
    }

    public static List<ResumeDto> createSampleResumeList(int count) {
        return List.of(
            createCustomResume(
                "Петр Петров",
                "Санкт-Петербург",
                "Middle разработчик",
                "Java Developer",
                "СПбГУ",
                "Тинькофф"
            ),
            createCustomResume(
                "Анна Сидорова",
                "Новосибирск",
                "Frontend разработчик",
                "JavaScript Developer",
                "НГУ",
                "Сбер"
            )
        ).subList(0, Math.min(count, 2));
    }
}