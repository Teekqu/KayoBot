package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Logging extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("logging")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("add")) {

            InteractionHook ih = e.deferReply(true).complete();

            GuildMessageChannel ch = e.getOption("channel").getAsChannel().asGuildMessageChannel();

            if(g.getLogging(ch)!=null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "In dem Kanal wird bereits geloggt!")).queue();
                return;
            }

            boolean success = g.addLogging(ch, true, true, true, true, true, true, true, true, true);
            if(!success) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler beim hinzufügen")).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Alle Events werden nun im Kanal "+ch.getAsMention()+" geloggt!\nDu kannst die Events unter */logging show* verwalten!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            ih.editOriginalEmbeds(embed.build()).queue();
            return;

        } else if(e.getSubcommandName().equals("show")) {

            InteractionHook ih = e.deferReply(true).complete();
            Collection<HashMap<String, String>> maps = g.getLogging();
            StringBuilder sb = new StringBuilder();
            AtomicInteger count = new AtomicInteger();
            Collection<SelectOption> options = new ArrayList<>();
            maps.forEach(map -> {
                GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId"));
                if(ch != null) {
                    count.getAndIncrement();
                    boolean member = Boolean.parseBoolean(map.get("member"));
                    boolean user = Boolean.parseBoolean(map.get("user"));
                    boolean server = Boolean.parseBoolean(map.get("server"));
                    boolean channel = Boolean.parseBoolean(map.get("channel"));
                    boolean role = Boolean.parseBoolean(map.get("role"));
                    boolean message = Boolean.parseBoolean(map.get("message"));
                    boolean moderation = Boolean.parseBoolean(map.get("moderation"));
                    boolean serverJoinLeave = Boolean.parseBoolean(map.get("serverJoinLeave"));
                    boolean voiceJoinLeave = Boolean.parseBoolean(map.get("voiceJoinLeave"));
                    sb.append("`"+count+".` **|** "+ch.getAsMention()+"\n");
                    String name = ch.getName();
                    if(name.length()>20) name = ch.getName().substring(0, 20)+"...";
                    options.add(SelectOption.of(name, "logging.select.show."+ch.getId()));
                }
            });

            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.archive() + " | **Logging**")
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
            StringSelectMenu sm = StringSelectMenu.create("logging.select.show")
                    .addOptions(options)
                    .setDisabled(disabled)
                    .setMinValues(1)
                    .setMaxValues(1)
                    .setPlaceholder("📌 | Wähle einen Kanal zum bearbeiten")
                    .build();
            ih.editOriginalEmbeds(embed.build()).setActionRow(sm).queue();

        }

    }

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(e.getGuild() == null || e.getMember() == null) return;
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(e.getInteraction().getComponentId().equals("logging.select.show")) {

            if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
                return;
            }

            InteractionHook ih = e.deferEdit().complete();

            GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3]);
            if(ch == null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
                return;
            }

            HashMap<String, String> map = g.getLogging(ch);
            if(map == null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
                return;
            }

            Collection<SelectOption> options = new ArrayList<>();
            Collection<SelectOption> defaultOptions = new ArrayList<>();

            SelectOption member1 = (SelectOption.of("Member Logs", "logging.select.show."+ch.getId()+".member").withEmoji(Emoji.fromFormatted(Emojis.crone())));
            SelectOption user1 = (SelectOption.of("User Logs", "logging.select.show."+ch.getId()+".user").withEmoji(Emoji.fromFormatted(Emojis.user())));
            SelectOption server1 = (SelectOption.of("Server Logs", "logging.select.show."+ch.getId()+".server").withEmoji(Emoji.fromFormatted(Emojis.data())));
            SelectOption channel1 = (SelectOption.of("Channel Logs", "logging.select.show."+ch.getId()+".channel").withEmoji(Emoji.fromFormatted(Emojis.channel())));
            SelectOption role1 = (SelectOption.of("Role Logs", "logging.select.show."+ch.getId()+".role").withEmoji(Emoji.fromFormatted(Emojis.role())));
            SelectOption message1 = (SelectOption.of("Message Logs", "logging.select.show."+ch.getId()+".message").withEmoji(Emoji.fromFormatted(Emojis.pen())));
            SelectOption moderation1 = (SelectOption.of("Moderation Logs", "logging.select.show."+ch.getId()+".moderation").withEmoji(Emoji.fromFormatted(Emojis.timeout())));
            SelectOption serverJoinLeave1 = (SelectOption.of("Join Leave (Server) Logs", "logging.select.show."+ch.getId()+".serverJoinLeave").withEmoji(Emoji.fromFormatted(Emojis.join())));
            SelectOption voiceJoinLeave1 = (SelectOption.of("Join Leave (Voice) Logs", "logging.select.show."+ch.getId()+".voiceJoinLeave").withEmoji(Emoji.fromFormatted(Emojis.leave())));

            options.add(member1);
            options.add(user1);
            options.add(server1);
            options.add(channel1);
            options.add(role1);
            options.add(message1);
            options.add(moderation1);
            options.add(serverJoinLeave1);
            options.add(voiceJoinLeave1);

            String member = Emojis.no();
            String user = Emojis.no();
            String server = Emojis.no();
            String channel = Emojis.no();
            String role = Emojis.no();
            String message = Emojis.no();
            String moderation = Emojis.no();
            String serverJoinLeave = Emojis.no();
            String voiceJoinLeave = Emojis.no();

            if(Boolean.parseBoolean(map.get("member"))) {
                defaultOptions.add(member1);
                member = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("user"))) {
                defaultOptions.add(user1);
                user = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("server"))) {
                defaultOptions.add(server1);
                server = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("channel"))) {
                defaultOptions.add(channel1);
                channel = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("role"))) {
                defaultOptions.add(role1);
                role = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("message"))) {
                defaultOptions.add(message1);
                message = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("moderation"))) {
                defaultOptions.add(moderation1);
                moderation = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("serverJoinLeave"))) {
                defaultOptions.add(serverJoinLeave1);
                serverJoinLeave = Emojis.yes();
            }
            if(Boolean.parseBoolean(map.get("voiceJoinLeave"))) {
                defaultOptions.add(voiceJoinLeave1);
                voiceJoinLeave = Emojis.yes();
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.archive()+" │ **Logging**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())

                    .addBlankField(false)
                    .addField(Emojis.crone()+" - Member Logs", member, true)
                    .addField(Emojis.user()+" - User Logs", user, true)
                    .addBlankField(false)
                    .addField(Emojis.data()+" - Server Logs", server, true)
                    .addField(Emojis.channel()+" - Channel Logs", channel, true)
                    .addField(Emojis.role()+" - Role Logs", role, true)
                    .addField(Emojis.pen()+" - Message Logs", message, true)
                    .addBlankField(false)
                    .addField(Emojis.timeout()+" - Moderation Logs", moderation, true)
                    .addField(Emojis.join()+" - Join Leave (Server) Logs", serverJoinLeave, true)
                    .addField(Emojis.leave()+" - Join Leave (Voice) Logs", voiceJoinLeave, true);

            Button btn = Button.danger("logging.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));

            StringSelectMenu sm = StringSelectMenu.create("logging.select.show."+ch.getId())
                    .addOptions(options)
                    .setDefaultOptions(defaultOptions)
                    .setMinValues(1)
                    .setMaxValues(9)
                    .setPlaceholder("📌 | Bearbeite die Log Aktionen")
                    .build();

            ih.editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(btn), ActionRow.of(sm)).queue();

        } else if(e.getInteraction().getComponentId().startsWith("logging.select.show.")) {

            if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
                return;
            }

            InteractionHook ih = e.deferEdit().complete();

            GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3]);
            if(ch == null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
                return;
            }

            HashMap<String, String> map = g.getLogging(ch);
            if(map == null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
                return;
            }

            Collection<SelectOption> options = new ArrayList<>();
            Collection<SelectOption> defaultOptions = new ArrayList<>();

            SelectOption member1 = (SelectOption.of("Member Logs", "logging.select.show."+ch.getId()+".member").withEmoji(Emoji.fromFormatted(Emojis.crone())));
            SelectOption user1 = (SelectOption.of("User Logs", "logging.select.show."+ch.getId()+".user").withEmoji(Emoji.fromFormatted(Emojis.user())));
            SelectOption server1 = (SelectOption.of("Server Logs", "logging.select.show."+ch.getId()+".server").withEmoji(Emoji.fromFormatted(Emojis.data())));
            SelectOption channel1 = (SelectOption.of("Channel Logs", "logging.select.show."+ch.getId()+".channel").withEmoji(Emoji.fromFormatted(Emojis.channel())));
            SelectOption role1 = (SelectOption.of("Role Logs", "logging.select.show."+ch.getId()+".role").withEmoji(Emoji.fromFormatted(Emojis.role())));
            SelectOption message1 = (SelectOption.of("Message Logs", "logging.select.show."+ch.getId()+".message").withEmoji(Emoji.fromFormatted(Emojis.pen())));
            SelectOption moderation1 = (SelectOption.of("Moderation Logs", "logging.select.show."+ch.getId()+".moderation").withEmoji(Emoji.fromFormatted(Emojis.timeout())));
            SelectOption serverJoinLeave1 = (SelectOption.of("Join Leave (Server) Logs", "logging.select.show."+ch.getId()+".serverJoinLeave").withEmoji(Emoji.fromFormatted(Emojis.join())));
            SelectOption voiceJoinLeave1 = (SelectOption.of("Join Leave (Voice) Logs", "logging.select.show."+ch.getId()+".voiceJoinLeave").withEmoji(Emoji.fromFormatted(Emojis.leave())));

            options.add(member1);
            options.add(user1);
            options.add(server1);
            options.add(channel1);
            options.add(role1);
            options.add(message1);
            options.add(moderation1);
            options.add(serverJoinLeave1);
            options.add(voiceJoinLeave1);

            String member = Emojis.no();
            String user = Emojis.no();
            String server = Emojis.no();
            String channel = Emojis.no();
            String role = Emojis.no();
            String message = Emojis.no();
            String moderation = Emojis.no();
            String serverJoinLeave = Emojis.no();
            String voiceJoinLeave = Emojis.no();
            boolean member2 = false;
            boolean user2 = false;
            boolean server2 = false;
            boolean channel2 = false;
            boolean role2 = false;
            boolean message2 = false;
            boolean moderation2 = false;
            boolean serverJoinLeave2 = false;
            boolean voiceJoinLeave2 = false;

            Collection<String> values = new ArrayList<>();
            e.getInteraction().getSelectedOptions().forEach(option -> {
                values.add(option.getValue().split("\\.")[4]);
            });

            if(values.contains("member")) {
                defaultOptions.add(member1);
                member = Emojis.yes();
                member2 = true;
            }
            if(values.contains("user")) {
                defaultOptions.add(user1);
                user = Emojis.yes();
                user2 = true;
            }
            if(values.contains("server")) {
                defaultOptions.add(server1);
                server = Emojis.yes();
                server2 = true;
            }
            if(values.contains("channel")) {
                defaultOptions.add(channel1);
                channel = Emojis.yes();
                channel2 = true;
            }
            if(values.contains("role")) {
                defaultOptions.add(role1);
                role = Emojis.yes();
                role2 = true;
            }
            if(values.contains("message")) {
                defaultOptions.add(message1);
                message = Emojis.yes();
                message2 = true;
            }
            if(values.contains("moderation")) {
                defaultOptions.add(moderation1);
                moderation = Emojis.yes();
                moderation2 = true;
            }
            if(values.contains("serverJoinLeave")) {
                defaultOptions.add(serverJoinLeave1);
                serverJoinLeave = Emojis.yes();
                serverJoinLeave2 = true;
            }
            if(values.contains("voiceJoinLeave")) {
                defaultOptions.add(voiceJoinLeave1);
                voiceJoinLeave = Emojis.yes();
                voiceJoinLeave2 = true;
            }

            boolean success = g.editLogging(ch, member2, user2, server2, channel2, role2, message2, moderation2, serverJoinLeave2, voiceJoinLeave2);
            if(!success) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.archive()+" │ **Logging**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())

                    .addBlankField(false)
                    .addField(Emojis.crone()+" - Member Logs", member, true)
                    .addField(Emojis.user()+" - User Logs", user, true)
                    .addBlankField(false)
                    .addField(Emojis.data()+" - Server Logs", server, true)
                    .addField(Emojis.channel()+" - Channel Logs", channel, true)
                    .addField(Emojis.role()+" - Role Logs", role, true)
                    .addField(Emojis.pen()+" - Message Logs", message, true)
                    .addBlankField(false)
                    .addField(Emojis.timeout()+" - Moderation Logs", moderation, true)
                    .addField(Emojis.join()+" - Join Leave (Server) Logs", serverJoinLeave, true)
                    .addField(Emojis.leave()+" - Join Leave (Voice) Logs", voiceJoinLeave, true);

            Button btn = Button.danger("logging.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));

            StringSelectMenu sm = StringSelectMenu.create("logging.select.show."+ch.getId())
                    .addOptions(options)
                    .setDefaultOptions(defaultOptions)
                    .setMinValues(1)
                    .setMaxValues(9)
                    .setPlaceholder("📌 | Bearbeite die Log Aktionen")
                    .build();

            ih.editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(btn), ActionRow.of(sm)).queue();

        }

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("logging.btn.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(e.getButton().getId().split("\\.")[3]);
        String action = e.getButton().getId().split("\\.")[4];

        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
            return;
        }

        HashMap<String, String> map = g.getLogging(ch);
        if(map == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
            return;
        }

        if(action.equals("delete")) {
            g.removeLogging(ch);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Im Kanal "+ch.getAsMention()+" werden nun keine Aktionen mehr geloggt!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());

            Button btn = Button.danger("logging.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete())).withDisabled(true);
            StringSelectMenu sm = StringSelectMenu.create("logging.select.show."+ch.getId())
                    .addOptions(SelectOption.of("None", "none.none.none"))
                    .setMinValues(1)
                    .setMaxValues(9)
                    .setPlaceholder("📌 | Bearbeite die Log Aktionen")
                    .setDisabled(true)
                    .build();
            ih.editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(btn), ActionRow.of(sm)).queue();
        }

    }

}
