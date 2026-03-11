package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.bot.infrastructure.service.UpdateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
    private final UpdateService updateService;

    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @PostMapping("/updates")
    public ResponseEntity<Void> postUpdate(@RequestBody @Valid LinkUpdateRequest request) {
        logger.atInfo()
                .addKeyValue("url", request.url())
                .addKeyValue("chatCount", request.tgChatIds().size())
                .log("Получено обновление ссылки");

        updateService.receive(request);
        return ResponseEntity.ok().build();
    }
}
