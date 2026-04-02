package backend.academy.linktracker.bot.application.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackSession {
    private TrackState state;
    private TrackCommandType commandType;
    private String url;

    public TrackSession() {
        this.state = TrackState.WAITING_FOR_URL;
    }

    public TrackSession(TrackCommandType commandType) {
        this.commandType = commandType;
        this.state = TrackState.WAITING_FOR_URL;
    }
}
