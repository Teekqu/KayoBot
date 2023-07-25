package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.Emojis;
import utils.Get;
import utils.KGuild;
import utils.KUser;

public class GuildRemove extends ListenerAdapter {

    public void onGuildLeave(GuildLeaveEvent e) {

        KGuild g = new KGuild(e.getGuild());

        int requiredBoosts = 0;
        if(g.getBoostTier().getKey()==0) requiredBoosts = 2;
        if(g.getBoostTier().getKey()==1) requiredBoosts = 7;
        if(g.getBoostTier().getKey()==2) requiredBoosts = 14;
        if(g.getBoostTier().getKey()==3) requiredBoosts = 14;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.join()+" │ **Server verlassen**")
                .setDescription("Ich wurde von einem Server gekickt!")
                .setColor(Get.embedColor(false))
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(e.getGuild().getName(), e.getGuild().getIconUrl())

                .addBlankField(false)
                .addField(Emojis.pen()+" - Name", g.getName(), true)
                .addField(Emojis.idea()+" - ID", g.getId(), true)
                .addField(Emojis.crone()+" - Owner", g.getOwner().getAsMention()+" ("+(new KUser(g.getOwner().getUser()).getUsername())+")", true)
                .addBlankField(false)
                .addField(Emojis.user()+" - User", String.valueOf(g.getUserCount()), true)
                .addField(Emojis.bot()+" - Bots", String.valueOf(g.getBotCount()), true)
                .addField(Emojis.boost()+" - Boosts", g.getBoostCount()+"/"+requiredBoosts+" (Level "+g.getBoostTier().getKey()+")", true)
                .addBlankField(false)
                .addField(Emojis.channel()+" - Kanäle", String.valueOf(g.getChannels().size()), true)
                .addField(Emojis.role()+" - Rollen", String.valueOf(g.getRoles().size()), true)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+g.getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+g.getTimeCreated().toInstant().getEpochSecond()+":R>)", true);

        String url = g.getIconUrl();
        if(url == null) url = "https://i.timo1005.de/Kayo.png";
        Button btn1 = Button.link(url, "Icon URL").withEmoji(Emoji.fromFormatted(Emojis.link())).withDisabled(url.equals("https://i.timo1005.de/Kayo.png"));
        try { Kayo.Kayo.getJda().getTextChannelById("1130521728225910874").sendMessageEmbeds(embed.build()).setActionRow(btn1).queue(); } catch (Exception ignored) { }

        g.getAutoDelete().forEach(map -> {
            g.removeAutoDelete((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });
        g.getAutoReact().forEach(g::removeAutoReact);
        g.getJoinRolesList().forEach(g::removeJoinRole);
        g.getLogging().forEach(map -> {
            g.removeLogging((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });
        g.getWelcomeMessages().forEach(map -> {
            g.removeWelcomeMessage((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });

    }

}
