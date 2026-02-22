package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public void execute(Long chatId, String username, String[] args, TelegramBot bot) {
        String message = "Добро пожаловать, " + username + "! Используйте /help, чтобы посмотреть доступные команды.";
        bot.execute(new SendMessage(chatId, message));
        logger.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("username", username)
                .log("Отправлено приветственное сообщение");
        logger.atInfo().log("Команда /start выполнена");
    }
}
