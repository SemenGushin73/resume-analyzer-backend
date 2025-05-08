package ru.javabegin.micro.planner.notificationservice.doccontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws")
public class WebSocketDocController {

    @Operation(
            summary = "Инструкция по использованию WebSocket для получения уведомлений",
            description = """
                ### Как подключиться к WebSocket

                1. Подключитесь по адресу: `ws://localhost:8080/ws/notifications`
                2. После подключения отправьте сообщение с `requestId`, который вы получили при отправке HTTP-запроса.
                3. Ожидайте ответ от сервиса — он придёт по этому же соединению.

                ### Пример (JavaScript):

                ```javascript
                const socket = new WebSocket("ws://localhost:8080/ws/notifications");

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
    @GetMapping("/instructions")
    public ResponseEntity<String> websocketInstructions() {
        return ResponseEntity.ok("Смотри описание выше в Swagger UI.");
    }
}