package ru.kata.project.subscriptionDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionMetaDto {
    private String message;
    private String paymentId;
    private String error;
    private String details;
    private String subscriptionType;
    private String confirmationUrl;
}
