package webapp.resumeanalyzer.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Сущность: владелец резюме файла - хранится в БД
 */
@Entity
@Table(name = "resume_owners")
@Data
@NoArgsConstructor
@ToString(exclude = "resumeFiles")
@Schema(description = "Сущность: владелец резюме файла - хранится в БД")
@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
public class ResumeOwner implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true)
    @Schema(description = "Идентификатор владельца резюме в БД",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResumeFile> resumeFiles = new ArrayList<>();;

    public ResumeOwner(UUID id, List<ResumeFile> resumeFiles) {
        this.id = id;
        this.resumeFiles = Optional.ofNullable(resumeFiles).map(ArrayList::new).orElse(new ArrayList<>());
    }

    public void addResumeFile(ResumeFile resumeFile) {
        this.resumeFiles.add(resumeFile);
        resumeFile.setOwner(this);
    }

    public void removeResumeFile(ResumeFile resumeFile) {
            this.resumeFiles.remove(resumeFile);
            resumeFile.setOwner(null);

    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<ResumeFile> getResumeFiles() {
        return Collections.unmodifiableList(new ArrayList<>(this.resumeFiles));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setResumeFiles(List<ResumeFile> resumeFiles) {
        this.resumeFiles = Optional.ofNullable(resumeFiles).map(ArrayList::new).orElse(new ArrayList<>());
    }
}
