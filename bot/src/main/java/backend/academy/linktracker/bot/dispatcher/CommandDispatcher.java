package backend.academy.linktracker.bot.dispatcher;

import backend.academy.linktracker.bot.repository.CommandRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);
    private final CommandRepository repository;
    private final TelegramBot bot;

    public CommandDispatcher(CommandRepository repository, TelegramBot bot) {
        this.repository = repository;
        this.bot = bot;
    }

    public void handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) return;

        String text = update.message().text().trim();
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();

        logger.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("username", username)
                .addKeyValue("text", text)
                .log("Получено сообщение");

        if (!text.startsWith("/")) {
            logger.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("username", username)
                    .addKeyValue("text", text)
                    .log("Неизвестная команда: сообщение не начинается с '/'");
            bot.execute(new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help."));
            return;
        }

        String[] words = text.substring(1).split(" ");
        String commandName = words[0].toLowerCase();
        String[] args = Arrays.copyOfRange(words, 1, words.length);

        repository
                .getCommand(commandName)
                .ifPresentOrElse(
                        command -> {
                            logger.atInfo()
                                    .addKeyValue("chatId", chatId)
                                    .addKeyValue("username", username)
                                    .addKeyValue("command", commandName)
                                    .log("Выполняется команда");
                            command.execute(chatId, username, args, bot);
                        },
                        () -> {
                            logger.atWarn()
                                    .addKeyValue("chatId", chatId)
                                    .addKeyValue("username", username)
                                    .addKeyValue("command", commandName)
                                    .log("Неизвестная команда: отсутствует в репозитории");
                            bot.execute(new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help."));
                        });
    }
}
