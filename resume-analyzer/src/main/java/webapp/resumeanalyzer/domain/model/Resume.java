package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

import java.util.Set;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resume implements Serializable {

    @Id
    @UuidGenerator
    @Column(unique = true)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "personalData_id")
    @NotNull
    private PersonalData personalData;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Set<Education> educations;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Set<Experience> experiences;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Set<SocialLink> socialLinks;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Set<Hobby> hobbies;

}