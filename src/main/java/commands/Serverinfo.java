package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.Emojis;
import utils.Get;
import utils.KGuild;
import utils.KUser;

public class Serverinfo extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("serverinfo")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        InteractionHook ih = e.deferReply(false).complete();

        int requiredBoosts = 0;
        if(g.getBoostTier().getKey()==0) requiredBoosts = 2;
        if(g.getBoostTier().getKey()==1) requiredBoosts = 7;
        if(g.getBoostTier().getKey()==2) requiredBoosts = 14;
        if(g.getBoostTier().getKey()==3) requiredBoosts = 14;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.settings()+" | **Server - Informationen**")
                .setDescription("In diesem Menü findest du aktuelle Informationen über den Server!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(g.getIconUrl())

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
        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1).queue();

    }

}
