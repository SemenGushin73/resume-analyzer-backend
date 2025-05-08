package ru.javabegin.micro.planner.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.micro.planner.notificationservice.model.ResumeRequest;

import java.util.UUID;

@RestController
@RequestMapping("/analyze")
@Tag(name = "Контроллер клиента.", description = "Осуществляет HTTP-запрос через Swagger")
public class ResumeAnalysisController {
    private final KafkaTemplate<String, ResumeRequest> kafkaTemplate;

    public ResumeAnalysisController(KafkaTemplate<String, ResumeRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Operation(
            summary = "Отправка http запроса через Swagger",
            description = """
                    ### Как подключиться к WebSocket
                    
                    1. Подключитесь по адресу: `ws://localhost:8885/ws/notifications`
                    2. После подключения отправьте сообщение с `requestId`, который вы получили при отправке HTTP-запроса.
                    3. Ожидайте ответ от сервиса — он придёт по этому же соединению.
                    
                    ### Пример (JavaScript):
                    
                    ```javascript
                    const socket = new WebSocket("ws://localhost:8885/ws/notifications");
                    
                    socket.onopen = () => {
                        socket.send("123e4567-e89b-12d3-a456-426614174000");
                    };
                    
                    socket.onmessage = (event) => {
                        console.log("Ответ от сервера:", event.data);
                    };
                    ```
                    
                    ⚠️ WebSocket-сессии автоматически очищаются при бездействии.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Инструкция отображена")
    @PostMapping
    public ResponseEntity<Void> analyzeResume(@RequestBody ResumeRequest request) {
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        kafkaTemplate.send("resume-analysis-topic", requestId, request);

        // Немедленно возвращаем 202 Accepted и requestId
        return ResponseEntity
                .accepted()
                .header("X-Request-ID", requestId)
                .build();
    }
}
