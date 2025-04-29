package webapp.resumegenerator.domain.PDFGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kata.project.resumeDto.ResumeDto;
import ru.kata.project.resumeDto.TemplateDTO;
import webapp.resumegenerator.domain.service.PdfGenerationService;

/*
 * Сервис для генерации PDF файлов резюме
 * 
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGenerationServiceWithHtmlImpl extends PdfGenerationService{

    private final HTMLResumeGenerator htmlGenerator;
    private final String fontPath = "fonts/ARIAL.TTF";

    @Override
    public byte[] createPdfFile(ResumeDto resume, TemplateDTO template) throws Exception {
        String html = htmlGenerator.createHTML( resume, template);
        byte[] pdfBytes = createPdfFilebyHtml( html );
        return pdfBytes;
    }

    private byte[] createPdfFilebyHtml( String filledHtml ) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        addFontToRenderer( renderer );
        renderer.setDocumentFromString(filledHtml);
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();

        return outputStream.toByteArray();
    }

    private void addFontToRenderer( ITextRenderer renderer ) throws DocumentException, IOException {
        renderer.getFontResolver().addFont(
            new ClassPathResource(fontPath).getURL().toString(),
            BaseFont.IDENTITY_H,
            BaseFont.EMBEDDED
        );
    }
}