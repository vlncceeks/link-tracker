package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.repository.CommandRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);
    private final CommandRepository repository;

    public HelpCommand(CommandRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(Long chatId, String username, String[] args, TelegramBot bot) {
        String message = "Доступные команды:\n"
                + repository.getAllCommands().stream()
                        .map(command -> "/" + command.getName())
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("Нет доступных команд");
        bot.execute(new SendMessage(chatId, message));
        logger.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("username", username)
                .log("Отправлено сообщение /help");
        logger.atInfo().log("Команда /help выполнена");
    }
}
