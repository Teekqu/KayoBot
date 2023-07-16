package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.util.*;

public class JoinRoles extends ListenerAdapter {

    private Collection<Long> activeUsers = new ArrayList<>();

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("joinrole") && !e.getName().equals("joinroles")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getName().equals("joinrole") && e.getSubcommandName().equals("add")) {

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

        } else if(e.getName().equals("joinroles")) {

            InteractionHook ih = e.deferReply(true).complete();

            Collection<Role> roles = g.getJoinRolesList();
            StringBuilder sb = new StringBuilder();

            long count = 0L;
            Collection<SelectOption> options = new ArrayList();
            for(Role r : roles) {
                count = count+1;
                sb.append("`"+count+".` **|** "+r.getAsMention()+"\n");
                String name = r.getName();
                if(name.length()>20) name = r.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "joinroles.select.show."+r.getId()));
            }

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

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("joinroles.select.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        String id = e.getInteraction().getSelectedOptions().get(0).getValue().substring(22);
        Role r = e.getGuild().getRoleById(id);
        if(r == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültige Rolle")).queue();
            return;
        }

        HashMap<String, String> map = g.getJoinRole(r);
        if(map == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültige JoinRole")).queue();
            return;
        }

        String addUser = Emojis.no();
        if(map.get("addUser").toLowerCase().equals("true")) addUser = Emojis.yes();

        String addBot = Emojis.no();
        if(map.get("addBot").toLowerCase().equals("true")) addBot = Emojis.yes();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.role()+" | **JoinRoles - Role Info**")
                .setDescription("**Rolle:** "+r.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.user()+" - User hinzufügen", addUser, true)
                .addField(Emojis.bot()+" - Bot hinzufügen", addBot, true);

        Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
        Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        if(addUser.equals(Emojis.yes())) btn1 = Button.success("joinroles.btn.show."+r.getId()+".disableAddUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        if(addBot.equals(Emojis.yes())) btn2 = Button.success("joinroles.btn.show."+r.getId()+".disableAddBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("joinroles.btn.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(this.activeUsers.contains(u.getIdLong())) e.deferEdit().queue();
        this.activeUsers.add(u.getIdLong());
        if(!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            this.activeUsers.remove(u.getIdLong());
            return;
        }
        Role r = e.getGuild().getRoleById(e.getButton().getId().split("\\.")[3]);
        if(r == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültige Rolle")).setEphemeral(true).queue();
            this.activeUsers.remove(u.getIdLong());
            return;
        }

        HashMap<String, String> map = g.getJoinRole(r);
        if(map == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültige JoinRole")).setEphemeral(true).queue();
            this.activeUsers.remove(u.getIdLong());
            return;
        }

        String id = e.getButton().getId().split("\\.")[4];

        if(id.equals("delete")) {
            if(g.getJoinRole(r)==null) {
                e.replyEmbeds(Embeds.error(g, u, "Die Rolle ist keine JoinRole")).setEphemeral(true).queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }
            g.removeJoinRole(r);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Die Rolle "+r.getAsMention()+" ist nun keine JoinRole mehr!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user())).withDisabled(true);
            Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot())).withDisabled(true);
            Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete())).withDisabled(true);
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
            this.activeUsers.remove(u.getIdLong());
        } else if(id.equals("enableAddUser")) {
            if(map.get("addUser").toLowerCase().equals("true")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editJoinRole(r, true, map.get("addBot").toLowerCase().equals("true"));

            String addUser = Emojis.yes();

            String addBot = Emojis.no();
            if(map.get("addBot").toLowerCase().equals("true")) addBot = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.role()+" | **JoinRoles - Role Info**")
                    .setDescription("**Rolle:** "+r.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.user()+" - User hinzufügen", addUser, true)
                    .addField(Emojis.bot()+" - Bot hinzufügen", addBot, true);

            Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(addUser.equals(Emojis.yes())) btn1 = Button.success("joinroles.btn.show."+r.getId()+".disableAddUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(addBot.equals(Emojis.yes())) btn2 = Button.success("joinroles.btn.show."+r.getId()+".disableAddBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
            this.activeUsers.remove(u.getIdLong());

        } else if(id.equals("enableAddBot")) {
            if(map.get("addBot").toLowerCase().equals("true")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editJoinRole(r, map.get("addUser").toLowerCase().equals("true"), true);

            String addBot = Emojis.yes();

            String addUser = Emojis.no();
            if(map.get("addUser").toLowerCase().equals("true")) addUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.role()+" | **JoinRoles - Role Info**")
                    .setDescription("**Rolle:** "+r.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.user()+" - User hinzufügen", addUser, true)
                    .addField(Emojis.bot()+" - Bot hinzufügen", addBot, true);

            Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(addUser.equals(Emojis.yes())) btn1 = Button.success("joinroles.btn.show."+r.getId()+".disableAddUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(addBot.equals(Emojis.yes())) btn2 = Button.success("joinroles.btn.show."+r.getId()+".disableAddBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
            this.activeUsers.remove(u.getIdLong());
        } else if(id.equals("disableAddUser")) {
            if(map.get("addUser").toLowerCase().equals("false")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editJoinRole(r, false, map.get("addBot").toLowerCase().equals("true"));

            String addUser = Emojis.no();

            String addBot = Emojis.no();
            if(map.get("addBot").toLowerCase().equals("true")) addBot = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.role()+" | **JoinRoles - Role Info**")
                    .setDescription("**Rolle:** "+r.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.user()+" - User hinzufügen", addUser, true)
                    .addField(Emojis.bot()+" - Bot hinzufügen", addBot, true);

            Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(addUser.equals(Emojis.yes())) btn1 = Button.success("joinroles.btn.show."+r.getId()+".disableAddUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(addBot.equals(Emojis.yes())) btn2 = Button.success("joinroles.btn.show."+r.getId()+".disableAddBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
            this.activeUsers.remove(u.getIdLong());
        } else if(id.equals("disableAddBot")) {
            if(map.get("addBot").toLowerCase().equals("false")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editJoinRole(r, map.get("addUser").toLowerCase().equals("true"), false);

            String addBot = Emojis.no();

            String addUser = Emojis.no();
            if(map.get("addUser").toLowerCase().equals("true")) addUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.role()+" | **JoinRoles - Role Info**")
                    .setDescription("**Rolle:** "+r.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.user()+" - User hinzufügen", addUser, true)
                    .addField(Emojis.bot()+" - Bot hinzufügen", addBot, true);

            Button btn1 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn2 = Button.danger("joinroles.btn.show."+r.getId()+".enableAddBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn3 = Button.danger("joinroles.btn.show."+r.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(addUser.equals(Emojis.yes())) btn1 = Button.success("joinroles.btn.show."+r.getId()+".disableAddUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(addBot.equals(Emojis.yes())) btn2 = Button.success("joinroles.btn.show."+r.getId()+".disableAddBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
            this.activeUsers.remove(u.getIdLong());
        }

    }

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(g.getJoinRoles()==null || g.getJoinRoles().size()==0) return;

        for(HashMap<String, String> map : g.getJoinRoles()) {

            Role r = e.getGuild().getRoleById(map.get("roleId"));
            if(r == null) continue;
            boolean addUser = map.get("addUser").toLowerCase().equals("true");
            boolean addBot = map.get("addBot").toLowerCase().equals("true");

            if(u.isBot() && addBot) try { e.getGuild().addRoleToMember(UserSnowflake.fromId(u.getId()), r).reason("JoinRole by Kayo").queue(); } catch (Exception ignored) { }
            if(!u.isBot() && addUser) try { e.getGuild().addRoleToMember(UserSnowflake.fromId(u.getId()), r).reason("JoinRole by Kayo").queue(); } catch (Exception ignored) { }

        }

    }

}
