package utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

    public Collection<HashMap<String, String>> getAutoResponse() {
        Statement stm = MySQL.connect();
        try {
            Collection<HashMap<String, String>> maps = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoReact WHERE guildId="+this.getId());
            while(rs.next()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", rs.getString(2));
                map.put("channelId", rs.getString(3));
                map.put("trigger", rs.getString(4));
                map.put("answer", rs.getString(5));
                maps.add(map);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return maps;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }
    public HashMap<String, String> getAutoResponse(long id) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT * FROM AutoReact WHERE guildId="+this.getId()+" AND id="+id);
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return null;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("id", rs.getString(2));
            map.put("channelId", rs.getString(3));
            map.put("trigger", rs.getString(4));
            map.put("answer", rs.getString(5));
            return map;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public boolean addAutoResponse(long id, GuildMessageChannel ch, String trigger, String answer) {
        return this.addAutoResponse(id, ch.getIdLong(), trigger, answer);
    }
    public boolean addAutoResponse(long id, String trigger, String answer) {
        return this.addAutoResponse(id, 0, trigger, answer);
    }
    private boolean addAutoResponse(long id, long channelId, String trigger, String answer) {
        Statement stm = MySQL.connect();
        try {
            if(this.getAutoResponse(id)!=null) return false;
            stm.execute("INSERT INTO AutoResponse(guildId, id, channelId, trigger, answer) VALUES("+this.getId()+","+id+","+channelId+",'"+trigger+"','"+answer+"');");
            try { stm.close(); } catch (Exception ignored) { }
            return true;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return false;
        }
    }

}
