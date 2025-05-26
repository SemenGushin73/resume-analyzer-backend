import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.kata.project.resumeDto.ResumeDto;
import webapp.resumegenerator.domain.service.ResumeService;
import webapp.resumegenerator.exception.PdfGenerationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ResumeService resumeService;

    @Test
    void getResume_shouldReturnResumeDto_whenRequestIsSuccessful() {
        UUID testId = UUID.randomUUID();
        ResumeDto expectedResume = new ResumeDto(resume); // или заполненный объект
        ResponseEntity<ResumeDto> responseEntity = new ResponseEntity<>(expectedResume, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(ResumeDto.class)))
                .thenReturn(responseEntity);

        // Act
        ResumeDto result = resumeService.getResume(testId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResume, result);
    }

    @Test
    void getResume_shouldThrowPdfGenerationException_whenResumeNotFound() {
        // Arrange
        UUID testId = UUID.randomUUID();
        ResponseEntity<ResumeDto> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restTemplate.getForEntity(anyString(), eq(ResumeDto.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        assertThrows(PdfGenerationException.class, () -> resumeService.getResume(testId));
    }

    @Test
    void getResume_shouldUseCorrectUrl() {
        // Arrange
        UUID testId = UUID.randomUUID();
        String expectedUrl = "http://resume-analyzer:8083/resumes/" + testId;
        ResumeDto expectedResume = new ResumeDto(resume);
        ResponseEntity<ResumeDto> responseEntity = new ResponseEntity<>(expectedResume, HttpStatus.OK);

        when(restTemplate.getForEntity(eq(expectedUrl), eq(ResumeDto.class)))
                .thenReturn(responseEntity);

        // Act
        ResumeDto result = resumeService.getResume(testId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResume, result);
    }
}