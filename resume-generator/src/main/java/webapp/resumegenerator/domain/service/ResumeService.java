package webapp.resumegenerator.domain.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import ru.kata.project.resumeDto.ResumeDto;
import webapp.resumegenerator.exception.PdfGenerationException;

/*
 * сервис для получения ResumeDto с resume-analyzer
 * TODO: сделать так, чтобы все запросы посылались через kafka
 */
@Service
@Slf4j
public class ResumeService {

    private RestTemplate restTemplate;
    private String URL_RESUME_ANALYZER = "http://resume-analyzer:8083/resumes/";

    @Autowired
    public ResumeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResumeDto getResume(UUID id) {
        String url = URL_RESUME_ANALYZER + id;
        ResponseEntity<ResumeDto> response = restTemplate.getForEntity(url, ResumeDto.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Resume not found, id: {}", id);
            throw new PdfGenerationException("Resume not found");
        }
        return response.getBody();
    }
}
