// package infrastructure.controller;

// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.core.io.ByteArrayResource;
// import org.springframework.core.io.Resource;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
// import ru.kata.project.resumeDto.ResumeDto;
// import ru.kata.project.resumeDto.TemplateDTO;
// import webapp.resumegenerator.domain.service.PdfGenerationService;
// import webapp.resumegenerator.domain.service.ResumeService;
// import webapp.resumegenerator.infrastructure.controller.ResumeFileDownloadController;

// import java.util.UUID;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(controllers = ResumeFileDownloadController.class)
// @ContextConfiguration(classes = {ResumeFileDownloadController.class})
// class ResumeFileDownloadControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Mock
//     private PdfGenerationService pdfService;

//     @Mock
//     private ResumeService resumeService;

//     @InjectMocks
//     private ResumeFileDownloadController controller;

//     @Test
//     void downloadResumePdf_ShouldReturnPdfFile() throws Exception {
//         // Arrange
//         UUID resumeId = UUID.randomUUID();
//         ResumeDto resume = new ResumeDto();
//         TemplateDTO template = new TemplateDTO();
//         byte[] pdfBytes = "PDF content".getBytes();

//         when(resumeService.getResume(resumeId)).thenReturn(resume);
//         when(pdfService.generateResumePdf(resume, template)).thenReturn(pdfBytes);

//         // Act & Assert
//         mockMvc.perform(MockMvcRequestBuilders.post("/resumes/{id}/download", resumeId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"templateName\":\"default\"}")) // Пример тела запроса
//                 .andExpect(status().isOk())
//                 .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, 
//                     "attachment; filename=resume_" + resumeId + ".pdf"))
//                 .andExpect(content().contentType(MediaType.APPLICATION_PDF))
//                 .andExpect(content().bytes(pdfBytes));
//     }

//     @Test
//     void downloadResumePdf_ShouldReturn404WhenResumeNotFound() throws Exception {
//         // Arrange
//         UUID resumeId = UUID.randomUUID();
//         when(resumeService.getResume(resumeId)).thenThrow(new RuntimeException("Resume not found"));

//         // Act & Assert
//         mockMvc.perform(MockMvcRequestBuilders.post("/resumes/{id}/download", resumeId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{\"templateName\":\"default\"}"))
//                 .andExpect(status().isNotFound());
//     }
// }