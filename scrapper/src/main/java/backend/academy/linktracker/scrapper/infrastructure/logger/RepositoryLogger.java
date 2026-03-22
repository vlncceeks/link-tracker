package backend.academy.linktracker.scrapper.infrastructure.logger;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryLogger implements ApplicationRunner {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("=== ChatRepository impl: " + chatRepository.getClass().getName());
        System.out.println("=== LinkRepository impl: " + linkRepository.getClass().getName());
    }
}
