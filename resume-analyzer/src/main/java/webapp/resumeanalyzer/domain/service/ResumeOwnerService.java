package webapp.resumeanalyzer.domain.service;

import webapp.resumeanalyzer.domain.model.ResumeOwner;

/**
 * Интерфейс CRUD методов для сервиса сущности ResumeOwner.
 */
public interface ResumeOwnerService {
    ResumeOwner findById(String ownerId);
}
