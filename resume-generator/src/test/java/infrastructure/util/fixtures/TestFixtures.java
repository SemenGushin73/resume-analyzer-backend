package infrastructure.util.fixtures;
import com.fasterxml.jackson.annotation.JsonInclude;
import webapp.resumegenerator.domain.repository.testfixtures.dto.PersonalDataDTO;
import webapp.resumegenerator.domain.repository.testfixtures.dto.ResumeRequest;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestFixtures {
    public static ResumeRequest minimalValidResume() {
        ResumeRequest request = new ResumeRequest();
        PersonalDataDTO personalData = new PersonalDataDTO();
        personalData.setFullName("Иван Иванов"); // Обязательное поле
        personalData.setEmail("ivan@example.com"); // Обязательное, если есть @Email валидация
        request.setPersonalDataDTO(personalData);
        return request;
    }
}
