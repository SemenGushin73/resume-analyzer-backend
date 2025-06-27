package webapp.subscriptionservice.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import webapp.subscriptionservice.application.util.LogSanitizer;

import java.util.UUID;

/**
 * Контроллер предназначен для обработки редиректов пользователя от платежного шлюза, пока отсутствует фронт. Потом можно удалить
 * после завершения или отмены оплаты. Он предоставляет текстовые сообщения для пользователя.
 **/
@RestController
@RequestMapping("/api/v1/payment/callback/subscriptions")
@Slf4j
public class ConfirmationController {

    /**
     * Обрабатывает перенаправление пользователя на страницу после успешной оплаты.
     * Этот метод предназначен только для информирования пользователя, так как фронт отсутствует.
     *
     * @param userId Уникальный идентификатор пользователя, для которого была создана подписка.
     * @param type   Тип подписки (например, "BASIC", "PRO"), для которой была произведена оплата.
     * @return Простое текстовое сообщение об успешном завершении оплаты.
     */
    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam UUID userId,
            @RequestParam String type) {

        log.info("Пользователь перенаправлен на /subscriptions/success для userId: {}, тип: {}. Статус платежа обновлен колбэком.",
                LogSanitizer.sanitizeLogInput(userId.toString()), LogSanitizer.sanitizeLogInput(type));
        return "Оплата успешно завершена! Ваша подписка будет активирована в ближайшее время. Спасибо за ваш выбор.";
    }

    /**
     * Обрабатывает перенаправление пользователя на страницу после неудачной или отмененной оплаты.
     * Этот метод предназначен только для информирования пользователя, так как фронт отсутствует.
     *
     * @param userId Уникальный идентификатор пользователя, для которого была предпринята попытка оплаты.
     * @return Простое текстовое сообщение о неудаче или отмене оплаты.
     */
    @GetMapping("/failure")
    public String paymentFailure(

            @RequestParam UUID userId) {

        log.info("Пользователь перенаправлен на /subscriptions/failure для userId: {}. Ошибка или отмена платежа.",
                LogSanitizer.sanitizeLogInput(userId.toString()));
        return "Оплата не удалась или была отменена. Пожалуйста, попробуйте еще раз или свяжитесь со службой поддержки.";
    }
}
