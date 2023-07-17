package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.apache.commons.collections4.functors.NonePredicate;
import org.checkerframework.checker.units.qual.K;
import utils.*;

import java.util.*;

public class AutoDelete extends ListenerAdapter {

    private Collection<Long> activeUsers = new ArrayList<>();

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("autodelete")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("add")) {

            InteractionHook ih = e.deferReply(true).complete();

            if(g.getAutoDelete().size()>=Get.limit("autodelete", g.hasPremium())) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Server hat das Limit erreicht")).queue();
                return;
            }
            if(e.getOption("channel")==null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
                return;
            }
            GuildMessageChannel ch = e.getOption("channel").getAsChannel().asGuildMessageChannel();
            if(g.getAutoDelete(ch)!=null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Channel besitzt bereits AutoDelete")).queue();
                return;
            }
            if(e.getOption("time")==null||e.getOption("unit")==null) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültige Argumente")).queue();
                return;
            }

            int time = e.getOption("time").getAsInt();
            String unit = e.getOption("unit").getAsString();

            if(unit.equals("m")) time = (time*60);
            if(unit.equals("h")) time = ((time*60)*60);
            if(unit.equals("d")) time = (((time*60)*60)*24);

            g.addAutoDelete(ch, Long.parseLong(String.valueOf(time)), false, true, true);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Alle neuen Nachrichten im Channel "+ch.getAsMention()+" werden nach "+e.getOption("time").getAsInt()+unit+" gelöscht!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            ih.editOriginalEmbeds(embed.build()).queue();

        } else if(e.getSubcommandName().equals("show")) {

            InteractionHook ih = e.deferReply(true).complete();

            Collection<HashMap<String, String>> maps = g.getAutoDelete();

            StringBuilder liste = new StringBuilder();

            int count = 1;
            Collection<SelectOption> options = new ArrayList<>();

            for(HashMap<String, String> map : maps) {

                String channelId = map.get("channelId");
                GuildChannel ch = e.getGuild().getGuildChannelById(channelId);
                if(ch == null) continue;
                Long seconds = Long.parseLong(map.get("seconds"));
                boolean delPins = Boolean.parseBoolean(map.get("delPins"));
                boolean delBots = Boolean.parseBoolean(map.get("delBots"));
                boolean delUser = Boolean.parseBoolean(map.get("delUser"));

                liste.append("`"+count+".` **|** "+ch.getAsMention()+" **|** "+this.convertSecondsInFormat(seconds)+"\n");
                String name = ch.getName();
                if(name.length()>20) name = ch.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "autodelete.select.show."+ch.getId()));
                count = count+1;
            }

            if(liste.toString().equals("")) liste.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete() + " | **AutoDelete**")
                    .setDescription(liste.toString())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            boolean disabled = false;
            if(options.size() == 0) {
                options.add(SelectOption.of("None", "none.none.none"));
                disabled = true;
            }
            StringSelectMenu sm = StringSelectMenu.create("autodelete.select.show")
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

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("autodelete.select.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        String channelId = e.getInteraction().getSelectedOptions().get(0).getValue().substring(23);
        GuildChannel ch = e.getGuild().getGuildChannelById(channelId);
        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
            return;
        }

        HashMap<String, String> map = g.getAutoDelete((GuildMessageChannel) ch);

        String delPins = Emojis.no();
        if(map.get("delPins").toLowerCase().equals("true")) delPins = Emojis.yes();

        String delBots = Emojis.no();
        if(map.get("delBots").toLowerCase().equals("true")) delBots = Emojis.yes();

        String delUser = Emojis.no();
        if(map.get("delUser").toLowerCase().equals("true")) delUser = Emojis.yes();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

        Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
        Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
        Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
        Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
        if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
        if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));

        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("autodelete.btn.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(this.activeUsers.contains(u.getIdLong())) e.deferEdit().queue();

        this.activeUsers.add(u.getIdLong());

        if(!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            this.activeUsers.remove(u.getIdLong());
            return;
        }

        GuildChannel ch = e.getGuild().getGuildChannelById(e.getButton().getId().split("\\.")[3]);
        if(ch == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültiger Channel")).setEphemeral(true).queue();
            this.activeUsers.remove(u.getIdLong());
            return;
        }

        HashMap<String, String> map = g.getAutoDelete((GuildMessageChannel) ch);

        String id = e.getButton().getId().split("\\.")[4];

        if(id.equals("delete")) {

            if(g.getAutoDelete((GuildMessageChannel) ch)==null) {
                e.replyEmbeds(Embeds.error(g, u, "Der Channel besitzt kein AutoDelete")).setEphemeral(true).queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.removeAutoDelete((GuildMessageChannel) ch);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Im Kanal "+ch.getAsMention()+" werden nun keine Nachrichten mehr gelöscht!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin())).withDisabled(true);
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash())).withDisabled(true);
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user())).withDisabled(true);
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete())).withDisabled(true);
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());
        } else if(id.equals("enableDelPins")) {

            if(map.get("delPins").toLowerCase().equals("true")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), true, map.get("delBots").toLowerCase().equals("true"), map.get("delUser").toLowerCase().equals("true"));

            String delPins = Emojis.yes();

            String delBots = Emojis.no();
            if(map.get("delBots").toLowerCase().equals("true")) delBots = Emojis.yes();

            String delUser = Emojis.no();
            if(map.get("delUser").toLowerCase().equals("true")) delUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        } else if(id.equals("enableDelBots")) {

            if(map.get("delBots").toLowerCase().equals("true")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), map.get("delPins").toLowerCase().equals("true"), true, map.get("delUser").toLowerCase().equals("true"));

            String delBots = Emojis.yes();

            String delPins = Emojis.no();
            if(map.get("delPins").toLowerCase().equals("true")) delPins = Emojis.yes();

            String delUser = Emojis.no();
            if(map.get("delUser").toLowerCase().equals("true")) delUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        } else if(id.equals("enableDelUser")) {

            if(map.get("delUser").toLowerCase().equals("true")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), map.get("delPins").toLowerCase().equals("true"), map.get("delBots").toLowerCase().equals("true"), true);

            String delUser = Emojis.yes();

            String delPins = Emojis.no();
            if(map.get("delPins").toLowerCase().equals("true")) delPins = Emojis.yes();

            String delBots = Emojis.no();
            if(map.get("delBots").toLowerCase().equals("true")) delBots = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        }else if(id.equals("disableDelPins")) {

            if(map.get("delPins").toLowerCase().equals("false")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), false, map.get("delBots").toLowerCase().equals("true"), map.get("delUser").toLowerCase().equals("true"));

            String delPins = Emojis.no();

            String delBots = Emojis.no();
            if(map.get("delBots").toLowerCase().equals("true")) delBots = Emojis.yes();

            String delUser = Emojis.no();
            if(map.get("delUser").toLowerCase().equals("true")) delUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        } else if(id.equals("disableDelBots")) {

            if(map.get("delBots").toLowerCase().equals("false")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), map.get("delPins").toLowerCase().equals("true"), false, map.get("delUser").toLowerCase().equals("true"));

            String delBots = Emojis.no();

            String delPins = Emojis.no();
            if(map.get("delPins").toLowerCase().equals("true")) delPins = Emojis.yes();

            String delUser = Emojis.no();
            if(map.get("delUser").toLowerCase().equals("true")) delUser = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        } else if(id.equals("disableDelUser")) {

            if(map.get("delUser").toLowerCase().equals("false")) {
                e.deferEdit().queue();
                this.activeUsers.remove(u.getIdLong());
                return;
            }

            g.editAutoDelete((GuildMessageChannel) ch, Long.parseLong(map.get("seconds")), map.get("delPins").toLowerCase().equals("true"), map.get("delBots").toLowerCase().equals("true"), false);

            String delUser = Emojis.no();

            String delPins = Emojis.no();
            if(map.get("delPins").toLowerCase().equals("true")) delPins = Emojis.yes();

            String delBots = Emojis.no();
            if(map.get("delBots").toLowerCase().equals("true")) delBots = Emojis.yes();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.delete()+" | **AutoDelete - Kanal Info**")
                    .setDescription("**Kanal:** "+ch.getAsMention())
                    .setColor(Get.embedColor())
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                    .addField(Emojis.pin()+" - Pins löschen", delPins, true)
                    .addField(Emojis.slash()+" - Bot Nachrichten löschen", delBots, true)
                    .addField(Emojis.user()+" - User Nachrichten löschen", delUser, true);

            Button btn1 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelPins", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            Button btn2 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelBots", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            Button btn3 = Button.success("autodelete.btn.show."+ch.getId()+".enableDelUser", "Aktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            Button btn4 = Button.danger("autodelete.btn.show."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
            if(delPins.equals(Emojis.yes())) btn1 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelPins", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.pin()));
            if(delBots.equals(Emojis.yes())) btn2 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelBots", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.slash()));
            if(delUser.equals(Emojis.yes())) btn3 = Button.danger("autodelete.btn.show."+ch.getId()+".disableDelUser", "Deaktivieren").withEmoji(Emoji.fromFormatted(Emojis.user()));
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3, btn4).queue();
            this.activeUsers.remove(u.getIdLong());

        }

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

    public void onMessageReceived(MessageReceivedEvent e) {

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getAuthor());

        if(g.getAutoDelete(e.getChannel().asGuildMessageChannel())==null) return;

        HashMap<String, String> map = g.getAutoDelete(e.getChannel().asGuildMessageChannel());
        Long seconds = Long.parseLong(map.get("seconds"));
        boolean delPins = Boolean.parseBoolean(map.get("delPins"));
        boolean delBots = Boolean.parseBoolean(map.get("delBots"));
        boolean delUser = Boolean.parseBoolean(map.get("delUser"));

        TimerTask task = new TimerTask() {
            public void run() {
                if(e.getMessage().getAuthor().isSystem()) return;
                try { Thread.sleep(seconds*1000); } catch (Exception ignored) { }
                Message msg = Kayo.Kayo.getJda().getGuildById(g.getId()).getTextChannelById(e.getChannel().getId()).retrieveMessageById(e.getMessageId()).complete();
                if(msg == null) return;
                if(msg.getAuthor().isBot()) {
                    if(delBots && !msg.isPinned()) try { msg.delete().queue(); } catch (Exception ignored) { }
                    if(delBots && msg.isPinned() && delPins) try { msg.delete().queue(); } catch (Exception ignored) { }
                } else {
                    if(delUser && !msg.isPinned()) try { msg.delete().queue(); } catch (Exception ignored) { }
                    if(delUser && msg.isPinned() && delPins) try { msg.delete().queue(); } catch (Exception ignored) { }
                }
            }
        };
        Timer timer = new Timer("AutoDelete");

        timer.schedule(task, 0);

    }

}
