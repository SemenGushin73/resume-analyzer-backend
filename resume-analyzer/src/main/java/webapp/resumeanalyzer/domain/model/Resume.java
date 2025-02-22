package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность Resume для хранения данных резюме в базе данных.
 */
@Entity
@Table(name = "resume")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resume {

    @Id
    @Column(unique = true)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "personalData_id")
    @NotNull
    private PersonalData personalData;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "education_id")
    private Set<Education> educations;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "experience_id")
    private Set<Experience> experiences;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "socialLink_id")
    private Set<SocialLink> socialLinks;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "hobby_id")
    private Set<Hobby> hobbies;

    private Department department;

    @PrePersist
    private void generateUUID() {
        if (id == null) {
            id = UUID.randomUUID();
            System.out.println("Generated UUID: " + id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resume resume = (Resume) o;
        return Objects.equals(id, resume.id) && Objects.equals(personalData, resume.personalData) && Objects.equals(educations, resume.educations) && Objects.equals(experiences, resume.experiences) && Objects.equals(socialLinks, resume.socialLinks) && Objects.equals(hobbies, resume.hobbies) && department == resume.department;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
