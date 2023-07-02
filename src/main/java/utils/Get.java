package utils;

import java.awt.*;

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

}
