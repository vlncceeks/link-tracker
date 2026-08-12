package backend.academy.linktracker.scrapper.application.client;

import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StackOverflowClient {
    Optional<StackOverflowResponse.StackOverflowItem> fetchQuestion(Long questionId);

    List<StackOverflowCommentResponse.CommentItem> fetchComments(Long questionId, Instant since);

    List<StackOverflowAnswerResponse.AnswerItem> fetchAnswers(Long questionId, Instant since);
}
