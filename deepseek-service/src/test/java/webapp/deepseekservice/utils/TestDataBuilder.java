package webapp.deepseekservice.utils;

import ru.kata.project.resumeDto.EducationDto;
import ru.kata.project.resumeDto.ExperienceDto;
import ru.kata.project.resumeDto.HobbyDto;
import ru.kata.project.resumeDto.PersonalDataDto;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.SocialLinkDto;
import webapp.deepseek.infrastructure.client.OpenRouterResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestDataBuilder {

    public static ResumeDto createSampleResume() {
        return new ResumeDto(
                UUID.randomUUID(),
                new PersonalDataDto(
                        UUID.randomUUID(),
                        "Иван Иванов",
                        "Москва",
                        "Опытный разработчик",
                        "Java Developer",
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
                                "МГУ"
                        )
                ),
                Set.of(
                        new ExperienceDto(
                                UUID.randomUUID(),
                                "Разработка backend",
                                "Senior Java Developer",
                                "2018",
                                "2023",
                                "Яндекс"
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

    public static OpenRouterResponse createMockResponse(String content) {
        OpenRouterResponse.Message message = new OpenRouterResponse.Message();
        message.setContent(content);

        OpenRouterResponse.Choice choice = new OpenRouterResponse.Choice();
        choice.setMessage(message);

        OpenRouterResponse mockResponse = new OpenRouterResponse();
        mockResponse.setChoices(List.of(choice));

        return mockResponse;
    }
}
