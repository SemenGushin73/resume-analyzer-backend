package webapp.resumeanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = ResumeAnalyzerApplication.class)
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testResourceNotFoundException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource-not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Resource not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
    }

    @Test
    void testInvalidRequestException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/invalid-request")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid request data"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
    }

    @Test
    void testKafkaProcessingException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/kafka-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Kafka error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500));
    }

    @Test
    void testExternalServiceException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/external-service-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("External service error!!!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500));
    }

    @Test
    void testGlobalException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/global-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Internal Server Error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(500));
    }
}