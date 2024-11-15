package Kayo;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import utils.Config;
import utils.Database;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Kayo {

    private static JDA jda;

    private static Database database;
    public static boolean isReady = false;
    public static String errorMessage = null;
    private static Config config;

    public static void main(String[] args) {
        config = new Config();

        String token = config.get("token").toString();
        jda = JDABuilder.createLight(token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.DEFAULT))
                .enableIntents(
                        GatewayIntent.AUTO_MODERATION_CONFIGURATION, GatewayIntent.AUTO_MODERATION_EXECUTION, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.FORUM_TAGS)
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

        /* Database */
        database = new Database(
                config.get("database.ip").toString(),
                Integer.parseInt(config.get("database.port").toString()),
                config.get("database.database").toString(),
                config.get("database.username").toString(),
                config.get("database.password").toString(),
                Integer.parseInt(config.get("database.maxConnections").toString())
        ).connect();


        // Events
        jda.addEventListener(new events.Ready());
        jda.addEventListener(new events.GuildJoin());
        jda.addEventListener(new events.GuildRemove());

        // Commands
        jda.addEventListener(new commands.Info());
        jda.addEventListener(new commands.addEmoji());
        jda.addEventListener(new commands.Serverinfo());
        jda.addEventListener(new commands.Userinfo());
        jda.addEventListener(new commands.RoleInfo());
        jda.addEventListener(new commands.Ping());

        // Functions
        jda.addEventListener(new functions.AutoDelete());
        jda.addEventListener(new functions.AutoReact());
        jda.addEventListener(new functions.JoinRoles());
        jda.addEventListener(new functions.AutoMod());
        jda.addEventListener(new functions.WelcomeMessages());
        jda.addEventListener(new functions.VoteReminder());
        jda.addEventListener(new functions.Logging());
        jda.addEventListener(new functions.TempChannels());

    }

    public static JDA getJda() {
        return jda;
    }
    public static Database getDatabase() {
        return database;
    }

}
