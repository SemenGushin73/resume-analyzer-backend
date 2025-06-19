package webapp.subscriptionservice.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        try {
            return jsonNode != null ? jsonNode.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Не получилось конвертировать JsonNode в String", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String jsonString) {
        try {
            return jsonString != null ? objectMapper.readTree(jsonString) : null;
        } catch (Exception e) {
            throw new RuntimeException("Не получилось конвертировать String в JsonNode", e);
        }
    }
}