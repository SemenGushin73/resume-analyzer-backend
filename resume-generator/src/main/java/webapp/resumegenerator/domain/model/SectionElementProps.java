package webapp.resumegenerator.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Ildar Khuzin
 * @version 1.0.0
 * Настройки для отображения каждого блока (стили, текстовые данные).
 */
@Document(collection = "section_element_props")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionElementProps {
    @Id
    private UUID id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("text")
    private String text;

    @JsonProperty("wrapperStyle")
    private Map<String, String> wrapperStyle;

    @JsonProperty("textStyle")
    private Map<String, String> textStyle;

    @JsonProperty("inputStyle")
    private Map<String, String> inputStyle;

    @JsonProperty("url")
    private String url;

    @JsonProperty("style")
    private Map<String, String> style;

    public SectionElementProps() {
        this.id = UUID.randomUUID();
    }

    public SectionElementProps(String key, String text, Map<String, String> wrapperStyle,
                               Map<String, String> textStyle, Map<String, String> inputStyle,
                               String url, Map<String, String> style) {
        this.id = UUID.randomUUID();
        this.key = key;
        this.text = text;
        this.wrapperStyle = wrapperStyle == null ? null : new HashMap<>(wrapperStyle);
        this.textStyle = textStyle == null ? null : new HashMap<>(textStyle);
        this.inputStyle = inputStyle == null ? null : new HashMap<>(inputStyle);
        this.url = url;
        this.style = style == null ? null : new HashMap<>(style);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getWrapperStyle() {
        return wrapperStyle == null? null : new HashMap<>(wrapperStyle);
    }

    public void setWrapperStyle(Map<String, String> wrapperStyle) {
        this.wrapperStyle = wrapperStyle == null ? null : new HashMap<>(wrapperStyle);
    }

    public Map<String, String> getTextStyle() {
        return textStyle== null? null : new HashMap<>(textStyle);
    }

    public void setTextStyle(Map<String, String> textStyle) {
        this.textStyle = textStyle == null ? null : new HashMap<>(textStyle);
    }

    public Map<String, String> getInputStyle() {
        return inputStyle == null? null : new HashMap<>(inputStyle);
    }

    public void setInputStyle(Map<String, String> inputStyle) {
        this.inputStyle = inputStyle == null ? null : new HashMap<>(inputStyle);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getStyle() {
        return style == null? null : new HashMap<>(style);
    }

    public void setStyle(Map<String, String> style) {
        this.style = style == null ? null : new HashMap<>(style);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SectionElementProps that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public SectionElementProps copy() {
        SectionElementProps copy = new SectionElementProps();
        copy.setKey(this.key);
        copy.setText(this.text);
        copy.setUrl(this.url);

        copy.setWrapperStyle(this.wrapperStyle == null ? null : Map.copyOf(this.wrapperStyle));
        copy.setTextStyle(this.textStyle == null ? null : Map.copyOf(this.textStyle));
        copy.setInputStyle(this.inputStyle == null ? null : Map.copyOf(this.inputStyle));
        copy.setStyle(this.style == null ? null : Map.copyOf(this.style));

        return copy;
    }
}