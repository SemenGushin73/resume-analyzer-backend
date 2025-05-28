package webapp.resumeanalyzer.application.service.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import webapp.resumeanalyzer.domain.model.ResumeFile;
import webapp.resumeanalyzer.domain.repository.ResumeFileRepository;
import webapp.resumeanalyzer.domain.repository.ResumeOwnerRepository;
import webapp.resumeanalyzer.domain.service.ResumeFileService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Сервис CRUD методов сущности ResumeFile.
 */

@Service
@Transactional
@Slf4j
public class ResumeFileServiceImpl implements ResumeFileService {

    private final ResumeFileRepository resumeFileRepository;
    private final ResumeOwnerRepository resumeOwnerRepository;

    @Value("${app.upload.dir:C:/temp/resume-files}")
    private String uploadDir;

    @Autowired
    public ResumeFileServiceImpl(ResumeFileRepository resumeFileRepository, ResumeOwnerRepository resumeOwnerRepository) {
        this.resumeFileRepository = resumeFileRepository;
        this.resumeOwnerRepository = resumeOwnerRepository;
    }

    @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "PATH_TRAVERSAL_IN"})
    public void uploadResumeFile(ResumeFile resumeFile) {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("Не удалось создать директорию для загрузки файлов: {}", uploadDir, e);
            throw new RuntimeException("Не удалось создать директорию для загрузки файлов", e);
        }
        if (resumeFileRepository.findByOwnerId(resumeFile.getOwner().getId()).size() < 3) {
            resumeFileRepository.save(resumeFile);
            saveFileToDisk(resumeFile);
        } else {
            deleteFileFromDisk(resumeFileRepository
                    .findByOwnerId(resumeFile.getOwner().getId())
                    .getFirst());
            resumeFileRepository.delete(resumeFileRepository
                    .findByOwnerId(resumeFile.getOwner().getId())
                    .getFirst());
            resumeFileRepository.save(resumeFile);
            saveFileToDisk(resumeFile);
        }
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "CRLF_INJECTION_LOGS"})
    private void saveFileToDisk(ResumeFile resumeFile) {
        try {
            final Path filePath = Paths.get(uploadDir).resolve(sanitizeFilename(resumeFile.getFilename())).normalize();

            if (!filePath.startsWith(Paths.get(uploadDir))) {
                log.error("Попытка Path Traversal при сохранении файла: {}", sanitizeLogInput(resumeFile.getFilename()));
                throw new IOException("Попытка Path Traversal: " + resumeFile.getFilename());
            }

            try (FileOutputStream os = new FileOutputStream(filePath.toFile())) {
                os.write(resumeFile.getData());
            }
        } catch (IOException e) {
            log.error("Не удалось сохранить файл на диск: {}. Сообщение об ошибке: {}",
                    sanitizeLogInput(resumeFile.getFilename()), sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Не удалось сохранить файл на диск: " + resumeFile.getFilename(), e);
        }
    }

    @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "PATH_TRAVERSAL_IN"})
    private void deleteFileFromDisk(ResumeFile resumeFile) {
        try {
            final Path filePath = Paths.get(uploadDir).resolve(sanitizeFilename(resumeFile.getFilename())).normalize();
            if (Files.exists(filePath) && filePath.startsWith(Paths.get(uploadDir))) {
                Files.delete(filePath);
            } else {
                log.warn("Попытка удалить файл вне разрешенной директории или несуществующий файл: {}",
                        sanitizeLogInput(resumeFile.getFilename()));
            }
        } catch (IOException e) {
            log.error("Не удалось удалить файл с диска: {}. Сообщение об ошибке: {}",
                    sanitizeLogInput(resumeFile.getFilename()), sanitizeLogInput(e.getMessage()), e);
        }
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым.");
        }

        final Path path = Paths.get(filename);
        final Path fileNamePath = path.getFileName();

        String sanitized;
        if (fileNamePath != null) {
            sanitized = fileNamePath.toString();
        } else {
            sanitized = "";
        }

        sanitized = sanitized.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

        if (sanitized.equals(".") || sanitized.equals("..") || sanitized.startsWith(".")) {
            sanitized = "_" + sanitized;
        }

        return sanitized;
    }

    private String sanitizeLogInput(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace('\r', ' ').replace('\n', ' ');
    }

    public String convertCyrilic(ResumeFile resumeFile) {

        String filename = resumeFile.getFilename();
        if (resumeFile.getFileType().equals("application/pdf")) {
            filename = filename.replaceAll(".pdf", "");
        } else {
            filename = filename.replaceAll(".docx", "");
        }

        final char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'ѓ', 'е', 'ж', 'з', 'ѕ', 'и', 'ј', 'к', 'л', 'љ', 'м', 'н',
                'њ', 'о', 'п', 'р', 'с', 'т', 'ќ', 'у', 'ф', 'х', 'ц', 'ч', 'џ', 'ш',
                'А', 'Б', 'В', 'Г', 'Д', 'Ѓ', 'Е', 'Ж', 'З', 'Ѕ', 'И', 'Ј', 'К', 'Л', 'Љ', 'М', 'Н', 'Њ', 'О', 'П',
                'Р', 'С', 'Т', 'Ќ', 'У', 'Ф', 'Х', 'Ц', 'Ч',
                'Џ', 'Ш', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
                'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '/', '-'};
        final String[] abcLat = {" ", "a", "b", "v", "g", "d", "]", "e", "zh", "z", "y", "i", "j", "c", "l", "q", "m", "n",
                "w", "o", "p", "r", "s", "t", "'", "u", "f", "h", "c", ";", "x",
                "{", "A", "B", "V", "G", "D", "}", "E", "Zh", "Z", "Y", "I", "J", "K", "L", "Q", "M", "N", "W", "O",
                "P", "R", "S", "T", "KJ", "U", "F", "H", "C",
                ":", "X", "{", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
                "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/", "-"};
        if (filename.matches("[A-Za-z+]")) {
            return filename;
        } else {

            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < filename.length(); i++) {
                for (int x = 0; x < abcCyr.length; x++) {
                    if (filename.charAt(i) == abcCyr[x]) {
                        builder.append(abcLat[x]);
                    }
                }
            }
            return builder.toString();
        }
    }

    public ResumeFile getResumeFileByFilename(String filename) {
        return resumeFileRepository.findByFilenameContainingIgnoreCase(filename);
    }

    public List<ResumeFile> getResumeFileByOwnerId(String ownerId) {
        return resumeFileRepository.findByOwnerId(UUID.fromString(ownerId));
    }


    public ResumeFile getResumeFileById(String id) throws EmptyResultDataAccessException {
        return resumeFileRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EmptyResultDataAccessException(
                        "Resume file with id:" + id + " not found.", 1));
    }


    @Override
    public void deleteResumeFile(ResumeFile resumeFile) {
        resumeFileRepository.delete(resumeFile);
        deleteFileFromDisk(resumeFile);
    }

    @Override
    public List<ResumeFile> getAllResumeFiles() {
        return resumeFileRepository.findAll();
    }

    @Override
    public ResumeFile createResumeFile(MultipartFile file, String ownerId)
            throws IOException, EmptyResultDataAccessException {

        final ResumeFile resumeFile = new ResumeFile();
        resumeFile.setFilename(file.getOriginalFilename());
        resumeFile.setFileType(file.getContentType());
        resumeFile.setOwner(
                resumeOwnerRepository.findById(UUID.fromString(ownerId)).orElseThrow(() ->
                        new EmptyResultDataAccessException(
                                "Resume owner with id:" + ownerId + " not found.", 1)));
        resumeFile.setSize(file.getSize());
        resumeFile.setData(file.getBytes());
        resumeFile.setCreatedAt(LocalDateTime.now());

        return resumeFile;
    }

    @Override
    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "CRLF_INJECTION_LOGS"})
    public File getFile(String id) {
        final ResumeFile resumeFile = getResumeFileById(id);

        final String sanitizedFilename = sanitizeFilename(resumeFile.getFilename());

        final Path filePath = Paths.get(uploadDir).resolve(sanitizedFilename).normalize();

        if (!filePath.startsWith(Paths.get(uploadDir))) {
            log.error("Попытка Path Traversal при получении файла: {}", sanitizeLogInput(resumeFile.getFilename()));
            throw new IllegalArgumentException("Попытка Path Traversal при получении файла: " + resumeFile.getFilename());
        }

        final File file = filePath.toFile();

        try (FileOutputStream os = new FileOutputStream(file)) {
            os.write(resumeFile.getData());
        } catch (IOException e) {
            log.error("Ошибка при записи файла {} на диск: {}. Сообщение об ошибке: {}",
                    sanitizedFilename, sanitizeLogInput(e.getMessage()), e);
            throw new RuntimeException("Не удалось записать файл на диск", e);
        }

        return file;
    }

    @Override
    public HttpHeaders getHeaders(ResumeFile resumeFile) {
        final HttpHeaders headers = new org.springframework.http.HttpHeaders();

        if (resumeFile.getFileType().equals("application/pdf")) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + convertCyrilic(resumeFile) + ".pdf");
        } else {
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + convertCyrilic(resumeFile) + ".docx");

        }
        return headers;
    }
}
