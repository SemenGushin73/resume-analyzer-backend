package ru.kata.project.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Класс предоставляет инструкцию для клиентов (разработчиков), как работать с
 * WebSocket-эндпоинтом, так как Swagger по умолчанию не документирует WS-соединения.
 * Написан пример кода, в описании (@Operation) готовый JavaScript-код для быстрого старта,
 * или использовать плагин в браузере(Advanced Websocket Client)
 */
@RestController
@RequestMapping("/ws")
@Tag(name = "Контроллер WebSocket.", description = "Осуществление взаимодействия по протоколу webSocket.")
public class WebSocketDocController {

    @Operation(
            summary = "Инструкция по использованию WebSocket для получения уведомлений",
            description = """
                *** Как подключиться к WebSocket:
                1.Можно установить расширение браузера (Advanced Websocket Client) или JavaScript.
                Описание кода ниже.
                2. Подключитесь по адресу: `ws://localhost:8885/ws/notifications`
                3. После подключения отправьте сообщение с `requestId`, который вы получили при отправке HTTP-запроса.
                4. Ожидайте ответ от сервиса — он придёт по этому же соединению.
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
                WebSocket-сессии автоматически очищаются при бездействии.
                """
    )
    @ApiResponse(responseCode = "200", description = "Инструкция отображена")
    @GetMapping("/instructions")
    public ResponseEntity<String> websocketInstructions() {
        return ResponseEntity.ok("Смотри описание выше в Swagger UI.");
    }
}