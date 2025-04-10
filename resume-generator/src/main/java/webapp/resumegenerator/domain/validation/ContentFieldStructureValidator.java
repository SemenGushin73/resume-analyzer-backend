package webapp.resumegenerator.domain.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ContentFieldStructureValidator implements ConstraintValidator<ValidContentFieldStructure, String> {

    private static final int MAX_BLOCKS = 50;
    private static final int MAX_DEPTH = 5;
    private static final String[] FIELDS = new String[] {"name", "layout", "props"};
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(value);
            JsonNode blocksNode = root.get("blocks");

            //TODO нужно проверить условие !blocksNode.isArray() в случае если
            // content не имеет ни одного вложенного уровня
            if (blocksNode == null || !blocksNode.isArray()) {
                return false;
            }

            if (blocksNode.size() > MAX_BLOCKS) {
                return false;
            }

            for (JsonNode blockNode : blocksNode) {
                if (!isValidBlock(blockNode, 1)) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isValidBlock(JsonNode blockNode, int depth) {

        if (depth > MAX_DEPTH) {
            return false;
        }

        for (String field : FIELDS) {
            if (!blockNode.hasNonNull(field)) {
                return false;
            }
        }

        JsonNode childrenNode = blockNode.get("children");
        if (childrenNode != null && !childrenNode.isArray()) {
            for (JsonNode childNode : childrenNode) {
                if (!isValidBlock(childNode, depth + 1)) {
                    return false;
                }
            }
        }

        return true;
    }
}
