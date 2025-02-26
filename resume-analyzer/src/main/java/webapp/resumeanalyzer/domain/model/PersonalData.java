package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность PersonalData для хранения данных резюме в базе данных.
 */
@Entity
@Table(name = "personal_data")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalData {

    @Id
    @Column(unique = true)
    private UUID id=UUID.randomUUID();

    @NotBlank
    @NotNull
    @Size(min = 3)
    @JsonProperty("fullName")
    private String full_name;

    @JsonProperty("adress")
    private String address;

    private String bio;

    private String position;

    private Long phone;

    private String website;

    @Email
    @JsonProperty("mail")
    @Schema(type = "string", example = "string@mail.com")
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonalData that = (PersonalData) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
