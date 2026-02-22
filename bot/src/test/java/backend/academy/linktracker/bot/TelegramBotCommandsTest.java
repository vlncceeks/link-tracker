package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.HelpCommand;
import backend.academy.linktracker.bot.command.StartCommand;
import backend.academy.linktracker.bot.dispatcher.CommandDispatcher;
import backend.academy.linktracker.bot.repository.InMemoryCommandRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TelegramBotCommandsTest {

    private TelegramBot bot;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);

        InMemoryCommandRepository repository = new InMemoryCommandRepository();
        repository.addCommand(new StartCommand());
        repository.addCommand(new HelpCommand(repository));

        dispatcher = new CommandDispatcher(repository, bot);
    }

    @Test
    void StartCommandTest() {
        Update update = createUpdate("/start", "user", 123L);

        dispatcher.handleUpdate(update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
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

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
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

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture());

        SendMessage message = captor.getValue();
        assertEquals(123L, message.getParameters().get("chat_id"));
        assertEquals(
                "Неизвестная команда. Воспользуйтесь /help.",
                message.getParameters().get("text"));
    }

    private Update createUpdate(String text, String username, Long chatId) {
        Chat chat = mock(com.pengrad.telegrambot.model.Chat.class);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);

        Message message = mock(Message.class);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);

        Update update = mock(Update.class);
        when(update.message()).thenReturn(message);

        return update;
    }
}
