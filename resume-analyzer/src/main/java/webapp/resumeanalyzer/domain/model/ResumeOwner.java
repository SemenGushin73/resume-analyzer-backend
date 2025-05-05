package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность: владелец резюме файла - хранится в БД
 */
@Entity
@Table(name = "resume_owners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "resumeFiles")
@Schema(description = "Сущность: владелец резюме файла - хранится в БД")
public class ResumeOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true)
    @Schema(description = "Идентификатор владельца резюме в БД",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResumeFile> resumeFiles;

    public void addResumeFile(ResumeFile resumeFile) {
        if (this.resumeFiles == null) {
            this.resumeFiles = new ArrayList<>();
        }

        this.resumeFiles.add(resumeFile);
        resumeFile.setOwner(this);
    }

    public void removeResumeFile(ResumeFile resumeFile) {
        if (this.resumeFiles != null) {
            this.resumeFiles.remove(resumeFile);
            resumeFile.setOwner(null);
        }
    }
}
