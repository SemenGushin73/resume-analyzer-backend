package ru.kata.project.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * POJO для вывода резюме с соответствующими Шаблонами
 */
@NoArgsConstructor
@Data
public class ResumeWithTemplate {

    private List<TemplateDTO> templates = new ArrayList<>();
    private ResumeDto resume;

    public ResumeWithTemplate(List<TemplateDTO> templates, ResumeDto resume) {
        if (templates != null) {
            this.templates = new ArrayList<>(templates);
        } else {
            this.templates = new ArrayList<>();
        }
        if (resume != null) {
            this.resume = new ResumeDto(resume);
        } else {
            this.resume = null;
        }
    }

    public ResumeWithTemplate(ResumeDto resume) {
        if (resume != null) {
            this.resume = new ResumeDto(resume);
        } else {
            this.resume = null;
        }
    }

    public List<TemplateDTO> getTemplates() {
        return new ArrayList<>(templates);
    }

    public void setTemplates(List<TemplateDTO> templates) {
        if (templates != null) {
            this.templates = new ArrayList<>(templates);
        } else {
            this.templates = new ArrayList<>();
        }
    }

    public ResumeDto getResume() {
        if (resume != null) {
            return new ResumeDto(resume);
        }
        return null;
    }

    public void setResume(ResumeDto resume) {
        if (resume != null) {
            this.resume = new ResumeDto(resume);
        } else {
            this.resume = null;
        }
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResumeWithTemplate that = (ResumeWithTemplate) o;
        return Objects.equals(templates, that.templates) && Objects.equals(resume, that.resume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templates, resume);
    }
}