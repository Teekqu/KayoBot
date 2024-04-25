package utils;

import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;

public class KUser {

    private User u;

    public KUser(User u) {
        this.u = u;
    }

    public User getUser() {
        return this.u;
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

    /* Eigene Funktionen */
    public long getVoteCount() {
        try {
            ResultSet rs = Kayo.Kayo.getDatabase().executeQuery("SELECT * FROM TopggVotes WHERE userId="+this.getId());
            if(!rs.next()) {
                Kayo.Kayo.getDatabase().execute("INSERT INTO TopggVotes(userId, count, lastVote) VALUES("+this.getId()+",0,0);");
                try { rs.close(); } catch (Exception ignored) { }
                return 0;
            }
            long count = Long.parseLong(rs.getString(2));
            try { rs.close(); } catch (Exception ignored) { }
            return count;
        } catch (Exception err) {
            err.printStackTrace();
            return -1;
        }
    }
    public long getLastVote() {
        try {
            ResultSet rs = Kayo.Kayo.getDatabase().executeQuery("SELECT * FROM TopggVotes WHERE userId="+this.getId());
            if(!rs.next()) {
                Kayo.Kayo.getDatabase().execute("INSERT INTO TopggVotes(userId, count, lastVote) VALUES("+this.getId()+",0,0);");
                try { rs.close(); } catch (Exception ignored) { }
                return 0;
            }
            long time = Long.parseLong(rs.getString(3));
            try { rs.close(); } catch (Exception ignored) { }
            return time;
        } catch (Exception err) {
            err.printStackTrace();
            return -1;
        }
    }
    public boolean setVoteCount(Long newCount, Long lastVote) {
        try {
            long oldCount = this.getVoteCount();
            Kayo.Kayo.getDatabase().execute("UPDATE TopggVotes SET count="+newCount+", lastVote="+lastVote+" WHERE userId="+this.getId());
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }
    public boolean enableVoteReminder(long timestamp) {
        if(Get.voteReminders(false).contains(this.getUser())) return false;
        try {
            Kayo.Kayo.getDatabase().execute("INSERT INTO VoteReminder(userId, time) VALUES("+this.getId()+","+timestamp+");");
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }
    public boolean disableVoteReminder() {
        if(!Get.voteReminders(false).contains(this.getUser())) return false;
        try {
            Kayo.Kayo.getDatabase().execute("DELETE FROM VoteReminder WHERE userId="+this.getId());
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

}
