package backend.academy.linktracker.bot.bootstrap;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotCommandsSetup {
    private static final Logger logger = LoggerFactory.getLogger(BotCommandsSetup.class);
    private final List<Command> commands;
    private final CommandRepository commandRepository;
    private final TelegramBot bot;

    @PostConstruct
    public void setupCommands() {
        try {
            BotCommand[] botCommands = commands.stream()
                    .map(c -> new BotCommand(c.getName(), c.getDescription()))
                    .toArray(BotCommand[]::new);
            bot.execute(new SetMyCommands(botCommands));
            logger.atInfo().log("Настроено меню команд");
        } catch (Exception e) {
            logger.atWarn().addKeyValue("error", e.getMessage()).log("Не удалось настроить меню команд");
        }
        commands.forEach(commandRepository::addCommand);
        logger.atInfo().log("Команды инициализированы");
    }
}
