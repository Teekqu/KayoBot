package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class RoleInfo extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("roleinfo")) return;
        if(!e.isFromGuild() || e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        Role r = e.getOption("role").getAsRole();

        InteractionHook ih = e.deferReply(true).complete();

        int count = e.getGuild().getMembersWithRoles(r).size();
        List<Permission> perms = new java.util.ArrayList<>(r.getPermissions().stream().toList());
        StringBuilder permissions = new StringBuilder();
        for(Permission perm : perms) permissions.insert(0, perm.isGuild() ? perm.getName() + ", " : "");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.role()+" | **Role Info**")
                .setDescription("**Rolle:** "+r.getAsMention()+" ["+r.getName()+"]")
                .setColor(r.getColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail((r.getIcon() == null ? g.getIconUrl() : r.getIcon().getIconUrl()))
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())

                .addBlankField(false)
                .addField(Emojis.pen()+" - Name", r.getName(), true)
                .addField(Emojis.idea()+" - ID", r.getId(), true)
                .addField(Emojis.stats()+" - Color", (r.getColor() == null ? "#000000" : Convert.colorToHex(r.getColor())), true)
                .addBlankField(false)
                .addField(Emojis.user()+" - Usercount", count+"/"+e.getGuild().getMembers().size(), true)
                .addField(Emojis.pin()+" - Hoisted", r.isHoisted() ? Emojis.yes() : Emojis.no(), true)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+r.getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+r.getTimeCreated().toInstant().getEpochSecond()+":R>)", true)
                .addBlankField(false)
                .addField(Emojis.crone()+" - Permissions", "```"+permissions.toString().substring(0, permissions.toString().length()-2)+"```", true);

        String url = (r.getIcon() == null ? "https://i.timo1005.de/Kayo.png" : r.getIcon().getIconUrl());
        Button btn1 = Button.link(url, "Icon").withEmoji(Emoji.fromFormatted(Emojis.link())).withDisabled(url.equals("https://i.timo1005.de/Kayo.png"));
        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1).queue();

    }

}
