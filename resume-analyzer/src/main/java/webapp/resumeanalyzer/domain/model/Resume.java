package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import java.io.Serial;
import java.io.Serializable;

import java.util.*;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * Сущность Resume для хранения данных резюме в базе данных.
 */
@Entity
@Table(name = "resume")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resume implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @Column(unique = true)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "personalData_id")
    @NotNull
    @SuppressFBWarnings("EI_EXPOSE_REP")
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

    public Resume() {
        this.educations = new HashSet<>();
        this.experiences = new HashSet<>();
        this.socialLinks = new HashSet<>();
        this.hobbies = new HashSet<>();
    }

    public Resume(UUID id, PersonalData personalData, Set<Education> educations, Set<Experience> experiences, Set<SocialLink> socialLinks, Set<Hobby> hobbies) {
        this.id = id;
        this.personalData = personalData;
        this.educations = Optional.ofNullable(educations).map(HashSet::new).orElse(new HashSet<>());
        this.experiences = Optional.ofNullable(experiences).map(HashSet::new).orElse(new HashSet<>());
        this.socialLinks = Optional.ofNullable(socialLinks).map(HashSet::new).orElse(new HashSet<>());
        this.hobbies = Optional.ofNullable(hobbies).map(HashSet::new).orElse(new HashSet<>());
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Set<Education> getEducations() {
        return Collections.unmodifiableSet(new HashSet<>(this.educations));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Set<Experience> getExperiences() {
        return Collections.unmodifiableSet(new HashSet<>(this.experiences));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Set<SocialLink> getSocialLinks() {
        return Collections.unmodifiableSet(new HashSet<>(this.socialLinks));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Set<Hobby> getHobbies() {
        return Collections.unmodifiableSet(new HashSet<>(this.hobbies));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setEducations(Set<Education> educations) {
        this.educations = Optional.ofNullable(educations).map(HashSet::new).orElse(null);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setExperiences(Set<Experience> experiences) {
        this.experiences = Optional.ofNullable(experiences).map(HashSet::new).orElse(null);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setSocialLinks(Set<SocialLink> socialLinks) {
        this.socialLinks = Optional.ofNullable(socialLinks).map(HashSet::new).orElse(null);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setHobbies(Set<Hobby> hobbies) {
        this.hobbies = Optional.ofNullable(hobbies).map(HashSet::new).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Resume resume = (Resume) o;
        return Objects.equals(id, resume.id) &&
                Objects.equals(personalData, resume.personalData) &&
                Objects.equals(educations, resume.educations) &&
                Objects.equals(experiences, resume.experiences) &&
                Objects.equals(socialLinks, resume.socialLinks) &&
                Objects.equals(hobbies, resume.hobbies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personalData, educations, experiences, socialLinks, hobbies);
    }
}