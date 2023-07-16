package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.util.*;

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

        } else if(e.getSubcommandName().equals("show")) {

            InteractionHook ih = e.deferReply(true).complete();

            Collection<Role> roles = g.getJoinRolesList();
            StringBuilder sb = new StringBuilder();

            long count = 0L;
            Collection<SelectOption> options = new ArrayList();
            roles.forEach(role => {
                count = count+1;
                sb.append("`"+count+".` **|** "+role.getAsMention()+"\n");
                String name = role.getName();
                if(name.length()>20) name = role.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "joinroles.select.show."+role.getId()));
            });

            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.role() + " | **JoinRoles**")
                    .setDescription(sb.toString())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            boolean disabled = false;
            if(options.size() == 0) {
                options.add(SelectOption.of("None", "none.none.none"));
                disabled = true;
            }
            StringSelectMenu sm = StringSelectMenu.create("joinroles.select.show")
                    .addOptions(options)
                    .setDisabled(disabled)
                    .setMinValues(1)
                    .setMaxValues(1)
                    .setPlaceholder("📌 | Wähle eine Rolle zum bearbeiten")
                    .build();
            ih.editOriginalEmbeds(embed.build()).setActionRow(sm).queue();



        }

    }

}
