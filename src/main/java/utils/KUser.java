package utils;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;

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
    public Collection<User.UserFlag> getUserFlags() { return this.u.getFlags(); }
    public User.Profile getProfile() {
        return this.u.retrieveProfile().complete();
    }

    public boolean isBlocked() {
        return false;
    }
    public boolean isBot() {
        return this.u.isBot();
    }
    public boolean isSystem() {
        return this.u.isSystem();
    }

}
