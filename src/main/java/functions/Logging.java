package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.awt.*;
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
                    .setTitle(Emojis.archive() + " │ **Logging**")
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
                    .addField( "User Logs", user, true)
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
                    .addField( "User Logs", user, true)
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


    /* Logging Events */
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "member");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle("**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat seinen Nicknamen geändert")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField("User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField("Vorher", e.getOldNickname(), true)
                .addField("Nachher", e.getNewNickname(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildMemberUpdateAvatar(GuildMemberUpdateAvatarEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "member");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle("**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat sein Avatar geändert")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField("User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField("Vorher", "[klick]("+e.getOldAvatarUrl()+")", true)
                .addField("Nachher", "[klick]("+e.getNewAvatarUrl()+")", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "member");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle("**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat eine neue Rolle bekommen")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField("User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField("Rolle", e.getRoles().get(0).getAsMention()+" ("+e.getRoles().get(0).getName()+")", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "member");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle("**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat eine Rolle entfernt bekommen")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField( "User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField( "Rolle", e.getRoles().get(0).getAsMention()+" ("+e.getRoles().get(0).getName()+")", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onUserUpdateName(UserUpdateNameEvent e) {
        KUser u = new KUser(e.getUser());
        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat seinen Namen geändert")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField( "User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField( "Vorher", e.getOldName(), true)
                .addField( "Nachher", e.getNewName(), true);

        Collection<Guild> guilds = e.getUser().getMutualGuilds();
        guilds.forEach(g -> {
            KGuild g1 = new KGuild(g);
            Collection<GuildMessageChannel> channels = this.getLoggingChannel(g1, "user");
            channels.forEach(ch -> {
                this.sendWebhookInChannel(ch, embed);
            });
        });

    }
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent e) {
        KUser u = new KUser(e.getUser());

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat seinen Username geändert")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField( "User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField( "Vorher", e.getOldGlobalName(), true)
                .addField( "Nachher", e.getNewGlobalName(), true);

        Collection<Guild> guilds = e.getUser().getMutualGuilds();
        guilds.forEach(g -> {
            KGuild g1 = new KGuild(g);
            Collection<GuildMessageChannel> channels = this.getLoggingChannel(g1, "user");
            channels.forEach(ch -> {
                this.sendWebhookInChannel(ch, embed);
            });
        });

    }
    public void onUserUpdateAvatar(UserUpdateAvatarEvent e) {
        KUser u = new KUser(e.getUser());

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat sein Avatar geändert")
                .setColor(Get.embedColor())
                .setThumbnail(u.getEffectiveAvatarUrl())

                .addField( "User", u.getAsMention()+" ("+u.getUsername()+")", true)
                .addField( "Vorher", "[klick]("+e.getOldAvatarUrl()+")", true)
                .addField( "Nachher", "[klick]("+e.getNewAvatarUrl()+")", true);

        Collection<Guild> guilds = e.getUser().getMutualGuilds();
        guilds.forEach(g -> {
            KGuild g1 = new KGuild(g);
            Collection<GuildMessageChannel> channels = this.getLoggingChannel(g1, "user");
            channels.forEach(ch -> {
                this.sendWebhookInChannel(ch, embed);
            });
        });

    }

    public void onGuildUpdateAfkChannel(GuildUpdateAfkChannelEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        String oldAfkChannel = "None";
        if(e.getOldAfkChannel() != null) oldAfkChannel = e.getOldAfkChannel().getAsMention();
        String newAfkChannel = "None";
        if(e.getNewAfkChannel() != null) newAfkChannel = e.getNewAfkChannel().getAsMention();

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild AFK Channel wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldAfkChannel, true)
                .addField( "Nachher", newAfkChannel, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateAfkTimeout(GuildUpdateAfkTimeoutEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild AFK Timeout wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldAfkTimeout().getSeconds()+" Sekunden", true)
                .addField( "Nachher", e.getNewAfkTimeout().getSeconds()+" Sekunden", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateBanner(GuildUpdateBannerEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Das Guild Banner wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", "[klick]("+e.getOldBannerUrl()+")", true)
                .addField( "Nachher", "[klick]("+e.getNewBannerUrl()+")", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateDescription(GuildUpdateDescriptionEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Die Guild Beschreibung wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldDescription(), true)
                .addField( "Nachher", e.getNewDescription(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateIcon(GuildUpdateIconEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Das Guild Icon wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", "[klick]("+e.getOldIconUrl()+")", true)
                .addField( "Nachher", "[klick]("+e.getNewIconUrl()+")", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateName(GuildUpdateNameEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild Name wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldName(), true)
                .addField( "Nachher", e.getNewName(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateOwner(GuildUpdateOwnerEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        String oldOwner = "None";
        String newOwner = "None";
        if(e.getOldOwner() != null) oldOwner = e.getOldOwner().getAsMention()+" ("+e.getOldOwner().getUser().getName()+")";
        if(e.getNewOwner() != null) newOwner = e.getNewOwner().getAsMention()+" ("+e.getNewOwner().getUser().getName()+")";

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild Owner wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldOwner, true)
                .addField( "Nachher", newOwner, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateRulesChannel(GuildUpdateRulesChannelEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        String oldChannel = "None";
        String newChannel = "None";
        if(e.getOldRulesChannel() != null) oldChannel = e.getOldRulesChannel().getAsMention();
        if(e.getNewRulesChannel() != null) newChannel = e.getNewRulesChannel().getAsMention();

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild Regel Channel wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldChannel, true)
                .addField( "Nachher", newChannel, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateSystemChannel(GuildUpdateSystemChannelEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        String oldChannel = "None";
        String newChannel = "None";
        if(e.getOldSystemChannel() != null) oldChannel = e.getOldSystemChannel().getAsMention();
        if(e.getNewSystemChannel() != null) newChannel = e.getNewSystemChannel().getAsMention();

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Guild Regel Channel wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldChannel, true)
                .addField( "Nachher", newChannel, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildUpdateVanityCode(GuildUpdateVanityCodeEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "server");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Die Vanity URL wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldVanityUrl(), true)
                .addField( "Nachher", e.getNewVanityUrl(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onChannelCreate(ChannelCreateEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Kanal wurde erstellt")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Kanal", e.getChannel().getAsMention(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelDelete(ChannelDeleteEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Kanal wurde gelöscht")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Kanal", e.getChannel().getAsMention(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateName(ChannelUpdateNameEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Kanal Name wurde bearbeitet")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldValue(), true)
                .addField( "Nachher", e.getNewValue(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateNSFW(ChannelUpdateNSFWEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Kanal NSFW Stufe wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldValue().toString(), true)
                .addField( "Nachher", e.getNewValue().toString(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateParent(ChannelUpdateParentEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        String oldChannel = "None";
        String newChannel = "None";
        if(e.getOldValue() != null) oldChannel = e.getOldValue().getName();
        if(e.getNewValue() != null) newChannel = e.getNewValue().getName();

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Kanal wurde in eine neue Kategorie verschoben")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldChannel, true)
                .addField( "Nachher", newChannel, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateSlowmode(ChannelUpdateSlowmodeEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        String before = "0 Sekunden";
        String after = "0 Sekunden";
        if(e.getOldValue() != null) before = e.getOldValue()+" Sekunden";
        if(e.getNewValue() != null) after = e.getNewValue()+" Sekunden";

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Der Slowmode eines Kanals wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", before, true)
                .addField( "Nachher", after, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateTopic(ChannelUpdateTopicEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Kanal Beschreibung wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldValue(), true)
                .addField( "Nachher", e.getNewValue(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateType(ChannelUpdateTypeEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Kanal Typ wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldValue().name(), true)
                .addField( "Nachher", e.getNewValue().name(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onChannelUpdateUserLimit(ChannelUpdateUserLimitEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "channel");

        String oldLimit = "0";
        String newLimit = "0";
        if(e.getOldValue() != null) oldLimit = String.valueOf(e.getOldValue());
        if(e.getNewValue() != null) newLimit = String.valueOf(e.getNewValue());

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Kanal Userlimit wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldLimit, true)
                .addField( "Nachher", newLimit, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onRoleCreate(RoleCreateEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Rolle wurde erstellt")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Rolle", e.getRole().getAsMention(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleDelete(RoleDeleteEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Rolle wurde gelöscht")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Rolle", e.getRole().getName(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleUpdateColor(RoleUpdateColorEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        String oldColor = "None";
        String newColor = "None";
        if(e.getOldColor() != null) oldColor = this.convertColorToHex(e.getOldColor());
        if(e.getNewColor() != null) newColor = this.convertColorToHex(e.getNewColor());

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Rollen Farbe wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldColor, true)
                .addField( "Nachher", newColor, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleUpdateHoisted(RoleUpdateHoistedEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Rollenanzeige wurde geändert (hoisted)")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldValue().toString(), true)
                .addField( "Nachher", e.getNewValue().toString(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleUpdateIcon(RoleUpdateIconEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        String oldIconUrl = "None";
        String newIconUrl = "None";
        if(e.getOldIcon() != null) oldIconUrl = "[klick]("+e.getOldIcon().getIconUrl()+")";
        if(e.getNewIcon() != null) newIconUrl = "[klick]("+e.getNewIcon().getIconUrl()+")";

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Rollen Icon wurde geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldIconUrl, true)
                .addField( "Nachher", newIconUrl, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleUpdateName(RoleUpdateNameEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein Rollen Name wurde bearbeitet")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", e.getOldName(), true)
                .addField( "Nachher", e.getNewName(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "role");

        StringBuilder oldPerms = new StringBuilder();
        StringBuilder newPerms = new StringBuilder();

        e.getOldPermissions().forEach(perm -> {
            oldPerms.append(perm.getName()).append(" ");
        });
        e.getNewPermissions().forEach(perm -> {
            newPerms.append(perm.getName()).append(" ");
        });
        if(oldPerms.toString().equals("")) oldPerms.append("-");
        if(newPerms.toString().equals("")) newPerms.append("-");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Die Rechte einer Rolle wurden geändert")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Server", g.getName(), true)
                .addField( "Vorher", oldPerms.toString(), true)
                .addField( "Nachher", newPerms.toString(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onMessageUpdate(MessageUpdateEvent e) {
        if(!e.isFromGuild()) return;
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "message");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Nachricht wurde bearbeitet")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Nachricht", "[klick]("+e.getMessage().getJumpUrl()+")", true)
                .addField( "Vorher", "Unknown", true)
                .addField( "Nachher", e.getMessage().getContentRaw(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onMessageDelete(MessageDeleteEvent e) {
        if(!e.isFromGuild()) return;
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "message");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Eine Nachricht wurde gelöscht")
                .setColor(Get.embedColor())
                .setThumbnail(g.getIconUrl())

                .addField( "Channel", e.getChannel().getAsMention(), true)
                .addField( "Message ID", e.getMessageId(), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onGuildAuditLogEntryCreate(GuildAuditLogEntryCreateEvent e) {
        AuditLogEntry entry = e.getEntry();
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "moderation");

        if(entry.getType().equals(ActionType.BAN)) {
            Webhook.EmbedObject embed = new Webhook.EmbedObject()
                    .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                    .setDescription("Ein User wurde gebannt")
                    .setColor(Get.embedColor())
                    .setThumbnail(g.getIconUrl())

                    .addField( "User", UserSnowflake.fromId(entry.getTargetId()).getAsMention(), true)
                    .addField(Emojis.supportman()+" - Moderator", entry.getUser().getAsMention()+" ("+entry.getUser().getName()+")", true)
                    .addField(Emojis.pen()+" - Grund", entry.getReason(), true);

            channels.forEach(ch -> {
                this.sendWebhookInChannel(ch, embed);
            });
        } else if(entry.getType().equals(ActionType.KICK)) {
            Webhook.EmbedObject embed = new Webhook.EmbedObject()
                    .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                    .setDescription("Ein User wurde gekickt")
                    .setColor(Get.embedColor())
                    .setThumbnail(g.getIconUrl())

                    .addField( "User", UserSnowflake.fromId(entry.getTargetId()).getAsMention(), true)
                    .addField(Emojis.supportman()+" - Moderator", entry.getUser().getAsMention()+" ("+entry.getUser().getName()+")", true)
                    .addField(Emojis.pen()+" - Grund", entry.getReason(), true);

            channels.forEach(ch -> {
                this.sendWebhookInChannel(ch, embed);
            });
        }

    }

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "serverJoinLeave");

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User ist dem Server beigetreten")
                .setColor(Get.embedColor())
                .setThumbnail(e.getMember().getEffectiveAvatarUrl())

                .addField( "User", e.getMember().getAsMention()+" ("+e.getUser().getName()+")", true)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+e.getUser().getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+e.getUser().getTimeCreated().toInstant().getEpochSecond()+":R>", true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "serverJoinLeave");

        long seconds = (TimeFormat.RELATIVE.now().toInstant().getEpochSecond()-e.getMember().getTimeJoined().toInstant().getEpochSecond());

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat den Server verlassen")
                .setColor(Get.embedColor())
                .setThumbnail(e.getMember().getEffectiveAvatarUrl())

                .addField( "User", e.getMember().getAsMention()+" ("+e.getUser().getName()+")", true)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+e.getUser().getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+e.getUser().getTimeCreated().toInstant().getEpochSecond()+":R>", true)
                .addField(Emojis.stats()+" - Zeit auf dem Server", this.convertSecondsInFormat(seconds), true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        KGuild g = new KGuild(e.getGuild());
        Collection<GuildMessageChannel> channels = this.getLoggingChannel(g, "voiceJoinLeave");

        String oldVoice = "None";
        String newVoice = "None";
        if(e.getOldValue() != null) oldVoice = e.getOldValue().getAsMention();
        if(e.getNewValue() != null) newVoice = e.getNewValue().getAsMention();

        Webhook.EmbedObject embed = new Webhook.EmbedObject()
                .setTitle( "**"+Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging**")
                .setDescription("Ein User hat den Voice gewechselt")
                .setColor(Get.embedColor())
                .setThumbnail(e.getMember().getEffectiveAvatarUrl())

                .addField( "User", e.getMember().getAsMention()+" ("+e.getMember().getUser().getName()+")", true)
                .addField( "Vorher", oldVoice, true)
                .addField( "Nachher", newVoice, true);

        channels.forEach(ch -> {
            this.sendWebhookInChannel(ch, embed);
        });

    }

    private Collection<GuildMessageChannel> getLoggingChannel(KGuild g, String name) {
        if(!Kayo.Kayo.isReady) return new ArrayList<>();

        Collection<GuildMessageChannel> channels = new ArrayList<>();
        g.getLogging().forEach(map -> {
            if(Boolean.parseBoolean(map.get(name))) {
                GuildMessageChannel ch = (GuildMessageChannel) g.getGuild().getGuildChannelById(map.get("channelId"));
                if(ch != null) channels.add(ch);
            }
        });
        return channels;

    }
    private void sendWebhookInChannel(GuildMessageChannel ch, Webhook.EmbedObject embed) {
        if(!Kayo.Kayo.isReady) return;

        boolean check = false;
        for(net.dv8tion.jda.api.entities.Webhook wh : ch.getGuild().retrieveWebhooks().complete()) {
            if(wh.getChannel().asGuildMessageChannel().equals(ch) || wh.getChannel().equals(ch)) {
                try {
                    String url = wh.getUrl();
                    Webhook webhook = new Webhook(url);
                    webhook.addEmbed(embed);
                    webhook.setUsername(Kayo.Kayo.getJda().getSelfUser().getName() + " - Logging");
                    webhook.setAvatarUrl(Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl());
                    webhook.execute();
                    check = true;
                    break;
                } catch (Exception ignored) { }
            }
        }
        if(!check) {
            try {
                net.dv8tion.jda.api.entities.Webhook wh = ((TextChannel) ch).createWebhook(Kayo.Kayo.getJda().getSelfUser().getName()+" - Logging").complete();
                String url = wh.getUrl();
                Webhook webhook = new Webhook(url);
                webhook.addEmbed(embed);
                webhook.setUsername(Kayo.Kayo.getJda().getSelfUser().getName() + " - Logging");
                webhook.setAvatarUrl(Kayo.Kayo.getJda().getSelfUser().getEffectiveAvatarUrl());
                webhook.execute();
            } catch (Exception ignored) { }
        }

    }

    private String convertColorToHex(Color color) {
        return "#"+Integer.toHexString(color.getRGB()).substring(2);
    }

    private String convertSecondsInFormat(Long seconds) {

        long days = seconds / (60 * 60 * 24);
        long remainder = seconds % (60 * 60 * 24);

        long hours = remainder / (60 * 60);
        remainder = remainder % (60 * 60);

        long mins = remainder / (60);
        remainder = remainder % (60);

        long secs = remainder;

        String strDaysHrsMinsSecs = "";

        if (days > 0) {
            strDaysHrsMinsSecs += days + "d ";
        }

        if (hours > 0) {
            strDaysHrsMinsSecs += hours + "h ";
        } else {
            strDaysHrsMinsSecs += "0h ";
        }

        if (mins > 0) {
            strDaysHrsMinsSecs += mins + "m ";
        } else {
            strDaysHrsMinsSecs += "0m ";
        }

        strDaysHrsMinsSecs += secs + "s";

        return strDaysHrsMinsSecs;

    }

}
