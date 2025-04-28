package webapp.resumegenerator.domain.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Класс реализующий обработку @ValidContentFieldStructure
 */
@Component
public class ContentFieldStructureValidator implements ConstraintValidator<ValidContentFieldStructure, String> {

    private static final int MAX_BLOCKS = 50;
    private static final int MAX_DEPTH = 5;
    private static final String[] FIELDS = new String[]{"name", "layout", "props"};
    private final ObjectMapper objectMapper;

    public ContentFieldStructureValidator() {
        this.objectMapper = new ObjectMapper();
    }

    @Autowired
    public ContentFieldStructureValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            final JsonNode root = objectMapper.readTree(value);
            final JsonNode blocksNode = root.get("blocks");

            if (blocksNode == null || !blocksNode.isArray()) {
                return false;
            }

            if (blocksNode.size() > MAX_BLOCKS) {
                return false;
            }

            for (JsonNode blockNode : blocksNode) {
                if (isInvalidBlock(blockNode, 1)) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Метод валидирующий глубину вложения поля blocks
     *
     * @param blockNode поле для проверки
     * @param depth     текущая глубина вложенности
     * @return false в случае не соответствия структуры заданным параметрам
     */
    private boolean isInvalidBlock(JsonNode blockNode, int depth) {

        if (depth > MAX_DEPTH) {
            return true;
        }

        for (String field : FIELDS) {
            if (!blockNode.hasNonNull(field)) {
                return true;
            }
        }

        final JsonNode childrenNode = blockNode.get("children");

        if (childrenNode == null) {
            return false;
        }

        if (childrenNode.isArray()) {
            for (JsonNode childNode : childrenNode) {
                if (isInvalidBlock(childNode, depth + 1)) {
                    return true;
                }
            }
        }

        return false;
    }
}
