package webapp.resumeanalyzer.application.utils.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/*
 * класс для отправки сообщений в ИИ модель
*/
@Component
public class AIMessageSender {

    private final ChatClient chatClient;

    @Autowired
    public AIMessageSender(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String send(String message) {
        return chatClient.prompt(message).call().content();
    }
}
