package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.KUser;

public class setupStuff extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent e) {

        if(!e.getMessage().getContentRaw().equals("!setup")) return;
        if(e.getMember() == null) return;

        if(!e.getMember().hasPermission(Permission.ADMINISTRATOR)) return;

        Button btn1 = Button.success("beta.btn.sendSuggestion.de", "Einsenden").withEmoji(Emoji.fromFormatted("<:german:1125047342479970334>"));

        e.getMessage().getChannel().sendMessage("⠀").addActionRow(btn1).queue();

    }

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().startsWith("beta.btn.sendSuggestion.")) return;

        String lang = e.getButton().getId().substring(24);

        TextInput ti1;
        TextInput ti2;
        if(lang.equals("de")) {
            ti1 = TextInput.create("beta.suggestion.modal.name", "System Name", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(75)
                    .setPlaceholder("Mein tolles System!")
                    .build();
            ti2 = TextInput.create("beta.suggestion.modal.description", "System Beschreibung", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(50)
                    .setMaxLength(3000)
                    .setPlaceholder("Mein tolles System ist dafür da, dass... [...] Er sollte ..., ..., ... können!")
                    .build();
        } else {
            ti1 = TextInput.create("beta.suggestion.modal.name", "System name", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(75)
                    .setPlaceholder("My great system!")
                    .build();
            ti2 = TextInput.create("beta.suggestion.modal.description", "System description", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(50)
                    .setMaxLength(3000)
                    .setPlaceholder("My great system is there for that.... [...] It should ..., ..., ... can!")
                    .build();
        }
        String title = "Kayo | System einsenden";
        if(lang.equals("en")) title = "Kayo | Submit system";
        Modal modal = Modal.create("beta.suggestion.modal."+lang, title)
                .addActionRow(ti1)
                .addActionRow(ti2)
                .build();

        e.replyModal(modal).queue();

    }

    public void onModalInteraction(ModalInteractionEvent e) {

        if(!e.getModalId().startsWith("beta.suggestion.modal.")) return;
        String lang = e.getModalId().substring(22);

        KUser u = new KUser(e.getUser());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Vorschlag von "+u.getUsername())
                .setDescription("**Name:** "+e.getValue("beta.suggestion.modal.name").getAsString()+"\n**Description:**\n"+e.getValue("beta.suggestion.modal.description").getAsString())
                .setColor(utils.Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .setFooter(e.getGuild().getName(), e.getGuild().getIconUrl());

        e.getGuild().getTextChannelById("1125044707614015538").sendMessageEmbeds(embed.build()).queue();

        String text = "<:k_yes:1125021009876090880> | **Erfolgreich, dein Vorschlag wurde ans Team weitergeleitet!**";
        if(lang.equals("en")) text = "<:k_yes:1125021009876090880> | **Successful, your suggestion was forwarded to the team!**";
        e.reply(text).setEphemeral(true).queue();

    }

}
