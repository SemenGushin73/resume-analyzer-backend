package webapp.resumegenerator.application.service;


import org.springframework.stereotype.Service;

import webapp.resumegenerator.domain.service.ResumeBuilder;
import webapp.resumeanalyzer.domain.model.Resume;

@Service
public class ResumeBuilderServiceImpl implements ResumeBuilder {

    @Override
    public Resume processResume(Resume resume) {

        return resume;
    }
}