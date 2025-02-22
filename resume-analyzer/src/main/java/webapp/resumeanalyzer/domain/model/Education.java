package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность Education для хранения данных резюме в базе данных.
 */
@Entity
@Table(name = "education")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Education {

    @Id
    @Column(unique = true)
    private UUID id;

    private String description;

    private String position;

    @JsonProperty("fromYear")
    @Schema(type = "string", example = "1999")
    private String from_year;

    @JsonProperty("toYear")
    @Schema(type = "string", example = "2000")
    private String to_year;

    private String name;

    @PrePersist
    private void generateUUID() {
        if (id == null) {
            id = UUID.randomUUID();
            System.out.println("Generated UUID: " + id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Education education = (Education) o;
        return id.equals(education.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
