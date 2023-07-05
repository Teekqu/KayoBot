package utils;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class Get {

    public static Color embedColor() {
        return Color.decode("#7700ff");
    }
    public static Color embedColor(boolean success) {
        if(success) return Color.decode("#00ff00");
        return Color.decode("#ff0000");
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
        return 0;
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
