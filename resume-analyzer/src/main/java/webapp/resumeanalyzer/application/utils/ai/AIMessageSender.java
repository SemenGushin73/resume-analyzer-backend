package webapp.resumeanalyzer.application.utils.ai;


/*
 * интерфейс для отправки сообщений в ИИ модель
*/
public interface AIMessageSender {

    //метод для отправки сообщений и принятия ответа
    public String send(String message);

}
