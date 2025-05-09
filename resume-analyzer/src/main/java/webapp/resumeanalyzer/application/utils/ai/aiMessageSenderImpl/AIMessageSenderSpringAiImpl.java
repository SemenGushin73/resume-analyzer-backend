




// этот класс не доступен по причине того, что использует библиотеку
// springAI версии 1.0.0-m8, что имеет баг из-за которого не работает swagger

// если же выйдет новая версия библиотеки, что работает со swagger
// то в идеале стоит удалить AIMessageSenderFeighClientImpl
// вмеcте с его вспоомогательными классами и расскоментить этот класс
//




// package webapp.resumeanalyzer.application.utils.ai.aiMessageSenderImpl;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;


// /*
//  * класс для отправки сообщений в ИИ модель
// */
// @Component
// public class AIMessageSenderSpringAiImpl implements AIMessageSender {

//     //private final ChatClient chatClient;

//     @Autowired
//     public AIMessageSenderSpringAiImpl() {
//     //    this.chatClient = chatClientBuilder.build();
//     }

//     public String send(String message) {
//     //    return chatClient.prompt(message).call().content();
//     return "whatsap fucker";
//     }
// }
