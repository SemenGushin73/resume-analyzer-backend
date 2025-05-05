package webapp.resumeanalyzer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import webapp.resumeanalyzer.domain.model.ResumeFile;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс репозитория для выполнения CRUD-операций для сущности ResumeFile в БД.
 */

@Repository
public interface ResumeFileRepository extends JpaRepository<ResumeFile, UUID> {

    //метод для поиска по имени
    @Query("SELECT r FROM ResumeFile r WHERE lower(r.filename) = lower(concat('%', :filename,'%'))")
    ResumeFile findByFilenameContainingIgnoreCase(@Param("filename") String filename);

    //метод для поиска по Id владельца
    @Query("SELECT r FROM ResumeFile r WHERE r.owner.id = :ownerId")
    List<ResumeFile> findByOwnerId(@Param("ownerId") UUID ownerId);
}
