package webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl.feignClient;


import lombok.Value;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Тело запроса к OpenRouter API
 * Содержит имя модели и историю диалога с ии
 */
@Value
@With
@SuppressFBWarnings({"ES_COMPARING_PARAMETER_STRING_WITH_EQ", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
public class OpenRouterRequest {
    String model;
    List<ChatMessage> messages;

    public OpenRouterRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        if (messages != null) {
            this.messages = new ArrayList<>(messages);
        } else {
            this.messages = new ArrayList<>();
        }
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(this.messages));
    }
}
