package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.Emojis;
import utils.Get;
import utils.KGuild;
import utils.KUser;

public class GuildRemove extends ListenerAdapter {

    public void onGuildLeave(GuildLeaveEvent e) {

        KGuild g = new KGuild(e.getGuild());

        int requiredBoosts = 0;
        if(g.getBoostTier().getKey()==0) requiredBoosts = 2;
        if(g.getBoostTier().getKey()==1) requiredBoosts = 7;
        if(g.getBoostTier().getKey()==2) requiredBoosts = 14;
        if(g.getBoostTier().getKey()==3) requiredBoosts = 14;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.join()+" │ **Server verlassen**")
                .setDescription("Ich wurde von einem Server gekickt!")
                .setColor(Get.embedColor(false))
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(e.getGuild().getName(), e.getGuild().getIconUrl())

                .addBlankField(false)
                .addField(Emojis.pen()+" - Name", g.getName(), true)
                .addField(Emojis.idea()+" - ID", g.getId(), true)
                .addField(Emojis.crone()+" - Owner", g.getOwner().getAsMention()+" ("+(new KUser(g.getOwner().getUser()).getUsername())+")", true)
                .addBlankField(false)
                .addField(Emojis.user()+" - User", String.valueOf(g.getUserCount()), true)
                .addField(Emojis.bot()+" - Bots", String.valueOf(g.getBotCount()), true)
                .addField(Emojis.boost()+" - Boosts", g.getBoostCount()+"/"+requiredBoosts+" (Level "+g.getBoostTier().getKey()+")", true)
                .addBlankField(false)
                .addField(Emojis.channel()+" - Kanäle", String.valueOf(g.getChannels().size()), true)
                .addField(Emojis.role()+" - Rollen", String.valueOf(g.getRoles().size()), true)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+g.getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+g.getTimeCreated().toInstant().getEpochSecond()+":R>)", true);

        String url = g.getIconUrl();
        if(url == null) url = "https://i.timo1005.de/Kayo.png";
        Button btn1 = Button.link(url, "Icon URL").withEmoji(Emoji.fromFormatted(Emojis.link())).withDisabled(url.equals("https://i.timo1005.de/Kayo.png"));
        try { Kayo.Kayo.getJda().getTextChannelById("1130521728225910874").sendMessageEmbeds(embed.build()).setActionRow(btn1).queue(); } catch (Exception ignored) { }

        g.getAutoDelete().forEach(map -> {
            g.removeAutoDelete((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });
        g.getAutoReact().forEach(g::removeAutoReact);
        g.getJoinRolesList().forEach(g::removeJoinRole);
        g.getLogging().forEach(map -> {
            g.removeLogging((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });
        g.getWelcomeMessages().forEach(map -> {
            g.removeWelcomeMessage((GuildMessageChannel) e.getGuild().getGuildChannelById(map.get("channelId")));
        });
        g.getTempChannels().forEach(g::removeTempChannel);
        g.getJoinHubs().forEach(g::removeJoinHub);

        sendFeedbackQuestion(g);

    }

    private void sendFeedbackQuestion(KGuild g) {

        User user = Kayo.Kayo.getJda().getUserById(g.getOwnerIdLong());
        if(user != null) {

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(Emojis.vote()+" | **Wir brauchen deine Meinung!**")
                    .setDescription("Guten Tag "+ user.getAsMention() +",\n\rwir haben soeben mitbekommen, dass Sie Kayo von Ihrem Server entfernt haben,\ndies kann viele individuelle Gründe haben, die wir alle akzeptieren.\n\rJedoch würden wir uns sehr über Ihre Rückmeldung freuen, wo das Problem lag,\ndamit wir Kayo weiterhin verbessern können, um mehr Leuten Zufriedenheit zu garantieren.\n\r> **Mit freundlichen Grüßen**\n> ~ **"+Emojis.kayo()+" Entwicklungsteam**")
                    .setColor(Get.embedColor())
                    .setThumbnail(g.getIconUrl())
                    .setFooter(null, "https://i.timo1005.de/Kayo.png")
                    .setTimestamp(TimeFormat.RELATIVE.now().toInstant());

            Button btn = Button.primary("guild.leave.btn.review."+g.getId()+"."+g.getName().replaceAll("\\.", "[point]"), "Bewerten").withEmoji(Emoji.fromFormatted(Emojis.vote()));
            try { user.openPrivateChannel().complete().sendMessageEmbeds(embed.build()).setActionRow(btn).queue(); } catch (Exception ignored) { }

        }

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("guild.leave.btn.review.")) return;

        TextInput system = TextInput.create("system", "Was am Bot hat dir nicht gefallen?", TextInputStyle.PARAGRAPH)
                .setPlaceholder("TempChannels, AutoDelete, Logging, ...")
                .setMinLength(2)
                .setMaxLength(250)
                .setRequired(true)
                .build();
        TextInput description = TextInput.create("description", "Gebe uns genauere Informationen zum Problem.", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Es gab einen Bug, Mir hat das Design nicht gefallen, ...")
                .setMinLength(10)
                .setMaxLength(3000)
                .setRequired(false)
                .build();
        Modal modal = Modal.create("guild.leave.modal."+e.getButton().getId().split("\\.")[4]+"."+e.getButton().getId().split("\\.")[5], "Was hat dir an Kayo nicht gefallen?")
                .addActionRow(system)
                .addActionRow(description)
                .build();

        e.replyModal(modal).queue();

    }

    public void onModalInteraction(ModalInteractionEvent e) {

        if(!e.getModalId().startsWith("guild.leave.modal.")) return;

        String system = e.getValue("system").getAsString();
        String description = "*Keine Rückmeldung*";
        if(e.getValue("description")!=null) description = e.getValue("description").getAsString();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.vote() + " | **Neue Bewertung**")
                .setDescription("**User:** "+e.getUser().getName()+"\n**Server:** "+e.getModalId().split("\\.")[4]+" ("+e.getModalId().split("\\.")[3]+")\n\r> **Was am Bot hat dir nicht gefallen?**\n"+system+"\n\r> **Gebe uns genauere Informationen zum Problem.**\n"+description)
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(e.getUser().getEffectiveAvatarUrl())
                .setFooter("Bewertet über guild.leave.modal", e.getJDA().getSelfUser().getEffectiveAvatarUrl());
        e.getJDA().getGuildById("1124803921978871850").getTextChannelById("1185900962603347989").sendMessageEmbeds(embed.build()).queue();
        e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setActionRow(Button.primary("guild.leave.btn.review.123.123", "Bewerten").withEmoji(Emoji.fromFormatted(Emojis.vote())).withDisabled(true)).queue();

        e.reply(Emojis.yes()+" | **Vielen Dank für deine Bewertung!**\n"+Emojis.yes()+" | **Die Bewertung wurde an das Entwicklungsteam weitergeleitet.**").setEphemeral(true).queue();

    }

}
