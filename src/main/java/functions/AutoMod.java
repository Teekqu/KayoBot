package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.automod.build.AutoModRuleData;
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.checkerframework.checker.units.qual.K;
import org.w3c.dom.Text;
import utils.*;

import java.time.OffsetDateTime;
import java.util.*;

public class AutoMod extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("automod")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        if(e.getSubcommandName().equals("rules")) {

            StringBuilder sb = new StringBuilder();

            int count = 1;
            Collection<SelectOption> options = new ArrayList<>();

            for(AutoModRule rule : e.getGuild().retrieveAutoModRules().complete()) {
                Collection<AutoModResponse> responses = rule.getActions();
                Collection<String> allowed = rule.getAllowlist();
                String creatorId = rule.getCreatorId();
                AutoModEventType eventType = rule.getEventType();
                Collection<GuildChannel> exemptChannels = rule.getExemptChannels();
                Collection<Role> exemptRoles = rule.getExemptRoles();
                Collection<String> blockedKeywords = rule.getFilteredKeywords();
                int mentionLimit = rule.getMentionLimit();
                AutoModTriggerType triggerType = rule.getTriggerType();
                String name = rule.getName();
                boolean enabled = rule.isEnabled();
                boolean isMentionRaidProtectionEnabled = rule.isMentionRaidProtectionEnabled();
                OffsetDateTime timeCreated = rule.getTimeCreated();
                long id = rule.getIdLong();

                sb.append("`").append(count).append(".` **|** ").append(name).append("\n");

                options.add(SelectOption.of(String.valueOf(count), "automod.rules.edit."+id));
                count++;
            }

            boolean disabled = false;
            if(sb.toString().equals("")) {
                disabled = true;
                sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*");
                options.add(SelectOption.of("none", "none"));
            }

            StringSelectMenu sm = StringSelectMenu.create("automod.rules.edit")
                    .setPlaceholder("Wähle eine AutoMod Rule")
                    .setMinValues(1)
                    .setMaxValues(1)
                    .addOptions(options)
                    .setDisabled(disabled)
                    .build();

