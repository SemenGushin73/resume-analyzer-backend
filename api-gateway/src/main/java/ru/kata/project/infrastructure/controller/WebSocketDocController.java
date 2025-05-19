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
 */
@RestController
@RequestMapping("/ws")
@Tag(name = "Контроллер WebSocket.", description = "Осуществление взаимодействия по протоколу webSocket.")
public class WebSocketDocController {

    @Operation(
            summary = "Инструкция по использованию WebSocket для получения уведомлений",
            description = """
                    *** Как подключиться к WebSocket:
                    1. Перейдите в браузере на страницу по адресу: `http://localhost:8765/ws_client.html
                    2. После подключения * Введите requestId *, который вы получили при отправке HTTP-запроса
                    в Swigger / Api-Gateway. (x-request-id в Response Headers, смотри в /ПРОСМОТРЕТЬ КОД/ (NETWORK)).
                    3. Ожидайте ответ от сервиса — он придёт по этому же соединению.
                    4. WebSocket-сессии автоматически очищаются при бездействии.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Инструкция отображена")
    @GetMapping("/instructions")
    public ResponseEntity<String> websocketInstructions() {
        return ResponseEntity.ok("Смотри описание выше в Swagger UI.");
    }
}
