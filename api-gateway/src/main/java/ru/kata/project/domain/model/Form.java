package ru.kata.project.domain.model;

import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.Department;


/**
 * Анкета заполняемая на фронте для генерации резюме
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Form {

    private final UUID id = UUID.randomUUID();

    private String personalData;

    private String educations;

    private String experiences;

    private String socialLinks;

    private String hobbies;

    private Department department;

    @Override
    public String toString() {
        return "Form{" +
               "personalData='" + personalData + '\'' +
               ", educations='" + educations + '\'' +
               ", experiences='" + experiences + '\'' +
               ", socialLinks='" + socialLinks + '\'' +
               ", hobbies='" + hobbies + '\'' +
               ", department=" + department +
               '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Form form = (Form) o;
        return Objects.equals(personalData, form.personalData) &&
               Objects.equals(educations, form.educations) &&
               Objects.equals(experiences, form.experiences) &&
               Objects.equals(socialLinks, form.socialLinks) &&
               Objects.equals(hobbies, form.hobbies) && department == form.department;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalData, educations, experiences, socialLinks, hobbies,
                department);
    }
}
