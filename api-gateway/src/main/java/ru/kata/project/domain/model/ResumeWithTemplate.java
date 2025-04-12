package ru.kata.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;


import java.util.List;
import java.util.Objects;

/**
 * POJO для вывода резюме с соответствующими Шаблонами
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResumeWithTemplate {

    private List<TemplateDTO> templates;
    private ResumeDto resume;

    public ResumeWithTemplate(ResumeDto resume) {
        this.resume = resume;
    }

    public void addTemplate(TemplateDTO template) {
        templates.add(template);
    }

    @Override
    public String toString() {
        return "ResumeWithTemplate{" +
                "templates=" + templates +
                ", resume=" + resume +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumeWithTemplate that = (ResumeWithTemplate) o;
        return Objects.equals(templates, that.templates) && Objects.equals(resume, that.resume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templates, resume);
    }
}