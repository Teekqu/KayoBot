package commands;

import net.dv8tion.jda.api.EmbedBuilder;
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

public class Info extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("info")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser bot = new KUser(e.getJDA().getSelfUser());
        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(u.isBlocked()) return;

        InteractionHook ih = e.deferReply(false).complete();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.kayo() + " │ "+bot.getName()+" - Informationen")
                .setDescription("In diesem Menü findest du aktuelle Informationen über "+bot.getName()+"!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(bot.getEffectiveAvatarUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())

                .addBlankField(false)
                .addField(Emojis.kayo()+" - Name", bot.getName(), true)
                .addField(Emojis.idea()+" - ID", bot.getId(), true)
                .addField(Emojis.crone()+" - Entwickler", "["+Get.timo().getUsername()+"]("+Get.timo().getProfileUrl()+")\n["+Get.killer().getUsername()+"]("+Get.killer().getProfileUrl()+")", true)
                .addBlankField(false)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+TimeFormat.RELATIVE.atInstant(bot.getTimeCreated().toInstant()).toInstant().getEpochSecond()+">", true)
                .addField(Emojis.code()+" - Libary", "[JDA](https://jda.wiki/)", true)
                .addField(Emojis.wumpus()+" - Server", ""+e.getJDA().getGuilds().size(), true)
                .addBlankField(false)
                .addField(Emojis.user()+" - User", ""+e.getJDA().getUsers().size(), true)
                .addField(Emojis.slash()+" - Commands", ""+e.getJDA().retrieveCommands().complete().size(), true)
                .addField(Emojis.vote()+" - Votes", ""+Get.topggVotes(), true);

        Button btn1 = Button.link(Get.inviteLink(), "Einladen").withEmoji(Emoji.fromFormatted(Emojis.link()));
        Button btn2 = Button.link(Get.topggVoteLink(), "Voten").withEmoji(Emoji.fromFormatted(Emojis.vote()));

        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2).queue();

    }

}
