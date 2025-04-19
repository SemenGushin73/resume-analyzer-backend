package webapp.resumegenerator.domain.repository.testfixtures.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalDataDTO {
    @Id
    @Column(unique = true)
    private UUID id = UUID.randomUUID();
    @NotBlank(message = "Full name is required")
    @NotNull
    @Size(min = 3)
    @JsonProperty("fullName")
    private String fullName;
    @JsonProperty("address")
    private String address;
    @Email
    @JsonProperty("mail")
    @Schema(type = "string", example = "string@mail.com")
    private String email;
}


