package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.*;

import java.awt.*;
import java.util.*;

public class AutoReact extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("autoreact")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        if(u.isBlocked()) return;

        if (!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("add")) {
            InteractionHook ih = e.deferReply(true).complete();

            GuildMessageChannel ch = e.getOption("channel").getAsChannel().asGuildMessageChannel();
            String emoji1 = e.getOption("emoji").getAsString();
            Emoji emoji;
            try {
                emoji = Emoji.fromFormatted(emoji1);
            } catch (Exception err) {
                ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                return;
            }

            if(g.getAutoReact().size()>=Get.limit("autoreactChannels", g.hasPremium())) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Server hat das Limit erreicht")).queue();
                return;
            }
            if(g.getAutoReact(ch)!=null && g.getAutoReact(ch).size()>=Get.limit("autoreactEmojis", g.hasPremium())) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Der Kanal hat das Limit erreicht")).queue();
                return;
            }
            if(g.getAutoReact(ch)!=null && g.getAutoReact(ch).contains(emoji)) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Das Emoji wurde bereits hinzugefügt")).queue();
                return;
            }

            boolean success = g.addAutoReact(ch, emoji);
            if(!success) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Es gab einen Fehler beim hinzufügen des Emojis")).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Alle neuen Nachrichten im Channel "+ch.getAsMention()+" werden nun mit dem Emoji "+emoji.getFormatted()+" reagiert!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            ih.editOriginalEmbeds(embed.build()).queue();

        } else if(e.getSubcommandName().equals("show")) {

            InteractionHook ih = e.deferReply(true).complete();
            StringBuilder liste = new StringBuilder();
            int count = 1;
            Collection<SelectOption> options = new ArrayList<>();

            for(GuildMessageChannel ch : g.getAutoReact()) {
                Collection<Emoji> emojis = g.getAutoReact(ch);
                liste.append("`"+count+".` **|** "+ch.getAsMention()+" **|** ");
                for(Emoji emoji : emojis) {
                    liste.append(emoji.getFormatted()+" ");
                }
                liste.append("\n");
                count = count+1;
                String name = ch.getName();
                if(name.length()>20) name = ch.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "autoreact.select.show."+ch.getId()+".emoji"));
            }

            if(liste.toString().equals("")) liste.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.emoji() + " | **AutoReact**")
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
            StringSelectMenu sm = StringSelectMenu.create("autoreact.select.show")
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

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("autoreact.select.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        String channelId = e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3];
        GuildChannel ch = e.getGuild().getGuildChannelById(channelId);
        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Channel")).queue();
            return;
        }

        if(e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.").length>5) {
            try {
                Emoji emoji = Emoji.fromFormatted(e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[5]);
                boolean success = g.removeAutoReact((GuildMessageChannel) ch, emoji);
                if(!success) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Es gab ein Fehler beim löschen")).queue();
                    return;
                }
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(Emojis.yes() + " │ Erfolgreich!")
                        .setDescription("Im Kanal "+ch.getAsMention()+" wird nun auf keine Nachrichten mehr mit "+emoji.getFormatted()+" reagiert!")
                        .setColor(Get.embedColor(true))
                        .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                        .setThumbnail(e.getGuild().getIconUrl())
                        .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
                Button btn1 = Button.danger("autoreact.btn.show."+ch.getId()+".deleteEmoji", "Emoji löschen").withEmoji(Emoji.fromFormatted(Emojis.emoji())).withDisabled(true);
                Button btn2 = Button.danger("autoreact.btn.show."+ch.getId()+".delete", "Alle löschen").withEmoji(Emoji.fromFormatted(Emojis.channel())).withDisabled(true);
                ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2).queue();
                return;
            } catch (Exception err) {
                ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                return;
            }
        }

        Collection<Emoji> emojis = g.getAutoReact((GuildMessageChannel) ch);
        StringBuilder sb = new StringBuilder();
        emojis.forEach(emoji -> {
            sb.append(emoji.getFormatted()).append(" ");
        });

        if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.delete()+" | **AutoReact - Kanal Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.list()+" - Emojis", sb.toString(), true);

        Button btn1 = Button.danger("autoreact.btn.show."+ch.getId()+".deleteEmoji", "Emoji löschen").withEmoji(Emoji.fromFormatted(Emojis.emoji()));
        Button btn2 = Button.danger("autoreact.btn.show."+ch.getId()+".delete", "Alle löschen").withEmoji(Emoji.fromFormatted(Emojis.channel()));
        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("autoreact.btn.show.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        GuildMessageChannel ch = (GuildMessageChannel) e.getGuild().getGuildChannelById(e.getButton().getId().split("\\.")[3]);
        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger Kanal")).queue();
            return;
        }
        String id = e.getButton().getId().split("\\.")[4];

        if(id.equals("deleteEmoji")) {

            Collection<Emoji> emojis = g.getAutoReact(ch);
            Collection<SelectOption> options = new ArrayList<>();
            emojis.forEach(emoji->{
                String name = emoji.getName();
                if(name.length()>= 20) name = name.substring(0, 20)+"...";
                options.add(SelectOption.of(name, "autoreact.select.show."+ch.getId()+".emoji."+emoji.getFormatted()).withEmoji(emoji));
            });
            StringSelectMenu sm = StringSelectMenu.create("autoreact.select.show."+ch.getId()+".delete")
                    .addOptions(options)
                    .setMinValues(1)
                    .setMaxValues(1)
                    .setPlaceholder("📌 | Wähle ein Emoji zum löschen")
                    .build();
            ih.editOriginal(Emojis.list()+" | **Wähle ein Emoji zum löschen**").setActionRow(sm).queue();

        } else if(id.equals("delete")) {
            boolean success = g.removeAutoReact(ch);
            if(!success) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Es gab einen Fehler beim entfernen")).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Im Kanal "+ch.getAsMention()+" wird nun auf keine Nachrichten mehr reagiert!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            Button btn1 = Button.danger("autoreact.btn.show."+ch.getId()+".deleteEmoji", "Emoji löschen").withEmoji(Emoji.fromFormatted(Emojis.emoji())).withDisabled(true);
            Button btn2 = Button.danger("autoreact.btn.show."+ch.getId()+".delete", "Alle löschen").withEmoji(Emoji.fromFormatted(Emojis.channel())).withDisabled(true);
            ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2).queue();
        }

    }

    public void onMessageReceived(MessageReceivedEvent e) {

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getAuthor());

        if(!g.getAutoReact().contains(e.getChannel().asGuildMessageChannel())) return;

        try {
            g.getAutoReact(e.getChannel().asGuildMessageChannel()).forEach(emoji -> {
                try { e.getMessage().addReaction(emoji).queue(); } catch (Exception ignored) { }
            });
        } catch (Exception ignored) { }

    }

}
