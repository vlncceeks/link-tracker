package backend.academy.linktracker.scrapper.infrastructure.service.impl;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl.StackOverflowClientWrapper;
import backend.academy.linktracker.scrapper.application.dto.InternalUpdateEvent;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.service.LinkUpdateChecker;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkUpdateChecker implements LinkUpdateChecker {
    private final LinkRepository linkRepository;

    private static final Logger logger = LoggerFactory.getLogger(StackOverflowLinkUpdateChecker.class);

    private final StackOverflowClient stackOverflowClient;

    @Override
    public boolean supports(String url) {
        return url.startsWith("https://stackoverflow.com/questions/");
    }

    @Override
    public Optional<InternalUpdateEvent> check(TrackedLink link) {
        return StackOverflowClientWrapper.parseUrl(link.getUrl()).flatMap(questionId -> {
            try {
                var question = stackOverflowClient.fetchQuestion(questionId);
                var answers = stackOverflowClient.fetchAnswers(questionId, link.getLastCheckedAt());
                var comments = stackOverflowClient.fetchComments(questionId, link.getLastCheckedAt());

                if (answers.isEmpty() && comments.isEmpty()) return Optional.empty();

                link.setLastCheckedAt(Instant.now());
                linkRepository.update(link);
                String title = question.map(q -> q.title()).orElse(link.getUrl());
                return Optional.of(buildMessage(title, answers, comments));
            } catch (RestClientException e) {
                logger.atWarn()
                        .addKeyValue("url", link.getUrl())
                        .addKeyValue("error", e.getMessage())
                        .log("Ошибка при обращении к StackOverflow API");
                return Optional.empty();
            }
        });
    }

    private InternalUpdateEvent buildMessage(
            String questionTitle,
            List<StackOverflowAnswerResponse.AnswerItem> answers,
            List<StackOverflowCommentResponse.CommentItem> comments) {
        StringBuilder sb = new StringBuilder();
        sb.append("Вопрос: ").append(questionTitle).append("\n\n");

        String author = "Unknown";

        for (var answer : answers) {
            String preview = answer.body() != null && answer.body().length() > 200
                    ? answer.body().substring(0, 197) + "..."
                    : answer.body();
            author = answer.owner().displayName();
            sb.append("Новый ответ\n")
                    .append("Время: ")
                    .append(answer.creationDate())
                    .append("\n")
                    .append("Превью: ")
                    .append(preview)
                    .append("\n\n");
        }

        for (var comment : comments) {
            String preview = comment.body() != null && comment.body().length() > 200
                    ? comment.body().substring(0, 197) + "..."
                    : comment.body();
            author = comment.owner().displayName();
            sb.append("Новый комментарий\n")
                    .append("Время: ")
                    .append(comment.creationDate())
                    .append("\n")
                    .append("Превью: ")
                    .append(preview)
                    .append("\n\n");
        }

        return new InternalUpdateEvent(author, sb.toString().trim());
    }
}
