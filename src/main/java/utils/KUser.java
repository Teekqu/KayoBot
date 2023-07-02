package utils;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.time.OffsetDateTime;

public class KUser {

    private User u;

    public KUser(User u) {
        this.u = u;
    }

    public String getName() {
        return this.u.getName();
    }
    public String getUsername() {
        return this.u.getAsTag().split("#")[0];
    }
    public String getAsMention() {
        return this.u.getAsMention();
    }
    public String getEffectiveAvatarUrl() {
        return this.u.getEffectiveAvatarUrl();
    }
    public String getId() {
        return this.u.getId();
    }
    public Long getIdLong() {
        return this.u.getIdLong();
    }
    public String getProfileUrl() {
        return "https://discord.com/users/"+this.getId();
    }
    public OffsetDateTime getTimeCreated() {
        return this.u.getTimeCreated();
    }

    public boolean isBlocked() {
        return false;
    }

}
