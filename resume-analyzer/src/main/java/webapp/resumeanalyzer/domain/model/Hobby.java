package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность Hobby для хранения данных резюме в базе данных.
 */
@Entity
@Table(name = "hobby")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Hobby {

    @Id
    @Column(unique = true)
    private UUID id = UUID.randomUUID();

    private String hobby;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Hobby hobby = (Hobby) o;
        return id.equals(hobby.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
