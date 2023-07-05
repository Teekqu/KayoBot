package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

public class addEmoji extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("add-emoji")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser bot = new KUser(e.getJDA().getSelfUser());
        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(u.isBlocked()) return;

        if(!e.getMember().hasPermission(Permission.MANAGE_EMOJIS_AND_STICKERS)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferReply(true).complete();

        String emoji1 = e.getOption("emoji").getAsString();
        CustomEmoji emoji;
        try {
            emoji = Emoji.fromFormatted(emoji1).asCustom();
        } catch (Exception err) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiges Emoji")).queue();
            return;
        }
        if(emoji.getName().equals(emoji.getFormatted())) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiges Emoji")).queue();
            return;
        }

        String name = emoji.getName();
        if(e.getOption("name") != null) name = e.getOption("name").getAsString();

        try {
            RichCustomEmoji newEmoji = e.getGuild().createEmoji(name, Icon.from(emoji.getImage().download().get())).complete();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Das Emoji "+newEmoji.getAsMention()+" wurde Erfolgreich mit dem Namen **"+newEmoji.getName()+"** zum Server hinzugefügt!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());

            ih.editOriginalEmbeds(embed.build()).queue();

        } catch (Exception err) {
            ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
            return;
        }

    }

}
