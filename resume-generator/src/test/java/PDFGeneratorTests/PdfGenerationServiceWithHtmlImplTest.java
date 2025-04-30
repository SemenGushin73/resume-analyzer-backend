// package PDFGeneratorTests;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.core.io.ClassPathResource;
// import org.xhtmlrenderer.pdf.ITextRenderer;
// import ru.kata.project.resumeDto.ResumeDto;
// import ru.kata.project.resumeDto.TemplateDTO;
// import webapp.resumegenerator.domain.PDFGenerator.HTMLResumeGenerator;
// import webapp.resumegenerator.domain.PDFGenerator.PdfGenerationServiceWithHtmlImpl;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class PdfGenerationServiceWithHtmlImplTest {

//     @Mock
//     private HTMLResumeGenerator htmlGenerator;

//     @Mock
//     private ITextRenderer renderer;

//     @Mock
//     private ClassPathResource fontResource;

//     @InjectMocks
//     private PdfGenerationServiceWithHtmlImpl pdfGenerationService;

//     @Test
//     void returnPdfBytesTest() throws Exception {
//         ResumeDto resume = new ResumeDto();
//         TemplateDTO template = new TemplateDTO();
//         String mockHtml = "<html>Mock HTML</html>";
//         byte[] expectedPdfBytes = new byte[]{1, 2, 3};

//         when(htmlGenerator.createHTML(resume, template)).thenReturn(mockHtml);
        
//         PdfGenerationServiceWithHtmlImpl spyService = spy(pdfGenerationService);
//         doReturn(expectedPdfBytes).when(spyService).createPdfFilebyHtml(mockHtml);

//         byte[] result = spyService.createPdfFile(resume, template);

//         assertNotNull(result);
//         assertArrayEquals(expectedPdfBytes, result);
//         verify(htmlGenerator).createHTML(resume, template);
//     }
// }