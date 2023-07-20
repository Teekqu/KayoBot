package utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class Get {

    public static Color embedColor() {
        return Color.decode("#7700ff");
    }
    public static Color embedColor(boolean success) {
        if(success) return Color.decode("#00ff00");
        return Color.decode("#ff0000");
    }
    public static Long id(String key, boolean addOne) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT value FROM IDs WHERE key="+key+";");
            if(!rs.next()) {
                if(addOne) {
                    stm.execute("INSERT INTO IDs(key, value) VALUES('"+key+"',1);");
                    try { stm.close(); } catch (Exception ignored) { }
                    return 1L;
                }
                return 0L;
            }
            long value = Long.parseLong(rs.getString(1));
            if(addOne) {
                stm.execute("UPDATE IDs SET value="+(value+1)+" WHERE key='"+key+"';");
                try { stm.close(); } catch (Exception ignored) { }
                return (value+1);
            }
            try { stm.close(); } catch (Exception ignored) { }
            return value;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return null;
        }
    }
    public static KUser timo() {
        return new KUser(Kayo.Kayo.getJda().getUserById("473737542630637569"));
    }
    public static String inviteLink() {
        return "https://discord.com/api/oauth2/authorize?client_id="+Kayo.Kayo.getJda().getSelfUser().getId()+"&permissions=8&scope=bot%20applications.commands";
    }
    public static String topggVoteLink() {
        return "https://top.gg/bot/"+Kayo.Kayo.getJda().getSelfUser().getId()+"/vote";
    }
    public static long topggVotes() {

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://top.gg/api/bots/1124785133002686524").openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjExMjQ3ODUxMzMwMDI2ODY1MjQiLCJib3QiOnRydWUsImlhdCI6MTY4OTY5MDM5OH0.HMasoFC67I2eTYtrQJ5CJtQdB025SzjM5SrLlNFl6nw");
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jo = new JSONObject(response.toString());

            return Long.parseLong(""+jo.get("monthlyPoints"));
        } catch (Exception err) {
            err.printStackTrace();
            return 0;
        }
    }
    public static Collection<KUser> voteReminders(boolean onlyToRemind) {
        Statement stm = MySQL.connect();
        try {
            Collection<KUser> users = new ArrayList<>();
            ResultSet rs = stm.executeQuery("SELECT * FROM VoteReminder");
            while(rs.next()) {

                User user = Kayo.Kayo.getJda().getUserById(rs.getString(1));
                if(user==null) continue;
                KUser u = new KUser(user);
                if(onlyToRemind && u.getLastVote()<= TimeFormat.RELATIVE.now().toInstant().getEpochSecond()) users.add(u);
                if(!onlyToRemind) users.add(u);

            }
            try { stm.close(); } catch (Exception ignored) { }
            return users;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static int limit(String id, boolean hasPremium) {
        Statement stm = MySQL.connect();
        try {
            ResultSet rs = stm.executeQuery("SELECT * FROM Limits WHERE id='"+id+"';");
            if(!rs.next()) {
                try { stm.close(); } catch (Exception ignored) { }
                return 0;
            }
            int normaly = Integer.parseInt(rs.getString("count"));
            int premium = Integer.parseInt(rs.getString("countPremium"));
            try { stm.close(); } catch (Exception ignored) { }
            if(hasPremium) return premium;
            return normaly;
        } catch (Exception err) {
            try { stm.close(); } catch (Exception ignored) { }
            err.printStackTrace();
            return 0;
        }
    }

}
