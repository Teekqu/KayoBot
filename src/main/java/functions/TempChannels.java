package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TempChannels extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(e.getName().equals("joinhub")) {
            if(e.getGuild() == null || e.getMember() == null) return;
            KGuild g = new KGuild(e.getGuild());
            KUser u = new KUser(e.getUser());

            if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
                return;
            }

            if(e.getSubcommandName().equals("add")) {

                InteractionHook ih = e.deferReply(true).complete();

                VoiceChannel ch = e.getOption("channel").getAsChannel().asVoiceChannel();
                Category category = null;
                if(e.getOption("category")!=null) category = e.getOption("category").getAsChannel().asCategory();

                if(g.getJoinHub(ch) != null) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Der Kanal ist bereits ein JoinHub")).queue();
                    return;
                }
                if(g.getTempChannel(ch) != null) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Ein TempChannel kann kein JoinHub sein")).queue();
                    return;
                }
                boolean success = g.addJoinHub(ch, category, "⌛ ▪ %NAME%", 0);
                if(!success) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                    return;
                }
                String text = "";
                if(category != null) text = "\nAlle erstellen TempChannels werden in der Kategorie "+category.getAsMention()+" erstellt.";
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(Emojis.yes() + " │ Erfolgreich!")
                        .setDescription("Der Kanal "+ch.getAsMention()+" ist nun ein JoinHub!"+text)
                        .setColor(Get.embedColor(true))
                        .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                        .setThumbnail(e.getGuild().getIconUrl())
                        .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
                ih.editOriginalEmbeds(embed.build()).queue();
            } else if(e.getSubcommandName().equals("variables")) {

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(Emojis.join()+" │ **JoinHub - Variables**")
                        .setDescription("Du kannst folgende Variabeln bei deinem JoinHub Standard Namen nutzen:")
                        .setColor(Get.embedColor())
                        .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                        .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                        .setThumbnail(g.getIconUrl())

                        .addBlankField(false)
                        .addField(Emojis.pen()+" - %NAME%", u.getUsername(), true)
                        .addField(Emojis.idea()+" - %ID%", u.getId(), true);
                e.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;

            }

        } else if(e.getName().equals("joinhubs")) {

            if(e.getGuild() == null || e.getMember() == null) return;
            KGuild g = new KGuild(e.getGuild());
            KUser u = new KUser(e.getUser());

            if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
                return;
            }

            InteractionHook ih = e.deferReply(true).complete();

            Collection<VoiceChannel> channels = g.getJoinHubs();
            Collection<SelectOption> options = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            AtomicInteger count = new AtomicInteger();
            channels.forEach(ch -> {
                count.getAndIncrement();
                sb.append("`").append(count.get()).append(".` **|** ").append(ch.getAsMention()).append("\n");
                String name = ch.getName();
                if(name.length()>20) name = ch.getName().substring(0, 20)+"...";
                options.add(SelectOption.of(name, "joinhubs.select.show."+ch.getId()));
            });
            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden!*");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.join() + " | **JoinHubs**")
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
            StringSelectMenu sm = StringSelectMenu.create("joinhubs.select.show")
                    .addOptions(options)
                    .setDisabled(disabled)
                    .setMinValues(1)
                    .setMaxValues(1)
                    .setPlaceholder("📌 | Wähle einen Kanal zum bearbeiten")
                    .build();
            ih.editOriginalEmbeds(embed.build()).setActionRow(sm).queue();

        } else if(e.getName().equals("tempchannel")) {

            if(e.getGuild() == null || e.getMember() == null) return;
            KGuild g = new KGuild(e.getGuild());
            KUser u = new KUser(e.getUser());

            if(e.getMember().getVoiceState() == null || !e.getMember().getVoiceState().inAudioChannel() || e.getMember().getVoiceState().getChannel() == null) {
                e.replyEmbeds(Embeds.error(g, u, "Du bist in keinem TempChannel")).setEphemeral(true).queue();
                return;
            }
            VoiceChannel ch = e.getMember().getVoiceState().getChannel().asVoiceChannel();
            HashMap<String, String> map = g.getTempChannel(ch);
            if(map == null) {
                e.replyEmbeds(Embeds.error(g, u, "Du bist in keinem TempChannel")).setEphemeral(true).queue();
                return;
            }
            InteractionHook ih = e.deferReply(true).complete();
            String userId = map.get("userId");
            Collection<User> mods = new ArrayList<>();
            Collection<User> bans = new ArrayList<>();
            for(String s : map.get("modIds").split(";")) {
                if(!s.equals("0")) {
                    User user = e.getJDA().getUserById(s);
                    if(user == null) continue;
                    mods.add(user);
                }
            }
            for(String s : map.get("bans").split(";")) {
                if(!s.equals("0")) {
                    User user = e.getJDA().getUserById(s);
                    if(user == null) continue;
                    bans.add(user);
                }
            }

            if(e.getSubcommandName().equals("name")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                String name = e.getOption("name").getAsString();
                try {
                    ch.getManager().setName(name).queue();
                    ih.editOriginal(Emojis.yes()+" | **Der Name wurde erfolgreich geändert!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("limit")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                int limit = e.getOption("limit").getAsInt();
                try {
                    ch.getManager().setUserLimit(limit).queue();
                    ih.editOriginal(Emojis.yes()+" | **Das Limit wurde erfolgreich geändert!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("lock")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                     ch.getPermissionOverrides().forEach(perm -> {
                         if(!perm.isMemberOverride() || perm.getMember() == null || (!mods.contains(perm.getMember().getUser()) && !perm.getMember().getId().equals(userId))) {
                             perm.getManager().setDenied(Permission.VOICE_CONNECT).queue();
                         }
                     });
                     if(ch.getPermissionOverride(e.getGuild().getPublicRole()) == null) {
                         try { ch.getManager().putPermissionOverride(e.getGuild().getPublicRole(), null, EnumSet.of(Permission.VOICE_CONNECT)).queue(); } catch (Exception ignored) { }
                     } else {
                         try { ch.getPermissionOverride(e.getGuild().getPublicRole()).getManager().setDenied(Permission.VOICE_CONNECT); } catch (Exception ignored) { }
                     }
                     ih.editOriginal(Emojis.yes()+" | **Der Kanal wurde erfolgreich gesperrt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("unlock")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    ch.getPermissionOverrides().forEach(perm -> {
                        if(!perm.isMemberOverride() || perm.getMember() == null || (!mods.contains(perm.getMember().getUser()) && !perm.getMember().getId().equals(userId))) {
                            perm.getManager().clear(Permission.VOICE_CONNECT).queue();
                        }
                    });
                    ih.editOriginal(Emojis.yes()+" | **Der Kanal wurde erfolgreich entsperrt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("hide")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    ch.getPermissionOverrides().forEach(perm -> {
                        if(!perm.isMemberOverride() || perm.getMember() == null || (!mods.contains(perm.getMember().getUser()) && !perm.getMember().getId().equals(userId))) {
                            perm.getManager().setDenied(Permission.VIEW_CHANNEL).queue();
                        }
                        if(ch.getPermissionOverride(e.getGuild().getPublicRole()) == null) {
                            try { ch.getManager().putPermissionOverride(e.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue(); } catch (Exception ignored) { }
                        } else {
                            try { ch.getPermissionOverride(e.getGuild().getPublicRole()).getManager().setDenied(Permission.VIEW_CHANNEL); } catch (Exception ignored) { }
                        }
                    });
                    ih.editOriginal(Emojis.yes()+" | **Der Kanal wurde erfolgreich versteckt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("unhide")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    ch.getPermissionOverrides().forEach(perm -> {
                        if(!perm.isMemberOverride() || perm.getMember() == null || (!mods.contains(perm.getMember().getUser()) && !perm.getMember().getId().equals(userId))) {
                            perm.getManager().clear(Permission.VIEW_CHANNEL).queue();
                        }
                    });
                    ih.editOriginal(Emojis.yes()+" | **Der Kanal ist nun wieder sichtbar!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("add-mod")) {
                if(!e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    User user = e.getOption("user").getAsUser();
                    if(mods.contains(user)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist bereits ein Moderator")).queue();
                        return;
                    }
                    mods.add(user);
                    StringBuilder modIds = new StringBuilder();
                    StringBuilder banIds = new StringBuilder();
                    mods.forEach(mod -> {
                        modIds.append(mod.getId()+";");
                    });
                    bans.forEach(ban -> {
                        banIds.append(ban.getId()+";");
                    });
                    if(modIds.toString().equals("")) modIds.append("0;;");
                    if(banIds.toString().equals("")) banIds.append("0;;");
                    boolean success = g.editTempChannel(ch, e.getJDA().getUserById(userId), modIds.toString().substring(0, (modIds.toString().length()-1)), banIds.toString().substring(0, (banIds.toString().length()-1)));
                    if(!success) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                        return;
                    }
                    ih.editOriginal(Emojis.yes()+" | **Der User "+user.getAsMention()+" ist nun ein Moderator!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("remove-mod")) {
                if(!e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    User user = e.getOption("user").getAsUser();
                    if(!mods.contains(user)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist kein Moderator")).queue();
                        return;
                    }
                    mods.remove(user);
                    StringBuilder modIds = new StringBuilder();
                    StringBuilder banIds = new StringBuilder();
                    mods.forEach(mod -> {
                        modIds.append(mod.getId()+";");
                    });
                    bans.forEach(ban -> {
                        banIds.append(ban.getId()+";");
                    });
                    if(modIds.toString().equals("")) modIds.append("0;;");
                    if(banIds.toString().equals("")) banIds.append("0;;");
                    boolean success = g.editTempChannel(ch, e.getJDA().getUserById(userId), modIds.toString().substring(0, (modIds.toString().length()-1)), banIds.toString().substring(0, (banIds.toString().length()-1)));
                    if(!success) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                        return;
                    }
                    ih.editOriginal(Emojis.yes()+" | **Der User "+user.getAsMention()+" ist nun kein Moderator mehr!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("transfer")) {
                if(!e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    User user = e.getOption("user").getAsUser();
                    if(user.getId().equals(userId)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist bereits der Owner")).queue();
                        return;
                    }
                    if(!ch.getMembers().contains(e.getGuild().getMemberById(user.getId()))) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist nicht im TempChannel")).queue();
                        return;
                    }
                    StringBuilder modIds = new StringBuilder();
                    StringBuilder banIds = new StringBuilder();
                    mods.forEach(mod -> {
                        modIds.append(mod.getId()+";");
                    });
                    bans.forEach(ban -> {
                        banIds.append(ban.getId()+";");
                    });
                    if(modIds.toString().equals("")) modIds.append("0;;");
                    if(banIds.toString().equals("")) banIds.append("0;;");
                    boolean success = g.editTempChannel(ch, user, modIds.toString().substring(0, (modIds.toString().length()-1)), banIds.toString().substring(0, (banIds.toString().length()-1)));
                    if(!success) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                        return;
                    }
                    ih.editOriginal(Emojis.yes()+" | **Die Eigentumsrechte vom Kanal wurden an "+user.getAsMention()+" übertragen!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("kick")) {
                if(!mods.contains(e.getUser()) && !e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                Member member = e.getOption("user").getAsMember();
                try {
                    if(member == null || !ch.getMembers().contains(member)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist nicht im TempChannel")).queue();
                        return;
                    }
                    if((mods.contains(e.getUser()) && !member.getId().equals(userId))) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Du kannst diesen User nicht kicken")).queue();
                        return;
                    }
                    if(member.equals(e.getMember())) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Du kannst dich selbst nicht kicken")).queue();
                        return;
                    }
                    if(member.getId().equals(userId)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Du kannst diesen User nicht kicken")).queue();
                        return;
                    }
                    e.getGuild().kickVoiceMember(member).queue();
                    ih.editOriginal(Emojis.yes()+" | **Der User "+member.getAsMention()+" wurde erfolgreich gekickt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("ban")) {
                if(!e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    User user = e.getOption("user").getAsUser();
                    if(bans.contains(user)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist bereits gebannt")).queue();
                        return;
                    }
                    bans.add(user);
                    StringBuilder modIds = new StringBuilder();
                    StringBuilder banIds = new StringBuilder();
                    mods.forEach(mod -> {
                        modIds.append(mod.getId()+";");
                    });
                    bans.forEach(ban -> {
                        banIds.append(ban.getId()+";");
                    });
                    if(modIds.toString().equals("")) modIds.append("0;;");
                    if(banIds.toString().equals("")) banIds.append("0;;");
                    boolean success = g.editTempChannel(ch, e.getJDA().getUserById(userId), modIds.toString().substring(0, (modIds.toString().length()-1)), banIds.toString().substring(0, (banIds.toString().length()-1)));
                    if(!success) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                        return;
                    }
                    Member m = e.getGuild().getMemberById(user.getId());
                    if(m != null && m.getVoiceState() != null && m.getVoiceState().getChannel() != null) {
                        e.getGuild().kickVoiceMember(m).queue();
                    }
                    ih.editOriginal(Emojis.yes()+" | **Der User "+user.getAsMention()+" wurde erfolgreich gebannt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            } else if(e.getSubcommandName().equals("unban")) {
                if(!e.getUser().getId().equals(userId)) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).queue();
                    return;
                }
                try {
                    User user = e.getOption("user").getAsUser();
                    if(!bans.contains(user)) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Der User ist nicht gebannt")).queue();
                        return;
                    }
                    bans.remove(user);
                    StringBuilder modIds = new StringBuilder();
                    StringBuilder banIds = new StringBuilder();
                    mods.forEach(mod -> {
                        modIds.append(mod.getId()+";");
                    });
                    bans.forEach(ban -> {
                        banIds.append(ban.getId()+";");
                    });
                    if(modIds.toString().equals("")) modIds.append("0;;");
                    if(banIds.toString().equals("")) banIds.append("0;;");
                    boolean success = g.editTempChannel(ch, e.getJDA().getUserById(userId), modIds.toString().substring(0, (modIds.toString().length()-1)), banIds.toString().substring(0, (banIds.toString().length()-1)));
                    if(!success) {
                        ih.editOriginalEmbeds(Embeds.error(g, u, "Unbekannter Fehler")).queue();
                        return;
                    }
                    ih.editOriginal(Emojis.yes()+" | **Der User "+user.getAsMention()+" wurde erfolgreich entbannt!**").queue();
                } catch (Exception err) {
                    ih.editOriginalEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            }

        }

    }

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(!e.getInteraction().getSelectMenu().getId().equals("joinhubs.select.show")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();
        String channelId = e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3];
        VoiceChannel ch = e.getGuild().getVoiceChannelById(channelId);
        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).queue();
            return;
        }
        HashMap<String, String> map = g.getJoinHub(ch);
        if(map == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).queue();
            return;
        }

        String categoryId = map.get("categoryId");
        Category category = e.getGuild().getCategoryById(categoryId);
        String name = map.get("name");
        int limit = Integer.parseInt(map.get("limit"));

        String categoryValue = "None";
        if(category != null) categoryValue = category.getAsMention();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.join()+" | **JoinHubs - Kanal Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.channel()+" - Kategorie", categoryValue, true)
                .addField(Emojis.slash()+" - Standard Name", name, true)
                .addField(Emojis.user()+" - Standard Limit", String.valueOf(limit), true);
        Button btn1 = Button.primary("joinhubs.btn.edit."+ch.getId()+".name", "Namen ändern").withEmoji(Emoji.fromFormatted(Emojis.pen()));
        Button btn2 = Button.primary("joinhubs.btn.edit."+ch.getId()+".limit", "Limit ändern").withEmoji(Emoji.fromFormatted(Emojis.pin()));
        Button btn3 = Button.danger("joinhubs.btn.edit."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("joinhubs.btn.edit.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        String channelId = e.getButton().getId().split("\\.")[3];
        String id = e.getButton().getId().split("\\.")[4];
        VoiceChannel ch = e.getGuild().getVoiceChannelById(channelId);
        if(ch == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).setEphemeral(true).queue();
            return;
        }
        HashMap<String, String> map = g.getJoinHub(ch);
        if(map == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).setEphemeral(true).queue();
            return;
        }

        if(id.equals("name")) {
            TextInput ti = TextInput.create("input", "Name", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("⌛ ▪ %NAME%")
                    .setValue(map.get("name"))
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(25)
                    .build();
            Modal modal = Modal.create("joinhubs.modal.edit."+ch.getId()+".name", "JoinHub - Namen ändern")
                    .addActionRow(ti)
                    .build();
            e.replyModal(modal).queue();
        } else if(id.equals("limit")) {
            TextInput ti = TextInput.create("input", "Limit (0 = Kein Limit)", TextInputStyle.SHORT)
                    .setPlaceholder("0")
                    .setValue(map.get("limit"))
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(2)
                    .build();
            Modal modal = Modal.create("joinhubs.modal.edit."+ch.getId()+".limit", "JoinHub - Limit ändern")
                    .addActionRow(ti)
                    .build();
            e.replyModal(modal).queue();
        } else if(id.equals("delete")) {

            g.removeJoinHub(ch);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.yes() + " │ Erfolgreich!")
                    .setDescription("Der Kanal "+ch.getAsMention()+" ist nun kein JoinHub mehr!")
                    .setColor(Get.embedColor(true))
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                    .setThumbnail(e.getGuild().getIconUrl())
                    .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
            Button btn1 = Button.primary("joinhubs.btn.edit."+ch.getId()+".name", "Namen ändern").withEmoji(Emoji.fromFormatted(Emojis.pen())).withDisabled(true);
            Button btn2 = Button.primary("joinhubs.btn.edit."+ch.getId()+".limit", "Limit ändern").withEmoji(Emoji.fromFormatted(Emojis.pin())).withDisabled(true);
            Button btn3 = Button.danger("joinhubs.btn.edit."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete())).withDisabled(true);
            e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();
        }

    }

    public void onModalInteraction(ModalInteractionEvent e) {
        if(!e.getModalId().startsWith("joinhubs.modal.edit.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }
        InteractionHook ih = e.deferEdit().complete();
        String channelId = e.getModalId().split("\\.")[3];
        String id = e.getModalId().split("\\.")[4];
        VoiceChannel ch = e.getGuild().getVoiceChannelById(channelId);
        String input = e.getValue("input").getAsString();
        if(ch == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).queue();
            return;
        }
        HashMap<String, String> map = g.getJoinHub(ch);
        if(map == null) {
            ih.editOriginalEmbeds(Embeds.error(g, u, "Ungültiger JoinHub")).queue();
            return;
        }

        String categoryId = map.get("categoryId");
        Category category = e.getGuild().getCategoryById(categoryId);
        String name = map.get("name");
        int limit = Integer.parseInt(map.get("limit"));

        String categoryValue = "None";
        if(category != null) categoryValue = category.getAsMention();

        if(id.equals("name")) {
            boolean success = g.editJoinHub(ch, category, input, limit);
            if(success) name = input;
        } else if(id.equals("limit")) {
            if(!Check.isInteger(input)) {
                ih.editOriginalEmbeds(Embeds.error(g, u, "Das Limit muss eine ganze Zahl sein")).queue();
                return;
            }
            boolean success = g.editJoinHub(ch, category, name, Integer.parseInt(input));
            if(success) limit = Integer.parseInt(input);
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.join()+" | **JoinHubs - Kanal Info**")
                .setDescription("**Kanal:** "+ch.getAsMention())
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(g.getIconUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .addField(Emojis.channel()+" - Kategorie", categoryValue, true)
                .addField(Emojis.slash()+" - Standard Name", name, true)
                .addField(Emojis.user()+" - Standard Limit", String.valueOf(limit), true);
        Button btn1 = Button.primary("joinhubs.btn.edit."+ch.getId()+".name", "Namen ändern").withEmoji(Emoji.fromFormatted(Emojis.pen()));
        Button btn2 = Button.primary("joinhubs.btn.edit."+ch.getId()+".limit", "Limit ändern").withEmoji(Emoji.fromFormatted(Emojis.pin()));
        Button btn3 = Button.danger("joinhubs.btn.edit."+ch.getId()+".delete", "Löschen").withEmoji(Emoji.fromFormatted(Emojis.delete()));
        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }


    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {

        KGuild g = new KGuild(e.getGuild());

        if(e.getChannelLeft() != null) {
            VoiceChannel ch = e.getChannelLeft().asVoiceChannel();
            HashMap<String, String> map = g.getTempChannel(ch);
            if(map != null) {
                if(ch.getMembers().size() == 0) {
                    g.removeTempChannel(ch);
                    ch.delete().queue();
                } else {
                    if(map.get("userId").equals(e.getMember().getId())) {
                        g.editTempChannel(ch, ch.getMembers().get(0).getUser(), map.get("modIds"), map.get("bans"));
                    }
                }
            }
        }
        if(e.getChannelJoined() != null) {
            VoiceChannel ch = e.getChannelJoined().asVoiceChannel();
            HashMap<String, String> map = g.getTempChannel(ch);
            if(map != null) {
                for(String s : map.get("bans").split(";")) {
                    if(s.equals(e.getMember().getId())) {
                        try { e.getGuild().kickVoiceMember(e.getMember()).queue(); } catch (Exception ignored) { }
                    }
                }
            }
            if(g.getJoinHubs().contains(ch)) {
                try {
                    map = g.getJoinHub(ch);
                    Category category = null;
                    if(!map.get("categoryId").equals("0")) category = e.getGuild().getCategoryById(map.get("categoryId"));
                    String name = map.get("name");
                    int limit = Integer.parseInt(map.get("limit"));
                    VoiceChannel channel = e.getGuild().createVoiceChannel(this.replaceName(name, e.getMember().getUser()), category)
                            .setUserlimit(limit)
                            .addPermissionOverride(e.getMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_SPEAK, Permission.VOICE_CONNECT), null)
                            .complete();
                    g.addTempChannel(channel, e.getMember().getUser());
                    e.getGuild().moveVoiceMember(e.getMember(), channel).queue();

                } catch (Exception ignored) { }
            }
        }

    }

    private String replaceName(String name, User user) {
        return name
                .replaceAll("%NAME%", user.getName())
                .replaceAll("%ID%", user.getId());
    }

}
