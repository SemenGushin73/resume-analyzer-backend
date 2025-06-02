package supportchatbotservice.domain.ports;

import java.util.Optional;

public interface FaqServicePort {
    Optional<String> findAnswer(String question);
}