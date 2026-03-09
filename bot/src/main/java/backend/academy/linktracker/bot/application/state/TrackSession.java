package backend.academy.linktracker.bot.application.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackSession {
    private TrackState state;
    private String url;

    public TrackSession() {
        this.state = TrackState.WAITING_FOR_URL;
    }
}
