package webapp.subscriptionservice.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер предназначен для обработки редиректов пользователя от платежного шлюза
 * после завершения или отмены оплаты. Он предоставляет текстовые сообщения для пользователя.
 **/
@RestController
@RequestMapping("/subscriptions")
@Slf4j
@Tag(name = "Подтверждение Платежей (Публичное API)", description = "Эндпоинты для редиректа пользователя от платежного шлюза (без фронтенда).")
public class ConfirmationController {

    @GetMapping("/success")
    @Operation(
            summary = "Перенаправление после успешной оплаты",
            description = "Обрабатывает перенаправление пользователя от платежного шлюза после успешной оплаты. " +
                    "Этот эндпоинт предназначен для пользовательского опыта; " +
                    "обновление статуса подписки обрабатывается серверными колбэками от платежного шлюза. " +
                    "Возвращает простое текстовое сообщение об успехе."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает простое сообщение об успехе")
    })
    public String paymentSuccess(
            @Parameter(in = ParameterIn.QUERY, description = "UUID пользователя, для которого создана подписка", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam UUID userId,
            @Parameter(in = ParameterIn.QUERY, description = "Тип подписки (BASIC, PRO)", required = true, example = "BASIC")
            @RequestParam String type) {

        log.info("Пользователь перенаправлен на /subscriptions/success для userId: {}, тип: {}. Статус платежа обновлен колбэком.", userId, type);
        return "Оплата успешно завершена! Ваша подписка будет активирована в ближайшее время. Спасибо за ваш выбор.";
    }

    @GetMapping("/failure")
    @Operation(
            summary = "Перенаправление после неудачной или отмененной оплаты",
            description = "Обрабатывает перенаправление пользователя от платежного шлюза после неудачной или отмененной оплаты. " +
                    "Этот эндпоинт предназначен исключительно для пользовательского опыта; " +
                    "обновление статуса подписки обрабатывается серверными колбэками от платежного шлюза. " +
                    "Возвращает простое текстовое сообщение о неудаче."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает простое сообщение о неудаче")
    })
    public String paymentFailure(
            @Parameter(in = ParameterIn.QUERY, description = "UUID пользователя, для которого была предпринята попытка оплаты", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @RequestParam UUID userId) {

        log.info("Пользователь перенаправлен на /subscriptions/failure для userId: {}. Ошибка или отмена платежа.", userId);
        return "Оплата не удалась или была отменена. Пожалуйста, попробуйте еще раз или свяжитесь со службой поддержки.";
    }
}
