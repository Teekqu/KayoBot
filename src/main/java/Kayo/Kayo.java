package Kayo;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Kayo {

    private static JDA jda;

    public static void main(String[] args) {

        String token = "MTEyNDc4NTEzMzAwMjY4NjUyNA.G68Bhq.K7-V6t7ks8neHv0RWsxbqBMerIBcGJgVWaDUvM"; // Main Bot
        // String token = "MTEyNTA5NTY3MjA2NTA0ODY1Ng.GA75pJ.hxXkEyzi4W0PL80mWBRISRyGV-CtpkgjOSZVLM"; // Test Bot
        jda = JDABuilder.createLight(token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.FORUM_TAGS)
                .setActivity(Activity.playing("booting up"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();

        /* Web Server */
        try {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("37.114.32.239", 1005), 0);

            httpServer.createContext("/kayo/topgg/vote/", new webserver.Topgg());

            httpServer.setExecutor(threadPoolExecutor);
            httpServer.start();
            System.out.println("[HTTP] Server started at " + httpServer.getAddress().getAddress() + ":" + httpServer.getAddress().getPort());

        } catch (Exception err) {
            System.out.println("[HTTP] Failed to start the webserver");
            err.printStackTrace();
        }


        // Events
        jda.addEventListener(new events.Ready());
        jda.addEventListener(new events.GuildJoin());
        jda.addEventListener(new events.GuildRemove());

        // Commands
        jda.addEventListener(new commands.Info());
        jda.addEventListener(new commands.addEmoji());
        jda.addEventListener(new commands.Serverinfo());
        jda.addEventListener(new commands.Userinfo());

        // Functions
        jda.addEventListener(new functions.AutoDelete());
        jda.addEventListener(new functions.AutoReact());
        jda.addEventListener(new functions.JoinRoles());
        jda.addEventListener(new functions.AutoMod());
        jda.addEventListener(new functions.WelcomeMessages());
        jda.addEventListener(new functions.VoteReminder());

    }

    public static JDA getJda() {
        return jda;
    }

}
