package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

public class JoinRoles extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("joinrole")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("add")) {

            InteractionHook ih = e.deferReply(true).complete();

            Role r = e.getOption("role").getAsRole();

            if(g.getJoinRolesList().contains(r)) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Die Rolle ist bereits eine JoinRole")).queue();
                return;
            }
            if(r.getPosition() >= e.getJDA().getRoles().get(0).getPosition()) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Die Rolle muss unter Kayo sein")).queue();
                return;
            }

            g.addJoinRole(r, true, true);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Alle neuen User und Bots bekommen nun die Rolle "+r.getAsMention()+" hinzugefügt!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            ih.editOriginalEmbeds(embed.build()).queue();

        }

    }

}
