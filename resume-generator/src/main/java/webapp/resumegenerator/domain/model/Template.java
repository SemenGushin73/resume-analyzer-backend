package webapp.resumegenerator.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.kata.project.model.Department;
import webapp.resumegenerator.domain.validation.ValidContentFieldStructure;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс, представляющий шаблон резюме.
 * Данный класс хранит информацию о шаблоне, такую как название шаблона, описания,
 * контент и дату создания.
 */
@Document(collection = "templates")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
//TODO проверить нужна ли @Builder и на что она влияет
// в документации написано, что эта аннотация работает только если не создан не один конструктор
// и не используются другие аннотации @XArgsConstructor
@Builder
public class Template {
    /**
     * Уникальный идентификатор, генерируется при создании нового шаблона.
     */
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private UUID id;

    /**
     * Название шаблона.
     * Не должно быть пустым.
     */
    @NotNull(message = "Имя шаблона не может быть пустым.")
    @Size(min = 3, max = 100, message = "Имя шаблона должно содержать от 3 до 100 символов.")
    private String name;

    /**
     * Описание шаблона.
     * Не должно быть пустым.
     */
    @NotNull(message = "Описание шаблона не может быть пустым.")
    @Size(max = 255, message = "Описание должно содержать не более 255 символов.")
    private String description;

    /**
     * Контент шаблона.
     * Не должен быть пустым.
     */
    @NotNull(message = "Контент шаблона не может быть пустым")
    @Size(max = 100000, message = "Контент шаблона не должен превышать 100 000 символов.")
    @ValidContentFieldStructure(message = "Некорректная структура JSON в поле content.")
    private String content;

    /**
     * Дата и время создания шаблона.
     * Устанавливается автоматически в момент создания шаблона.
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Версия шаблона.
     * Устанавливается автоматически в момент создания шаблона.
     */
    @NotNull()
    private Integer version;

    private Department department;

    /**
     * Конструктор с параметрами для создания шаблона.
     *
     * @param name Название шаблона.
     * @param description Описание шаблона.
     * @param content Контент шаблона.
     */
    public Template(String name, String description, String content) {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.description = description;
        this.content = content;
        this.version = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Template template = (Template) o;
        return Objects.equals(id, template.id) &&
          Objects.equals(name, template.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id, name);
    }
}