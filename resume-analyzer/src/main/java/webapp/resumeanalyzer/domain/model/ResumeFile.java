package webapp.resumeanalyzer.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность: ResumeFile для хранения файлов резюме в базе данных.
 */
@Entity
@Table(name = "resume_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность: ResumeFile для хранения " +
        "файлов резюме в базе данных.")
public class ResumeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true)
    @Schema(description = "Идентификатор файла резюме в БД",
            example = "f9876543-210e-dcba-9876-543210fedcba")
    private UUID id;

    @Column(unique = true)
    @Schema(description = "Имя файл резюме",
            example = "CV_Senior_Java")
    private String filename;

    @Schema(description = "Тип файла резюме",
            example = "application/pdf")
    private String fileType;

    @Schema(description = "Размер файла",
            example = "512")
    private long size;

    @Schema(description = "Данные файла резюме")
    private byte[] data;

    @Schema(description = "Дата создания резюме файла",
            example = "2007-12-03T10:15:30")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @Schema(description = "Владелец резюме файла",
            implementation = ResumeOwner.class)
    private ResumeOwner owner;

    public ResumeFile(String filename, ResumeOwner owner) {
        this.filename = filename;
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResumeFile that = (ResumeFile) o;
        return size == that.size && Objects.equals(id, that.id) &&
               Objects.equals(filename, that.filename) &&
               Objects.equals(fileType, that.fileType) &&
               Objects.deepEquals(data, that.data) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename, fileType, size, Arrays.hashCode(data), createdAt, owner);
    }
}
