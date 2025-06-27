package ru.kata.project.subscriptionDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи информации, возвращаемой после инициации платежной сессии
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSessionResponseDto {
    private String paymentId;
    private String confirmationUrl;
    private String qrCodeData;
}