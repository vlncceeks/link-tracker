package backend.academy.linktracker.scrapper.application.client.GitHubClientImpl;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubUrlParser {
    private static final Pattern GITHUB_URL = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)");

    public static Optional<String[]> parseUrl(String url) {
        Matcher matcher = GITHUB_URL.matcher(url);
        if (!matcher.matches()) return Optional.empty();
        return Optional.of(new String[] {matcher.group(1), matcher.group(2)});
    }
}
