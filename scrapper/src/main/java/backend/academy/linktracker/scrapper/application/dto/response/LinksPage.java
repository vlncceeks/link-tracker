package backend.academy.linktracker.scrapper.application.dto.response;

import java.util.List;
import java.util.Map;

public record LinksPage(Map<String, List<Long>> links, long lastId) {}
