package backend.academy.linktracker.scrapper.infrastructure.service;

public class LinkTypeResolver {
    public static LinkType resolve(String url) {
        if (url.startsWith("https://github.com/")) return LinkType.GITHUB;
        if (url.startsWith("https://stackoverflow.com/questions/")) return LinkType.STACKOVERFLOW;
        return LinkType.UNKNOWN;
    }
}
