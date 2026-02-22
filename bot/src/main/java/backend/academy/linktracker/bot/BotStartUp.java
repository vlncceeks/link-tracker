package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.runner.BotRunner;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class BotStartUp {
    private final TelegramBot bot;

    public BotStartUp(TelegramBot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void start() {
        BotRunner.run(bot);
    }
}
