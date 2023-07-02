package Kayo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Kayo {

    private static JDA jda;

    public static void main(String[] args) {

        // String token = "MTEyNDc4NTEzMzAwMjY4NjUyNA.G68Bhq.K7-V6t7ks8neHv0RWsxbqBMerIBcGJgVWaDUvM"; // Main Bot
        String token = "MTEyNTA5NTY3MjA2NTA0ODY1Ng.GA75pJ.hxXkEyzi4W0PL80mWBRISRyGV-CtpkgjOSZVLM"; // Test Bot
        jda = JDABuilder.createLight(token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.STICKER)
                .setActivity(Activity.playing("booting up"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();


        // Events
        jda.addEventListener(new events.Ready());
        jda.addEventListener(new commands.setupStuff());

        // Commands
        jda.addEventListener(new commands.Info());
        jda.addEventListener(new commands.addEmoji());

        // Functions

    }

    public static JDA getJda() {
        return jda;
    }

}
