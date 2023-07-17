package webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import okhttp3.internal.http.HttpHeaders;
import org.json.JSONObject;
import utils.Emojis;
import utils.KUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.http.HttpRequest;

public class Topgg implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {

            if(!exchange.getRequestMethod().equals("POST")) return;

            String authorization = exchange.getRequestHeaders().get("Authorization").get(0);
            if(!authorization.equals("Kayo 13r87shgbr8wfdsgbf138ew6fgbu3")) return;

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    body.append(line);
                }
            }

            JSONObject jo = new JSONObject(body.toString());

            Guild g = Kayo.Kayo.getJda().getGuildById("1124803921978871850");
            TextChannel ch = g.getTextChannelById("1130518001859440811");

            User user = Kayo.Kayo.getJda().getUserById(jo.getString("user"));
            if(user == null) {
                String description = "Der User " + UserSnowflake.fromId(jo.getString("user")) + " hat für mich gevotet!\nEr votet zum `1.` mal.";
                if(jo.getString("type").equals("upvote")) {
                    if(jo.getBoolean("isWeekend")) {
                        description += "\n\r:star: **Wochenende** :star:\n*Es ist Wochenende, der Vote zählt doppelt!*";
                    }
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(Emojis.vote() + " | **Top.gg Vote**")
                        .setDescription(description)
                        .setColor(0x2f3136)
                        .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                        .setThumbnail(user.getEffectiveAvatarUrl())
                        .setFooter("Danke für die Unterstützung!", Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl());

                Button btn = Button.link("https://top.gg/bot/"+Kayo.Kayo.getJda().getSelfUser().getId()+"/vote", "Voten").withEmoji(Emoji.fromFormatted(Emojis.vote())).withDisabled(false);

                if (!jo.getString("type").equals("upvote")) {
                    embed.setTitle(utils.Emojis.vote() + " | **Top.gg Vote [TEST]**");
                }
                try { ch.sendMessageEmbeds(embed.build()).setActionRow(btn).queue(); } catch(Exception ignored) { }

                String response = "Erfolgreich";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            KUser u = new KUser(user);

            long voteCount = u.getVoteCount();
            u.setVoteCount((voteCount+1), TimeFormat.RELATIVE.now().toInstant().getEpochSecond());

            String description = "Der User " + u.getAsMention() + " [`" + u.getUsername() + "`] hat für mich gevotet!\nEr votet zum `" + (voteCount+1) + ".` mal.";
            if(jo.getString("type").equals("upvote")) {
                if(jo.getBoolean("isWeekend")) {
                    description += "\n\r:star: **Wochenende** :star:\n*Es ist Wochenende, der Vote zählt doppelt!*";
                }
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.vote() + " | **Top.gg Vote**")
                    .setDescription(description)
                    .setColor(0x2f3136)
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .setFooter("Danke für die Unterstützung!", Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl());

            Button btn = Button.link("https://top.gg/bot/"+Kayo.Kayo.getJda().getSelfUser().getId()+"/vote", "Voten").withEmoji(Emoji.fromFormatted(Emojis.vote())).withDisabled(false);

            if (!jo.getString("type").equals("upvote")) {
                embed.setTitle(utils.Emojis.vote() + " | **Top.gg Vote [TEST]**");
            }
            try { ch.sendMessageEmbeds(embed.build()).setActionRow(btn).queue(); } catch(Exception ignored) { }

            String response = "Erfolgreich";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        } catch(Exception err) {
            err.printStackTrace();
            return;
        }

    }
}
