package webapp.resumeanalyzer.application.mapper;

import org.springframework.stereotype.Component;
import ru.kata.project.resumeDto.*;
import webapp.resumeanalyzer.domain.model.*;

import java.util.stream.Collectors;

/**
 * Класс для преобразования сущности Resume в ResumeDto для передачи через kafka.
 * Преобразовывает как сам Resume, так и зависимые подклассы.
 */

@Component
public class ResumeMapper {

    public static ResumeDto toDto(Resume resume) {
        return new ResumeDto(
                resume.getId(),
                toDto(resume.getPersonalData()),
                resume.getEducations().stream().map(ResumeMapper::toDto).collect(Collectors.toSet()),
                resume.getExperiences().stream().map(ResumeMapper::toDto).collect(Collectors.toSet()),
                resume.getSocialLinks().stream().map(ResumeMapper::toDto).collect(Collectors.toSet()),
                resume.getHobbies().stream().map(ResumeMapper::toDto).collect(Collectors.toSet())
        );
    }

    private static PersonalDataDto toDto(PersonalData data) {
        return new PersonalDataDto(
                data.getId(),
                data.getFull_name(),
                data.getAddress(),
                data.getBio(),
                data.getPosition(),
                data.getPhone(),
                data.getWebsite(),
                data.getEmail());
    }

    private static EducationDto toDto(Education education) {
        return new EducationDto(
                education.getId(),
                education.getDescription(),
                education.getPosition(),
                education.getFrom_year(),
                education.getTo_year(),
                education.getName()
        );
    }

    private static ExperienceDto toDto(Experience experience) {
        return new ExperienceDto(
                experience.getId(),
                experience.getDescription(),
                experience.getPosition(),
                experience.getFrom_year(),
                experience.getTo_year(),
                experience.getName()
        );
    }

    private static SocialLinkDto toDto(SocialLink socialLink) {
        return new SocialLinkDto(
                socialLink.getId(),
                socialLink.getLink(),
                socialLink.getName()
        );
    }

    private static HobbyDto toDto(Hobby hobby) {
        return new HobbyDto(
                hobby.getId(),
                hobby.getHobby()
        );
    }
}
