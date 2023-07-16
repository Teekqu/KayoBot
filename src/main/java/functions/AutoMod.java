package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.automod.build.AutoModRuleData;
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import utils.Emojis;
import utils.KGuild;
import utils.KUser;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AutoMod extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("automod")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

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

            StringSelectMenu sm = StringSelectMenu.create("automod.rules.edit")
                    .setPlaceholder("Wähle eine AutoMod Rule")
                    .setMinValues(1)
                    .setMaxValues(1)
                    .addOptions(options)
                    .build();

            e.reply(sb.toString()).setEphemeral(true).setActionRow(sm).queue();

        }

    }

    public void onStringSelectInteraction(StringSelectInteractionEvent e) {

        if(!e.getInteraction().getSelectedOptions().get(0).getValue().startsWith("automod.rules.edit.")) return;
        if(e.getGuild()==null || e.getMember()==null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        String id = e.getInteraction().getSelectedOptions().get(0).getValue().split("\\.")[3];
        AutoModRule r = e.getGuild().retrieveAutoModRuleById(id).complete();

        Collection<AutoModResponse> responses = r.getActions();
        Collection<String> allowed = r.getAllowlist();
        String creatorId = r.getCreatorId();
        AutoModEventType eventType = r.getEventType();
        Collection<GuildChannel> exemptChannels = r.getExemptChannels();
        Collection<Role> exemptRoles = r.getExemptRoles();
        Collection<String> blockedKeywords = r.getFilteredKeywords();
        int mentionLimit = r.getMentionLimit();
        AutoModTriggerType triggerType = r.getTriggerType();
        String name = r.getName();
        boolean enabled = r.isEnabled();
        boolean isMentionRaidProtectionEnabled = r.isMentionRaidProtectionEnabled();
        OffsetDateTime timeCreated = r.getTimeCreated();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.automod() + " │ AutoMod - Rules")
                .setDescription("Bearbeite oder lösche die AutoMod Rule mit der ID "+r.getId());

    }

}
