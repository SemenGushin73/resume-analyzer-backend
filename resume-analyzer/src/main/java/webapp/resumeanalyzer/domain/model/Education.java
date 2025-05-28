package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
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
public class Education implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true)
    private UUID id = UUID.randomUUID();

    private String description;

    private String position;

    @JsonProperty("fromYear")
    private String from_year;

    @JsonProperty("toYear")
    private String to_year;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Education education = (Education) o;
        return id.equals(education.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
