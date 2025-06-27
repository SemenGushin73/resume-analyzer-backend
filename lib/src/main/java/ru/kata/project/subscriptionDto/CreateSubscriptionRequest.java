package ru.kata.project.subscriptionDto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kata.project.model.SubscriptionType;


import java.util.UUID;

/**
 * DTO для передачи информации, необходимой для создания новой подписки
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {
    @NotNull
    private UUID userId;
    @NotNull
    private SubscriptionType type;
}