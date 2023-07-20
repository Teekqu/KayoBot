package utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;

public class Embeds {

    public static MessageEmbed error(KGuild guild, KUser user, String error) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.no() + " │ Es ist ein Fehler vorgefallen!")
                .setDescription("**Server:** "+guild.getName()+"\n**User:** "+user.getAsMention()+" [`"+user.getUsername()+"`]\n\r**Fehler:** ```java\n"+error+"```")
                .setColor(Get.embedColor(false))
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail("https://i.timo1005.de/loading.gif")
                .setAuthor(Kayo.Kayo.getJda().getSelfUser().getName(), null, Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl())
                .setFooter(user.getUsername(), user.getEffectiveAvatarUrl());
        return embed.build();

    }

    public static MessageEmbed error(KUser user, String error) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.no() + " │ Es ist ein Fehler vorgefallen!")
                .setDescription("**Server:** None\n**User:** "+user.getAsMention()+" [`"+user.getUsername()+"`]\n\r**Fehler:** ```java\n"+error+"```")
                .setColor(Get.embedColor(false))
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail("https://i.timo1005.de/loading.gif")
                .setAuthor(Kayo.Kayo.getJda().getSelfUser().getName(), null, Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl())
                .setFooter(user.getUsername(), user.getEffectiveAvatarUrl());
        return embed.build();

    }

}
