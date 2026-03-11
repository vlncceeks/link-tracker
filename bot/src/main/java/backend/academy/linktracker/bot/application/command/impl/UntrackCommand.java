package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand implements Command {
    private final ScrapperClient scrapperClient;

    public UntrackCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String getName() {
        return "untrack";
    }

    @Override
    public String getDescription() {
        return "Прекратить отслеживание ссылки";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        if (args.length == 0) {
            return "Введите команду в формате:\n/untrack {url}";
        }
        LinkResponse response = scrapperClient.removeLink(chatId, new RemoveLinkRequest(args[0]));
        return "Ссылка " + response.url() + " больше не отслеживается.";
    }
}
