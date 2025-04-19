package infrastructure.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import webapp.resumegenerator.domain.service.ResumeBuilder;
import webapp.resumegenerator.infrastructure.controller.ResumeBuilderController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeBuilderController.class)
class ResumeBuilderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ResumeBuilder resumeBuilder;

//    @BeforeEach
//    void setUp() throws Exception {
//        resumeBuilder = Mockito.mock(ResumeBuilder.class);
//        ResumeBuilderController controller = new ResumeBuilderController(resumeBuilder);
//        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
//        objectMapper = new ObjectMapper();
//    }

    @Test
    void shouldBuildResumeAndReturn201() throws Exception {
        String resumeRequest = """
                { "personal_data" : {
                "fullName":"Иван Петров",
                "address":"Moscow city",
                "bio":"learn to program",
                "position":"student",
                "phone":89125088541,
                "website":"https://www.google.com",
                "email":"ivan@example.com"
                },
                 "educations": [
                                {
                                  "institution": "МГУ",
                                  "degree": "Бакалавр",
                                  "year": 2022
                                }
                              ],
                              "experiences": [
                                {
                                  "company": "Яндекс",
                                  "position": "Java Developer",
                                  "years": "2021-2023"
                                }
                              ],
                              "socialLinks": [
                                {
                                  "platform": "LinkedIn",
                                  "url": "https://linkedin.com/in/ivan"
                                }
                              ],
                              "hobbies": [
                                {
                                  "name": "Чтение"
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
                        .content(resumeRequest))  // Отправляем inputJson напрямую
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resume").value("Generated resume"));

    }
}
