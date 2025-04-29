//package infrastructure.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.junit.jupiter.api.*;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mockito;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import webapp.resumegenerator.domain.model.Template;
//import webapp.resumegenerator.domain.service.TemplateService;
//import webapp.resumegenerator.infrastructure.controller.TemplateController;
//
///**
// * Тестовый класс для проверки валидации поля content в классе {@link Template}
// */
//@WebMvcTest(controllers = TemplateController.class)
//@ContextConfiguration(classes = {TemplateController.class})
//public class TemplateControllerValidationTest {
//    private static final Logger log = LoggerFactory.getLogger(TemplateControllerValidationTest.class);
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private TemplateService templateService;
//
//    @BeforeEach
//    public void setUp(TestInfo testInfo) {
//
////        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//
////        mockMvc = MockMvcBuilders.standaloneSetup(templateController).build();
//
//        log.info("Запуск теста: {}", testInfo.getDisplayName());
//    }
//
//    @AfterEach
//    public void tearDown() {
//        log.info("Тест завершен!");
//    }
//
//    @Test
//    @DisplayName("Проверка успешной валидации корректного шаблона. Ожидается статус 201 (Created).")
//    public void testValidTemplate() throws Exception {
//        final Template validTemplate = new Template("Valid Template", "Description",
//                "{\"blocks\": [{\"name\": \"block1\", \"layout\": \"layout1\", \"props\": {}}]}");
//
//        Mockito.when(templateService.createTemplate(ArgumentMatchers.any(Template.class))).thenReturn(validTemplate);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/templates")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validTemplate)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//    }
//
//    @Test
//    @DisplayName("Проверка шаблона с контентом, превышающим максимальный размер. Ожидает статус 400 (Bad Request).")
//    public void testTemplateExeedsSize() throws Exception {
//        final String oversizedContent = new String(new char[100001]).replace("\0", "a");
//        final Template invalidTemplate = new Template("Oversized Template", "Description", oversizedContent);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/templates")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidTemplate)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("Проверка шаблона, у которого отсутствует обязательное поле name. Ожидает статус 400 (Bad Request)")
//    public void testTemplateMissingRequiredFieldName() throws Exception {
//        final Template invalidTemplate = new Template(null, "Description",
//                "{\"blocks\": [{\"layout\": \"layout1\", \"props\": {}}]}");
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/templates")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidTemplate)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("Проверка шаблона, у которого отсутствует обязательное поле content. Ожидает статус 400 (Bad Request)")
//    public void testTemplateMissingRequiredFieldContent() throws Exception {
//        final Template invalidTemplate = new Template("Invalid Template", "Description",
//                null);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/templates")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidTemplate)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("Проверка шаблона с некорректной структурой JSON. Ожидает статус 400 (Bad Request)")
//    public void testTemplateInvalidStructure() throws Exception {
//        final Template invalidTemplate = new Template("Invalid Template", "Description",
//                "{\"invalidField\": []}");
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/templates")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidTemplate)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//}
