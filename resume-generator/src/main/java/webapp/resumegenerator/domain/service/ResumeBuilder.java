package webapp.resumegenerator.domain.service;

import webapp.resumeanalyzer.domain.model.Resume;

public interface ResumeBuilder {

    Resume processResume(Resume resume);
}