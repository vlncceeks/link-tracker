package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartCommand implements Command {
    private final ScrapperClient scrapperClient;
    private static final Logger logger = LoggerFactory.getLogger(StartCommand.class);

    public StartCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        try {
            scrapperClient.registerChat(chatId);
        } catch (Exception e) {
            logger.atWarn().addKeyValue("chatId", chatId)
                .log(e.getMessage());
        }
        String message = "Добро пожаловать, " + username + "! Используйте /help, чтобы посмотреть доступные команды.";
        return message;
    }
}
