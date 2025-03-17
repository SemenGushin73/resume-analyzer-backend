package webapp.deepseek.application.mapper;

import com.example.kafkadto.domain.model.ResumeDto;
import org.springframework.stereotype.Component;

@Component
public class ResumeMapper {

    public static Resume toEntity(ResumeDto dto) {
        return new Resume(
                dto.getId(),
                PersonalDataMapper.toEntity(dto.getPersonalData()),
                toEntity(dto.getEducations()),
                toEntity(dto.getExperiences()),
                toEntity(dto.getSocialLinks()),
                toEntity(dto.getHobbies())
        );
    }

    private static Set<Education> toEntity(Set<EducationDto> dtos) {
        return dtos.stream().map(ResumeMapper::toEntity).collect(Collectors.toSet());
    }

    private static Education toEntity(EducationDto dto) {
        return new Education(
                dto.getId(),
                dto.getDescription(),
                dto.getPosition(),
                dto.getFrom_year(),
                dto.getTo_year(),
                dto.getName()
        );
    }
}
