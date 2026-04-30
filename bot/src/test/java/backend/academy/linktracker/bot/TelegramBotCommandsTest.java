package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.application.command.impl.HelpCommand;
import backend.academy.linktracker.bot.application.command.impl.StartCommand;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import backend.academy.linktracker.bot.infrastructure.registry.InMemoryCommandRepository;
import backend.academy.linktracker.bot.infrastructure.service.CommandService;
import backend.academy.linktracker.bot.infrastructure.service.SessionService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramBotCommandsTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private TrackSessionRepository sessionRepository;

    private CommandDispatcher dispatcher;
    private TrackDialogHandler handler;
    private TelegramBot bot;
    @Mock
    private CommandService commandService;
    @Mock
    private SessionService sessionService;

    @Captor
    ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);

        InMemoryCommandRepository repository = new InMemoryCommandRepository();
        repository.addCommand(new StartCommand(scrapperClient));
        repository.addCommand(new HelpCommand(repository));

        handler = new TrackDialogHandler(scrapperClient, sessionService);

        CommandService realCommandService = new CommandService(repository); // реальный сервис
        dispatcher = new CommandDispatcher(realCommandService, bot, handler);
    }

    @Test
    void StartCommandTest() {
        Update update = createUpdate("/start", "user", 123L);

        dispatcher.handleUpdate(update);

        verify(bot).execute(captor.capture());

        SendMessage message = captor.getValue();
        assertEquals(123L, message.getParameters().get("chat_id"));
        assertEquals(
                "Добро пожаловать, user! Используйте /help, чтобы посмотреть доступные команды.",
                message.getParameters().get("text"));
    }

    @Test
    void HelpCommandTest() {
        Update update = createUpdate("/help", "user", 123L);

        dispatcher.handleUpdate(update);

        verify(bot).execute(captor.capture());

        SendMessage message = captor.getValue();
        String text = (String) message.getParameters().get("text");

        assert (text.contains("/start"));
        assert (text.contains("/help"));
    }

    @Test
    void UnknownCommandTest() {
        Update update = createUpdate("/unknown", "user", 123L);

        dispatcher.handleUpdate(update);

        verify(bot).execute(captor.capture());

        SendMessage message = captor.getValue();
        assertEquals(123L, message.getParameters().get("chat_id"));
        assertEquals(
                "Неизвестная команда. Воспользуйтесь /help.",
                message.getParameters().get("text"));
    }

    private Update createUpdate(String text, String username, Long chatId) {
        Update update = mock(Update.class, RETURNS_DEEP_STUBS);

        when(update.message().chat().id()).thenReturn(chatId);
        when(update.message().chat().username()).thenReturn(username);
        when(update.message().text()).thenReturn(text);

        return update;
    }
}
