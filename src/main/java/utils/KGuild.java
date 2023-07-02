package utils;

import net.dv8tion.jda.api.entities.Guild;

public class KGuild {

    private Guild g;

    public KGuild(Guild guild) {
        this.g = guild;
    }

    public String getName() {
        return this.g.getName();
    }
    public String getId() {
        return this.g.getId();
    }
    public Long getIdLong() {
        return this.g.getIdLong();
    }
    public String getIconUrl() {
        return this.g.getIconUrl();
    }

}
