package utils;

import net.dv8tion.jda.api.entities.User;

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
    public String getEffectiveAvatarUrl() {
        return this.u.getEffectiveAvatarUrl();
    }
    public String getId() {
        return this.u.getId();
    }
    public Long getIdLong() {
        return this.u.getIdLong();
    }

}
