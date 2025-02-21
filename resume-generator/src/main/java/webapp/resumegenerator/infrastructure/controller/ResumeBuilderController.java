package webapp.resumegenerator.infrastructure.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import webapp.resumeanalyzer.domain.model.Resume;
import webapp.resumegenerator.domain.service.ResumeBuilder;
@RestController
@RequestMapping("/builder")
public class ResumeBuilderController {

    private final ResumeBuilder resumeBuilder;

    @Autowired
    public ResumeBuilderController(ResumeBuilder resumeBuilder) {
        this.resumeBuilder = resumeBuilder;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Resume> buildResume(@Valid @RequestBody Resume resume) {
        Resume generatedResume = resumeBuilder.processResume(resume);

        return ResponseEntity.status(HttpStatus.CREATED).body(generatedResume);
    }
}