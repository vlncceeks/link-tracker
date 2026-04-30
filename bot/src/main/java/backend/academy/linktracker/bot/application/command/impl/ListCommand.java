package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
    public String getDescription() {
        return "Вывести список всех ссылок, отслеживаемых пользователем";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        ListLinksResponse listLinks = scrapperClient.getLinks(chatId);
        return Optional.ofNullable(listLinks)
            .filter(links -> !links.links().isEmpty())
            .map(links -> links.links().stream()
                .map(LinkResponse::url)
                .collect(Collectors.joining(", ")))
            .orElse("Отслеживаемых ссылок нет. \nДобавьте с помощью команды /track url");
    }
}
