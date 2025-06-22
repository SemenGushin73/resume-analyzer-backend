package ru.kata.project.subscriptionDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionMetaDto {
    private String message;
    private String paymentId;
    private String error;
    private String details;
    private String subscriptionType;
    private String confirmationUrl;
    private String action;
    private UUID subscriptionId;
    private Map<String, Object> externalData;
}
