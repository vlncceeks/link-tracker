package backend.academy.linktracker.bot.application.command;

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
                .findCommand(commandName)
                .ifPresentOrElse(
                        command -> {
                            logger.atInfo()
                                    .addKeyValue("chatId", chatId)
                                    .addKeyValue("username", username)
                                    .addKeyValue("command", commandName)
                                    .log("Выполняется команда");

                            try {
                                String response = command.execute(username, args);
                                bot.execute(new SendMessage(chatId, response));
                                logger.atDebug()
                                        .addKeyValue("chatId", chatId)
                                        .addKeyValue("username", username)
                                        .log("Отправлено сообщение /" + command.getName());
                                logger.atInfo().log("Команда /" + command.getName() + " выполнена");
                            } catch (Exception e) {
                                logger.atError()
                                        .addKeyValue("chatId", chatId)
                                        .addKeyValue("username", username)
                                        .addKeyValue("command", commandName)
                                        .setCause(e)
                                        .log("Ошибка при отправке сообщения для команды /" + command.getName());
                            }
                        },
                        () -> {
                            logger.atWarn()
                                    .addKeyValue("chatId", chatId)
                                    .addKeyValue("username", username)
                                    .addKeyValue("command", commandName)
                                    .log("Неизвестная команда: отсутствует в репозитории");
                            try {
                                bot.execute(new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help."));
                            } catch (Exception e) {
                                logger.atError()
                                        .addKeyValue("chatId", chatId)
                                        .addKeyValue("username", username)
                                        .setCause(e)
                                        .log("Не удалось отправить сообщение об неизвестной команде");
                            }
                        });
    }
}
