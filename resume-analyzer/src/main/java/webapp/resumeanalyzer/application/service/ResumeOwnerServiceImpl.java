package webapp.resumeanalyzer.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webapp.resumeanalyzer.domain.model.ResumeOwner;
import webapp.resumeanalyzer.domain.repository.ResumeOwnerRepository;
import webapp.resumeanalyzer.domain.service.ResumeOwnerService;

import java.util.UUID;

/**
 * Сервис CRUD методов сущности ResumeOwner.
 */
@Service
public class ResumeOwnerServiceImpl implements ResumeOwnerService {

    private final ResumeOwnerRepository resumeOwnerRepository;

    @Autowired
    public ResumeOwnerServiceImpl(ResumeOwnerRepository resumeOwnerRepository) {
        this.resumeOwnerRepository = resumeOwnerRepository;
    }

    @Override
    public ResumeOwner findById(String ownerId) {
        return resumeOwnerRepository.findById(UUID.fromString(ownerId)).orElse(null);
    }
}