            e.reply(sb.toString()).setEphemeral(true).setActionRow(sm).queue();

        }

    }

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("automod.rules.edit.")) return;
        if(e.getGuild()==null || e.getMember()==null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        InteractionHook ih = e.deferEdit().complete();

        String id = e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3];
        AutoModRule r = e.getGuild().retrieveAutoModRuleById(id).complete();

        Collection<AutoModResponse> responses = r.getActions();
        Collection<String> allowed = r.getAllowlist();
        String creatorId = r.getCreatorId();
        UserSnowflake creator = UserSnowflake.fromId(creatorId);
        AutoModEventType eventType = r.getEventType();
        Collection<GuildChannel> exemptChannels = r.getExemptChannels();
        Collection<Role> exemptRoles = r.getExemptRoles();
        Collection<String> blockedKeywords = r.getFilteredKeywords();
        int mentionLimit = r.getMentionLimit();
        AutoModTriggerType triggerType = r.getTriggerType();
        String name = r.getName();
        boolean enabled = r.isEnabled();
        String active = Emojis.no();
        if(enabled) active = Emojis.yes();
        boolean isMentionRaidProtectionEnabled = r.isMentionRaidProtectionEnabled();
        OffsetDateTime timeCreated = r.getTimeCreated();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.automod() + " │ AutoMod - Rules")
                .setDescription("Bearbeite oder lösche die AutoMod Rule mit der ID `"+r.getId()+"`!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(g.getIconUrl())

                .addField(Emojis.pen()+" - Name", name, true)
                .addField(Emojis.idea()+" - ID", id, true)
                .addField(Emojis.settings()+" - Type", triggerType.name(), true)
                .addBlankField(false)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+timeCreated.toInstant().getEpochSecond()+"> (<t:"+timeCreated.toInstant().getEpochSecond()+":R>)", true)
                .addField(Emojis.user()+" - Ersteller", creator.getAsMention(), true)
                .addField(Emojis.search()+" - Aktiviert", active, true);
        if(triggerType.equals(AutoModTriggerType.KEYWORD)) {
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            blockedKeywords.forEach(word->{
                sb.append(word).append(", ");
            });
            allowed.forEach(word->{
                sb2.append(word).append(", ");
            });
            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            if(sb2.toString().equals("")) sb2.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            embed.addBlankField(false);
            embed.addField(Emojis.no()+" - Verbotene Wörter", sb.toString().substring(0, (sb.toString().length()-2)), false);
            embed.addField(Emojis.yes()+" - Erlaubte Wörter", sb2.toString().substring(0, (sb2.toString().length()-2)), false);
        }
        Button btn1 = Button.success("automod.rules.edit.btn."+id+".blacklist", "Blacklist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn2 = Button.success("automod.rules.edit.btn."+id+".whitelist", "Whitelist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn3 = Button.success("automod.rules.edit.btn."+id+".activate", "Aktivieren");
        if(enabled) btn3 = Button.danger("automod.rules.edit.btn."+id+".deactivate", "Deaktivieren");
        ih.editOriginal("").setEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("automod.rules.edit.btn.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        String ruleId = e.getButton().getId().split("\\.")[4];
        String action = e.getButton().getId().split("\\.")[5];

        AutoModRule r = e.getGuild().retrieveAutoModRuleById(ruleId).complete();
        if(r == null) {
            e.editMessageEmbeds(Embeds.error(g, u, "Ungültige AutoMod Rule")).queue();
            return;
        }
        if(action == null) {
            e.editMessageEmbeds(Embeds.error(g, u, "Ungültige Button Action")).queue();
            return;
        }

        Collection<AutoModResponse> responses = r.getActions();
        Collection<String> allowed = r.getAllowlist();
        String creatorId = r.getCreatorId();
        UserSnowflake creator = UserSnowflake.fromId(creatorId);
        AutoModEventType eventType = r.getEventType();
        Collection<GuildChannel> exemptChannels = r.getExemptChannels();
        Collection<Role> exemptRoles = r.getExemptRoles();
        Collection<String> blockedKeywords = r.getFilteredKeywords();
        int mentionLimit = r.getMentionLimit();
        AutoModTriggerType triggerType = r.getTriggerType();
        String name = r.getName();
        boolean enabled = r.isEnabled();
        String active = Emojis.no();
        if(enabled) active = Emojis.yes();
        boolean isMentionRaidProtectionEnabled = r.isMentionRaidProtectionEnabled();
        OffsetDateTime timeCreated = r.getTimeCreated();

        switch (action) {
            case "deactivate" -> {
                try {
                    r.getManager().setEnabled(false).queue();
                    enabled = false;
                    active = Emojis.no();
                } catch (Exception err) {
                    e.editMessageEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            }
            case "activate" -> {
                try {
                    r.getManager().setEnabled(true).queue();
                    enabled = true;
                    active = Emojis.yes();
                } catch (Exception err) {
                    e.editMessageEmbeds(Embeds.error(g, u, err.getMessage())).queue();
                    return;
                }
            }
            case "blacklist" -> {
                StringBuilder sb = new StringBuilder();
                r.getFilteredKeywords().forEach(word -> {
                    sb.append(word).append(", ");
                });
                String value = sb.toString();
                if (value.equals("")) value = null;
                if(value != null) value = value.substring(0, (sb.toString().length()-2));
                TextInput ti1 = TextInput.create("words", "Blockierte Wörter", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Wort1, Wort2, *Wort, Wort*, *Wort*")
                        .setRequired(true)
                        .setMinLength(1)
                        .setMaxLength(4000)
                        .setValue(value)
                        .build();
                Modal modal = Modal.create("autmod.rules.edit.modal." + ruleId + ".blacklist", "AutoMod Blacklist bearbeiten")
                        .addActionRow(ti1)
                        .build();
                e.replyModal(modal).queue();
                return;
            }
            case "whitelist" -> {
                StringBuilder sb = new StringBuilder();
                r.getAllowlist().forEach(word -> {
                    sb.append(word).append(", ");
                });
                String value = sb.toString();
                if (value.equals("")) value = null;
                if(value != null) value = value.substring(0, (sb.toString().length()-2));
                TextInput ti1 = TextInput.create("words", "Erlaubte Wörter", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Wort1, Wort2, *Wort, Wort*, *Wort*")
                        .setRequired(true)
                        .setMinLength(1)
                        .setMaxLength(4000)
                        .setValue(value)
                        .build();
                Modal modal = Modal.create("autmod.rules.edit.modal." + ruleId + ".whitelist", "AutoMod Whitelist bearbeiten")
                        .addActionRow(ti1)
                        .build();
                e.replyModal(modal).queue();
                return;
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.automod() + " │ AutoMod - Rules")
                .setDescription("Bearbeite oder lösche die AutoMod Rule mit der ID `"+r.getId()+"`!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(g.getIconUrl())

                .addField(Emojis.pen()+" - Name", name, true)
                .addField(Emojis.idea()+" - ID", ruleId, true)
                .addField(Emojis.settings()+" - Type", triggerType.name(), true)
                .addBlankField(false)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+timeCreated.toInstant().getEpochSecond()+"> (<t:"+timeCreated.toInstant().getEpochSecond()+":R>)", true)
                .addField(Emojis.user()+" - Ersteller", creator.getAsMention(), true)
                .addField(Emojis.search()+" - Aktiviert", active, true);
        if(triggerType.equals(AutoModTriggerType.KEYWORD)) {
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            blockedKeywords.forEach(word->{
                sb.append(word).append(", ");
            });
            allowed.forEach(word->{
                sb2.append(word).append(", ");
            });
            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            if(sb2.toString().equals("")) sb2.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            embed.addBlankField(false);
            embed.addField(Emojis.no()+" - Verbotene Wörter", sb.toString().substring(0, (sb.toString().length()-2)), false);
            embed.addField(Emojis.yes()+" - Erlaubte Wörter", sb2.toString().substring(0, (sb2.toString().length()-2)), false);
        }
        Button btn1 = Button.success("automod.rules.edit.btn."+r.getId()+".blacklist", "Blacklist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn2 = Button.success("automod.rules.edit.btn."+r.getId()+".whitelist", "Whitelist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn3 = Button.success("automod.rules.edit.btn."+r.getId()+".activate", "Aktivieren");
        if(enabled) btn3 = Button.danger("automod.rules.edit.btn."+r.getId()+".deactivate", "Deaktivieren");
        e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }

    public void onModalInteraction(ModalInteractionEvent e) {

        if(!e.getModalId().startsWith("autmod.rules.edit.modal.")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.replyEmbeds(Embeds.error(g, u, "Dir fehlen nötige Rechte")).setEphemeral(true).queue();
            return;
        }

        String ruleId = e.getModalId().split("\\.")[4];
        String action = e.getModalId().split("\\.")[5];

        AutoModRule r = e.getGuild().retrieveAutoModRuleById(ruleId).complete();
        if(r == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültige AutoMod Rule")).setEphemeral(true).queue();
            return;
        }
        if(action == null) {
            e.replyEmbeds(Embeds.error(g, u, "Ungültige Button Action")).setEphemeral(true).queue();
            return;
        }

        String value = e.getValue("words").getAsString();
        Collection<String> words = new ArrayList<>();
        for(String s : value.split(",")) {
            List<String> list = new ArrayList<>(Arrays.stream(s.split("")).toList());
            int count = 0;
            boolean check = false;
            for(String x : list) {
                if(x.equals(" ") && !check) count++;
                if(!x.equals(" ")) {
                    check = true;
                }
            }
            Collections.reverse(list);
            check = false;
            int count1 = 0;
            for(String x : list) {
                if(x.equals(" ") && !check) count1++;
                if(!x.equals(" ")) {
                    check = true;
                }
            }

            words.add(s.substring(count, (s.length()-count1)));
        }

        if(action.equals("whitelist")) {

            r.getManager().setTriggerConfig(TriggerConfig.keywordFilter(r.getFilteredKeywords()).addAllowList(words)).queue();

        } else if(action.equals("blacklist")) {

            r.getManager().setTriggerConfig(TriggerConfig.keywordFilter(words).addAllowList(r.getAllowlist())).queue();

        }

        AutoModRule rule = e.getGuild().retrieveAutoModRuleById(ruleId).complete();
        Collection<AutoModResponse> responses = rule.getActions();
        Collection<String> allowed = rule.getAllowlist();
        String creatorId = rule.getCreatorId();
        UserSnowflake creator = UserSnowflake.fromId(creatorId);
        AutoModEventType eventType = rule.getEventType();
        Collection<GuildChannel> exemptChannels = rule.getExemptChannels();
        Collection<Role> exemptRoles = rule.getExemptRoles();
        Collection<String> blockedKeywords = rule.getFilteredKeywords();
        int mentionLimit = rule.getMentionLimit();
        AutoModTriggerType triggerType = rule.getTriggerType();
        String name = rule.getName();
        boolean enabled = rule.isEnabled();
        String active = Emojis.no();
        if(enabled) active = Emojis.yes();
        boolean isMentionRaidProtectionEnabled = rule.isMentionRaidProtectionEnabled();
        OffsetDateTime timeCreated = rule.getTimeCreated();

        if(action.equals("whitelist")) allowed = words;
        if(action.equals("blacklist")) blockedKeywords = words;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.automod() + " │ AutoMod - Rules")
                .setDescription("Bearbeite oder lösche die AutoMod Rule mit der ID `"+rule.getId()+"`!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(g.getIconUrl())

                .addField(Emojis.pen()+" - Name", name, true)
                .addField(Emojis.idea()+" - ID", ruleId, true)
                .addField(Emojis.settings()+" - Type", triggerType.name(), true)
                .addBlankField(false)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+timeCreated.toInstant().getEpochSecond()+"> (<t:"+timeCreated.toInstant().getEpochSecond()+":R>)", true)
                .addField(Emojis.user()+" - Ersteller", creator.getAsMention(), true)
                .addField(Emojis.search()+" - Aktiviert", active, true);
        if(triggerType.equals(AutoModTriggerType.KEYWORD)) {
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            blockedKeywords.forEach(word->{
                sb.append(word).append(", ");
            });
            allowed.forEach(word->{
                sb2.append(word).append(", ");
            });
            if(sb.toString().equals("")) sb.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            if(sb2.toString().equals("")) sb2.append(Emojis.warning()+" *Es wurden keine Einträge gefunden*--");
            embed.addBlankField(false);
            embed.addField(Emojis.no()+" - Verbotene Wörter", sb.toString().substring(0, (sb.toString().length()-2)), false);
            embed.addField(Emojis.yes()+" - Erlaubte Wörter", sb2.toString().substring(0, (sb2.toString().length()-2)), false);
        }
        Button btn1 = Button.success("automod.rules.edit.btn."+rule.getId()+".blacklist", "Blacklist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn2 = Button.success("automod.rules.edit.btn."+rule.getId()+".whitelist", "Whitelist bearbeiten").withDisabled(!triggerType.equals(AutoModTriggerType.KEYWORD));
        Button btn3 = Button.success("automod.rules.edit.btn."+rule.getId()+".activate", "Aktivieren");
        if(enabled) btn3 = Button.danger("automod.rules.edit.btn."+rule.getId()+".deactivate", "Deaktivieren");
        e.editMessageEmbeds(embed.build()).setActionRow(btn1, btn2, btn3).queue();

    }

}
