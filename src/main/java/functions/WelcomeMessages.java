package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WelcomeMessages extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("welcome")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("add")) {

            InteractionHook ih = e.deferReply(true).complete();

            if(g.getWelcomeMessages().size() >= Get.limit("welcomeMessages", g.hasPremium())) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Server hat das Limit erreicht")).queue();
                return;
            }
            GuildMessageChannel ch = e.getOption("channel").getAsChannel().asGuildMessageChannel();
            if(g.getWelcomeMessage(ch)!=null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Channel hat bereits eine Welcome Message konfiguriert")).queue();
                return;
            }

            String defaultMessage = Emojis.wave()+" | **Herzlich Willkommen %MENTION%!**";
            g.addWelcomeMessage(ch, defaultMessage, true, true);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Alle neuen User werden nun im Kanal "+ch.getAsMention()+" begrüßt!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            ih.editOriginalEmbeds(embed.build()).queue();

        } else if(e.getSubcommandName().equals("show")) {

            InteractionHook ih = e.deferReply(true).complete();

            Collection<HashMap<String, String>> maps = g.getWelcomeMessages();
            StringBuilder sb = new StringBuilder();
            AtomicInteger count = new AtomicInteger();
            Collection<SelectOption> options = new ArrayList<>();
            maps.forEach(map -> {
                count.getAndIncrement();
                GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId"));
                String message = map.get("message");
                boolean sendUser = Boolean.parseBoolean(map.get("sendUser"));
                boolean sendBot = Boolean.parseBoolean(map.get("sendBot"));
                sb.append("`"+count+".` **|** "+ch.getAsMention()+"\n");

                String name = ch.getName();
                if(name.length()>20) name = ch.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "welcomemessages.select.show."+ch.getId()));

            });
            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave() + " | **WelcomeMessages**")
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
            StringSelectMenu sm = StringSelectMenu.create("welcomemessages.select.show")
                    .addOptions(options)
                    .setDisabled(disabled)
                    .setMinValues(1)
                    .setMaxValues(1)
                    .setPlaceholder("📌 | Wähle einen Kanal zum bearbeiten")
                    .build();
            ih.editOriginalEmbeds(embed.build()).setActionRow(sm).queue();

        } else if(e.getSubcommandName().equals("variables")) {

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave()+" │ **WelcomeMessage - Variables**")
                    .setDescription("Du kannst folgende Variabeln bei deiner WelcomeMessage nutzen:")
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .setThumbnail(g.getIconUrl())

                    .addBlankField(false)
                    .addField(Emojis.mail()+" - %MENTION%", u.getAsMention(), true)
                    .addField(Emojis.pen()+" - %USERNAME%", u.getUsername(), true)
                    .addField(Emojis.crone()+" - %SERVER%", g.getName(), true)
                    .addBlankField(false)
                    .addField(Emojis.stats()+" - %MEMBERCOUNT%", String.valueOf(g.getMemberCount()), true)
                    .addField(Emojis.user()+" - %USERCOUNT%", String.valueOf(g.getUserCount()), true)
                    .addField(Emojis.bot()+" - %BOTCOUNT%", String.valueOf(g.getBotCount()), true);
            e.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;

        }

    }

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("welcomemessages.select.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        String channelId = e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3];

        GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(channelId);
        HashMap<String, String> config = g.getWelcomeMessage(ch);

        String message = config.get("message");
        boolean sendUser = Boolean.parseBoolean(config.get("sendUser"));
        boolean sendBot = Boolean.parseBoolean(config.get("sendBot"));

        String sendUserEmoji = Emojis.no();
        String sendBotEmoji = Emojis.no();
        if(sendUser) sendUserEmoji = Emojis.yes();
        if(sendBot) sendBotEmoji = Emojis.yes();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.wave()+" - Nachricht", message, true)
                .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

        Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
        Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
        Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("welcomemessages.btn.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(e.getButton().getId().split("\\.")[3]);
        if(ch == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültiger Channel")).setEphemeral(true).queue();
            return;
        }

        HashMap<String, String> map = g.getWelcomeMessage(ch);
        if(map==null) {
            e.replyEmbeds(Embeds.error(g, u, "Der Channel besitzt keine WelcomeMessage")).setEphemeral(true).queue();
            return;
        }

        String message = map.get("message");
        boolean sendUser = Boolean.parseBoolean(map.get("sendUser"));
        boolean sendBot = Boolean.parseBoolean(map.get("sendBot"));

        String sendUserEmoji = Emojis.no();
        String sendBotEmoji = Emojis.no();
        if(sendUser) sendUserEmoji = Emojis.yes();
        if(sendBot) sendBotEmoji = Emojis.yes();

        String id = e.getButton().getId().split("\\.")[4];

        if(id.equals("delete")) {

            g.removeWelcomeMessage(ch);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Im Kanal "+ch.getAsMention()+" werden nun keine User mehr begrüßt!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave())).withDisabled(true);
            Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user())).withDisabled(true);
            Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot())).withDisabled(true);
            Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete())).withDisabled(true);
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            return;

        } else if(id.equals("changeMessage")) {

            TextInput ti = TextInput.create("text", "Nachricht", TextInputStyle.PARAGRAPH)
                    .setPlaceholder(":wave: | **Herzlich Willkommen %MENTION%!**")
                    .setValue(message)
                    .setMinLength(1)
                    .setMaxLength(3000)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("welcomemessages.modal."+ch.getId()+".editMessage", "WelcomeMessage bearbeiten")
                    .addActionRow(ti)
                    .build();
            e.replyModal(modal).queue();

        } else if(id.equals("enableSendUser")) {

            if(sendUser) {
                e.deferEdit().queue();
                return;
            }
            sendUser = true;
            g.editWelcomeMessage(ch, message, sendUser, sendBot);

            sendUserEmoji = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.wave()+" - Nachricht", message, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                    .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

            Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
            Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

        } else if(id.equals("enableSendBot")) {

            if(sendBot) {
                e.deferEdit().queue();
                return;
            }

            sendBot = true;
            g.editWelcomeMessage(ch, message, sendUser, sendBot);

            sendBotEmoji = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.wave()+" - Nachricht", message, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                    .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

            Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
            Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

        } else if(id.equals("disableSendUser")) {

            if(!sendUser) {
                e.deferEdit().queue();
                return;
            }

            sendUser = false;
            g.editWelcomeMessage(ch, message, sendUser, sendBot);

            sendUserEmoji = Emojis.no();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.wave()+" - Nachricht", message, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                    .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

            Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
            Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

        } else if(id.equals("disableSendBot")) {

            if(!sendBot) {
                e.deferEdit().queue();
                return;
            }

            sendBot = false;
            g.editWelcomeMessage(ch, message, sendUser, sendBot);

            sendBotEmoji = Emojis.no();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.wave()+" - Nachricht", message, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                    .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

            Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
            Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
            Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

        }

    }

    public void onModalInteraction(ModalInteractionEvent e) {

        if(!e.getModalId().startsWith("welcomemessages.modal.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        String channelId = e.getModalId().split("\\.")[2];
        String action = e.getModalId().split("\\.")[3];

        GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(channelId);
        HashMap<String, String> map = g.getWelcomeMessage(ch);
        if(map == null) {
            e.replyEmbeds(Embeds.error(g, u, "Der Channel hat keine WelcomeMessage konfiguriert")).setEphemeral(true).queue();
            return;
        }

        String newMessage = e.getValue("text").getAsString();
        String message = map.get("message");
        boolean sendUser = Boolean.parseBoolean(map.get("sendUser"));
        boolean sendBot = Boolean.parseBoolean(map.get("sendBot"));
        String sendUserEmoji = Emojis.no();
        String sendBotEmoji = Emojis.no();
        if(sendUser) sendUserEmoji = Emojis.yes();
        if(sendBot) sendBotEmoji = Emojis.yes();

        g.editWelcomeMessage(ch, newMessage, sendUser, sendBot);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.wave()+" | **WelcomeMessages - Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.wave()+" - Nachricht", newMessage, true)
                .addField(Emojis.user()+" - User Nachrichten löschen", sendUserEmoji, true)
                .addField(Emojis.bot()+" - Für Bots senden", sendBotEmoji, true);

        Button btn1 = Button.primary("welcomemessages.btn.show."+ch.getId()+".changeMessage", "Nachricht ändern").withEmoji(Emoji.fromFormatted(Emojis.wave()));
        Button btn2 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        Button btn3 = Button.success("welcomemessages.btn.show."+ch.getId()+".enableSendBot", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));
        Button btn4 = Button.danger("welcomemessages.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        if(sendUser) btn2 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        if(sendBot) btn3 = Button.danger("welcomemessages.btn.show."+ch.getId()+".disableSendBot", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.bot()));

        e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

    }

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        g.getWelcomeMessages().forEach(map -> {

            GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId"));
            if(ch != null) {
                String message = map.get("message");
                boolean sendUser = Boolean.parseBoolean(map.get("sendUser"));
                boolean sendBot = Boolean.parseBoolean(map.get("sendBot"));

                if(u.isBot() && sendBot) try { ch.sendMessage(this.replaceMessage(message, g, u)).queue(); } catch (Exception ignored) { }
                if(!u.isBot() && sendUser) try { ch.sendMessage(this.replaceMessage(message, g, u)).queue(); } catch (Exception ignored) { }
            }

        });

    }

    private String replaceMessage(String message, KGuild g, KUser u) {

        return message
                .replaceAll("%SERVER%", g.getName())
                .replaceAll("%USERNAME%", u.getUsername())
                .replaceAll("%MEMBERCOUNT%", String.valueOf(g.getMemberCount()))
                .replaceAll("%USERCOUNT%", String.valueOf(g.getUserCount()))
                .replaceAll("%BOTCOUNT%", String.valueOf(g.getBotCount()))
                .replaceAll("%MENTION%", u.getAsMention());
    }

}
