package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

public class ListCommand implements Command {
    private final ScrapperClient scrapperClient;
    private static final Logger logger = LoggerFactory.getLogger(ListCommand.class);

    public ListCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        try {
            ListLinksResponse listLinks = scrapperClient.getLinks(chatId);
            if (listLinks.size() == 0) return "Отслеживаемых ссылок нет. \nДобавьте с помощью команды /track url";
            return listLinks.links().stream().map(link -> link.url()).collect(Collectors.joining(", "));
        }
        catch (Exception e) {
            logger.atError().addKeyValue("status", e.getMessage())
                .log("Ошибка ответа от Scrapper");
            return e.getMessage();
        }
    }
}
