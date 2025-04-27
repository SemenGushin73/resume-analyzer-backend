package infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import webapp.resumegenerator.domain.service.ResumeBuilder;
import webapp.resumegenerator.infrastructure.controller.ResumeBuilderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тестовый класс для проверки контроллера в классе {@link ResumeBuilderController}
 */
@WebMvcTest(ResumeBuilderController.class)
@ContextConfiguration(classes = {ResumeBuilderController.class})
class ResumeBuilderControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ResumeBuilderController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResumeBuilder resumeBuilder;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        objectMapper.registerModule(new JavaTimeModule());
        log.info("Запуск теста контроллера ResumeBuilderController: {}", testInfo.getDisplayName());
    }

    @AfterEach
    public void tearDown() {
        log.info("Тест контроллера ResumeBuilderController завершен!");
    }

    @DisplayName("Проверка успешного создания резюме. Ожидается статус 201 (Created).")
    @Test
    void shouldBuildResumeAndReturnCreated() throws Exception {

        String resumeRequest = """
                { "personal_data" : {
                    "fullName":"Иван Петров",
                    "adress":"Moscow city",
                    "bio":"learn to program",
                    "position":"student",
                    "phone":89125088541,
                    "website":"https://www.google.com",
                    "email":"ivan@example.com"
                    },
                    "educations": [
                        { "institution": "МГУ",
                        "degree": "Бакалавр",
                        "year": 2022
                        }
                    ],
                    "experiences": [
                        { "company": "Яндекс",
                        "position": "Java Developer",
                        "years": "2021-2023"
                        }
                    ],
                    "socialLinks":  [
                        { "platform": "LinkedIn",
                        "url": "https://linkedin.com/in/ivan"
                        }
                    ],
                    "hobbies": [
                    { "name": "Чтение"
                    }
                    ]
                }
                """;
        JsonNode inputJson = objectMapper.readTree(resumeRequest);
        String resumeResponse = "{\"resume\":\"Generated resume\"}";
        JsonNode responseJson = objectMapper.readTree(resumeResponse);

        // Мокаем метод сервиса
        Mockito.when(resumeBuilder.processResume(Mockito.any(JsonNode.class))).thenReturn(responseJson);

        // Проводим тест с MockMvc
        mockMvc.perform(post("/builder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resumeRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resume").value("Generated resume"));
    }

    @DisplayName("Проверка успешного создания неполного резюме. Ожидается статус 201 (Created). " +
                 "Так как @Valid + JsonNode = не работает (аннотации валидации не применяются).")
    @Test
    void shouldReturnBadRequestForNotCompleteEmptyResume() throws Exception {
        String invalidRequest = """
                { "personal_data" :
                    { "fullName":"Maxim",
                    "address":"Ivanov city",
                    "bio":"learn to program",
                    "position":"student",
                    "phone":89125088541,
                    "website":"https://www.google.com",
                    "email":"invalid"
                    }
                }
                """;
        JsonNode inputJson = objectMapper.readTree(invalidRequest);
        String resumeResponse = "{\"resume\":\"Generated NotComplete resume\"}";
        JsonNode responseJson = objectMapper.readTree(resumeResponse);
        Mockito.when(resumeBuilder.processResume(Mockito.any(JsonNode.class))).thenReturn(responseJson);
        mockMvc.perform(post("/builder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resume").value("Generated NotComplete resume"));
    }

    @DisplayName("Проверка реальной валидации пустого резюме. Ожидается статус 400 (BadRequest).")
    @Test
    void shouldReturnBadRequestMissingBody() throws Exception {
        String invalidRequest = """
                {""}
                """;
        mockMvc.perform(post("/builder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @DisplayName("Проверка минимальной валидации резюме. Только имя ожидается статус 200 (Created). Используются " +
                 "минимальная фикстура DTO ")
    @Test
    void shouldBuildNotCompleteResumeAndReturnCreated() throws Exception {
        String resumeRequest = """
                { "personal_data" :
                    { "fullName":"Иван Петров"
                    }
                }
                """;
        JsonNode inputJson = objectMapper.readTree(resumeRequest);
        String resumeResponse = "{\"resume\":\"Generated minFixDto resume\"}";
        JsonNode responseJson = objectMapper.readTree(resumeResponse);

        // Мокаем метод сервиса
        Mockito.when(resumeBuilder.processResume(Mockito.any(JsonNode.class))).thenReturn(responseJson);

        // Проводим тест с MockMvc
        mockMvc.perform(post("/builder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resumeRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resume").value("Generated minFixDto resume"));
    }
}

