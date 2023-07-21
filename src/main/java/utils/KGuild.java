package utils;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class KGuild {

    private Guild g;

    private int getUserCountPrivate() {
        int users = 0;
        for(Member u : g.getMembers()) {
            if(!u.getUser().isBot()) users++;
        }
        return users;
    }
    private int getBotCountPrivate() {
        int bots = 0;
        for(Member u : g.getMembers()) {
            if(u.getUser().isBot()) bots++;
        }
        return bots;
    }

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
    public Member getOwner() { return this.g.getOwner(); }
    public String getOwnerId() { return this.g.getOwnerId(); }
    public Long getOwnerIdLong() { return this.g.getOwnerIdLong(); }
    public int getMemberCount() { return this.g.getMemberCount(); }
    public int getUserCount() { return this.getUserCountPrivate(); }
    public int getBotCount() { return this.getBotCountPrivate(); }
    public int getBoostCount() { return this.g.getBoostCount(); }
    public Guild.BoostTier getBoostTier() { return this.g.getBoostTier(); }
    public Collection<Member> getBoosters() { return this.g.getBoosters(); }
    public List<GuildChannel> getChannels() { return this.g.getChannels(); }
    public List<Role> getRoles() { return this.g.getRoles(); }
    public OffsetDateTime getTimeCreated() { return this.g.getTimeCreated(); }

    public boolean hasPremium() {
        return false;
    }

    public Collection<HashMap<String, String>> getAutoDelete() {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> channels = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoDelete WHERE guildId="+this.getId());
            while(rs.next()) {
                HashMap<String, String> map = new HashMap<>();
                Channel ch = this.g.getGuildChannelById(rs.getString(2));
                if(ch == null) continue;
                map.put("channelId", ch.getId());
                map.put("seconds", rs.getString(3));
                map.put("delPins", rs.getString(4));
                map.put("delBots", rs.getString(5));
                map.put("delUser", rs.getString(6));
                channels.add(map);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return channels;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public HashMap<String, String> getAutoDelete(GuildMessageChannel ch) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoDelete WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            GuildChannel channel = this.g.getGuildChannelById(rs.getString(2));
            if(channel == null) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("channelId", ch.getId());
            map.put("seconds", rs.getString(3));
            map.put("delPins", rs.getString(4));
            map.put("delBots", rs.getString(5));
            map.put("delUser", rs.getString(6));
            try { stm.close(); } catch (Exception ignored) { }
            return map;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addAutoDelete(GuildMessageChannel ch, Long seconds, boolean deletePins, boolean deleteBots, boolean deleteUser) {
        if(this.getAutoDelete(ch)!=null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("INSERT INTO AutoDelete(guildId, channelId, seconds, delPins, delBots, delUser) VALUES("+this.getId()+","+ch.getId()+","+seconds+",'"+deletePins+"','"+deleteBots+"','"+deleteUser+"');");
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean removeAutoDelete(GuildMessageChannel ch) {
        if(this.getAutoDelete(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("DELETE FROM AutoDelete WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean editAutoDelete(GuildMessageChannel ch, Long seconds, boolean deletePins, boolean deleteBots, boolean deleteUser) {
        if(this.getAutoDelete(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("UPDATE AutoDelete SET seconds="+seconds+",delPins='"+deletePins+"',delBots='"+deleteBots+"',delUser='"+deleteUser+"' WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }

    public Collection<GuildMessageChannel> getAutoReact() {
        Statement stm = MySQL.connect();
        try {
            Collection<GuildMessageChannel> channels = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoReact WHERE guildId="+this.getId());
            while(rs.next()) {
                Channel ch = this.g.getGuildChannelById(rs.getString(2));
                if(ch == null) continue;
                if(!channels.contains(ch)) channels.add((GuildMessageChannel) ch);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return channels;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Collection<Emoji> getAutoReact(GuildMessageChannel ch) {
        Statement stm = MySQL.connect();
        try {
            Collection<Emoji> emojis = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoReact WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            while(rs.next()) {

                String emoji1 = rs.getString(3);
                Emoji emoji;
                try { emoji = Emoji.fromFormatted(emoji1); } catch (Exception err) { continue; }
                emojis.add(emoji);

            }
            try { stm.close(); } catch (Exception ignored) { }
            if(emojis.size()==0) return null;
            return emojis;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addAutoReact(GuildMessageChannel ch, Emoji emoji) {

        if(this.getAutoReact(ch)!=null && this.getAutoReact(ch).equals(emoji)) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("INSERT INTO AutoReact(guildId, channelId, emoji) VALUES("+this.getId()+","+ch.getId()+",'"+emoji.getFormatted()+"');");
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }

    }
    public boolean removeAutoReact(GuildMessageChannel ch, Emoji emoji) {
        System.out.println(emoji.getFormatted());
        if(this.getAutoReact(ch)==null || !this.getAutoReact(ch).contains(emoji)) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("DELETE FROM AutoReact WHERE guildId="+this.getId()+" AND channelId="+ch.getId()+" AND emoji='"+emoji.getFormatted()+"'");
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean removeAutoReact(GuildMessageChannel ch) {
        if(this.getAutoReact(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("DELETE FROM AutoReact WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }

    public Collection<Role> getJoinRolesList() {
        Statement stm = MySQL.connect();
        try {
            Collection<Role> roles = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM JoinRoles WHERE guildId="+this.getId());
            while(rs.next()) {
                Role r = this.g.getRoleById(rs.getString(2));
                if(r == null) continue;
                if(!roles.contains(r)) roles.add(r);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return roles;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Collection<HashMap<String, String>> getJoinRoles() {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> roles = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM JoinRoles WHERE guildId="+this.getId());
            while(rs.next()) {
                Role r = this.g.getRoleById(rs.getString(2));
                if(r == null) continue;
                HashMap<String, String> map = new HashMap<>();
                map.put("guildId", this.getId());
                map.put("roleId", r.getId());
                map.put("addUser", rs.getString(3));
                map.put("addBot", rs.getString(4));
                if(!roles.contains(map)) roles.add(map);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return roles;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public HashMap<String, String> getJoinRole(Role role) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT * FROM JoinRoles WHERE guildId="+this.getId()+" AND roleId="+role.getId());
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            Role r = this.g.getRoleById(rs.getString(2));
            if(r == null) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("guildId", this.getId());
            map.put("roleId", r.getId());
            map.put("addUser", rs.getString(3));
            map.put("addBot", rs.getString(4));
            try { stm.close(); } catch (Exception ignored) { }
            return map;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addJoinRole(Role role, boolean addUser, boolean addBot) {
        if(this.getJoinRole(role)!=null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("INSERT INTO JoinRoles(guildId, roleId, addUser, addBot) VALUES("+this.getId()+","+role.getId()+",'"+addUser+"','"+addBot+"');");
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean editJoinRole(Role role, boolean addUser, boolean addBot) {
        if(this.getJoinRole(role)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("UPDATE JoinRoles SET addUser='"+addUser+"', addBot='"+addBot+"' WHERE guildId="+this.getId()+" AND roleId="+role.getId());
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean removeJoinRole(Role role) {

			if(this.getJoinRole(role)==null) return false;
			Statement stm = MySQL.connect();
			try {
                stm.execute("DELETE FROM JoinRoles WHERE guildId="+this.getId()+" AND roleId="+role.getId());
			    try { stm.close(); } catch(Exception ignored) { }
			    return true;
			} catch(Exception err) {
			    try { stm.close(); } catch(Exception ignored) { }
			    err.printStackTrace();
			    return false;
			}

    }

    public Collection<HashMap<String, String>> getWelcomeMessages() {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> maps = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM WelcomeMessages WHERE guildId="+this.getId());
            while(rs.next()) {
                HashMap<String, String> map = new HashMap<>();
                if(this.g.getGuildChannelById(rs.getString(2)) == null) continue;
                map.put("guildId", this.getId());
                map.put("channelId", rs.getString(2));
                map.put("message", rs.getString(3));
                map.put("sendUser", rs.getString(4));
                map.put("sendBot", rs.getString(5));
                maps.add(map);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return maps;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public HashMap<String, String> getWelcomeMessage(GuildMessageChannel ch) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT * FROM WelcomeMessages WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            if(this.g.getGuildChannelById(rs.getString(2)) == null) {
                try { stm.close(); } catch (Exception ignored) { }
                this.removeWelcomeMessage(ch);
                return null;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("guildId", this.getId());
            map.put("channelId", rs.getString(2));
            map.put("message", rs.getString(3));
            map.put("sendUser", rs.getString(4));
            map.put("sendBot", rs.getString(5));
            try { stm.close(); } catch (Exception ignored) { }
            return map;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addWelcomeMessage(GuildMessageChannel ch, String message, boolean sendUser, boolean sendBot) {
        if(this.getWelcomeMessage(ch)!=null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("INSERT INTO WelcomeMessages(guildId, channelId, message, sendUser, sendBot) VALUES("+this.getId()+","+ch.getId()+",'"+message+"','"+sendUser+"','"+sendBot+"')");
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean editWelcomeMessage(GuildMessageChannel ch, String message, boolean sendUser, boolean sendBot) {
        if(this.getWelcomeMessage(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("UPDATE WelcomeMessages SET message='"+message+"', sendUser='"+sendUser+"', sendBot='"+sendBot+"' WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean removeWelcomeMessage(GuildMessageChannel ch) {
        if(this.getWelcomeMessage(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("DELETE FROM WelcomeMessages WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }

    public Collection<HashMap<String, String>> getLogging() {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> maps = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM Logging WHERE guildId="+this.getId());
            while(rs.next()) {
                HashMap<String, String> map = new HashMap<>();
                if(this.g.getGuildChannelById(rs.getString(2)) == null) continue;
                map.put("guildId", this.getId());
                map.put("channelId", rs.getString(2));
                map.put("member", rs.getString(3));
                map.put("user", rs.getString(4));
                map.put("server", rs.getString(5));
                map.put("channel", rs.getString(6));
                map.put("role", rs.getString(7));
                map.put("message", rs.getString(8));
                map.put("moderation", rs.getString(9));
                map.put("serverJoinLeave", rs.getString(10));
                map.put("voiceJoinLeave", rs.getString(11));
                maps.add(map);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return maps;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public HashMap<String, String> getLogging(GuildMessageChannel ch) {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> maps = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM Logging WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            if(this.g.getGuildChannelById(rs.getString(2)) == null) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("guildId", this.getId());
            map.put("channelId", rs.getString(2));
            map.put("member", rs.getString(3));
            map.put("user", rs.getString(4));
            map.put("server", rs.getString(5));
            map.put("channel", rs.getString(6));
            map.put("role", rs.getString(7));
            map.put("message", rs.getString(8));
            map.put("moderation", rs.getString(9));
            map.put("serverJoinLeave", rs.getString(10));
            map.put("voiceJoinLeave", rs.getString(11));
            maps.add(map);
            try { stm.close(); } catch (Exception ignored) { }
            return map;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addLogging(GuildMessageChannel ch, boolean member, boolean user, boolean server, boolean channel, boolean role, boolean message, boolean moderation, boolean serverJoinLeave, boolean voiceJoinLeave) {
        if(this.getLogging(ch)!=null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("INSERT INTO Logging(guildId,channelId,member,user,server,channel,role,message,moderation,serverJoinLeave,voiceJoinLeave) VALUES("+this.getId()+","+ch.getId()+",'"+member+"','"+user+"','"+server+"','"+channel+"','"+role+"','"+message+"','"+moderation+"','"+serverJoinLeave+"','"+voiceJoinLeave+"')");
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean editLogging(GuildMessageChannel ch, boolean member, boolean user, boolean server, boolean channel, boolean role, boolean message, boolean moderation, boolean serverJoinLeave, boolean voiceJoinLeave) {
        if(this.getLogging(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("UPDATE Logging SET member='"+member+"',user='"+user+"',server='"+server+"',channel='"+channel+"',role='"+role+"',message='"+message+"',moderation='"+moderation+"',serverJoinLeave='"+serverJoinLeave+"',voiceJoinLeave='"+voiceJoinLeave+"' WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }
    public boolean deleteLogging(GuildMessageChannel ch) {
        if(this.getLogging(ch)==null) return false;
        Statement stm = MySQL.connect();
        try {
            stm.execute("DELETE FROM Logging WHERE guildId="+this.getId()+" AND channelId="+ch.getId());
            try { stm.close(); } catch(Exception ignored) { }
            return true;
        } catch(Exception err) {
            try { stm.close(); } catch(Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }

}
