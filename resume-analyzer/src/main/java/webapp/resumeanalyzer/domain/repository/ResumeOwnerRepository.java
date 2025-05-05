package webapp.resumeanalyzer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webapp.resumeanalyzer.domain.model.ResumeOwner;

import java.util.UUID;

/**
 * Интерфейс репозитория для выполнения CRUD-операций для сущности ResumeOwner в БД.
 */
@Repository
public interface ResumeOwnerRepository extends JpaRepository<ResumeOwner, UUID> {

}
