package webapp.resumegenerator.domain.repository.testfixtures.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeRequest {
    @Valid
    @NotNull
    private PersonalDataDTO personalDataDTO;
}




