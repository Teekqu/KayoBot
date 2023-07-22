package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.Emojis;
import utils.Get;
import utils.KGuild;
import utils.KUser;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Ping extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("ping")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        Button btn = Button.primary("ping.btn.loading", Emoji.fromFormatted(Emojis.load())).withDisabled(true);
        InteractionHook ih = e.reply(Emojis.clock()+" | *Der Ping wird berechnet...*").setActionRow(btn).setEphemeral(true).complete();
        try { Thread.sleep(1000); } catch (Exception ignored) { }

        String states = "🟢 Ausgezeichnet";
        String states2 = "🟢 Ausgezeichnet";

        long msg = e.getJDA().getRestPing().complete();
        long api = e.getJDA().getGatewayPing();

        if (msg > 70) states = "🟢 Gut";
        if (msg > 170) states = "🟡 Nicht schlecht";
        if (msg > 350) states = "🔴 Sehr schlecht";

        if (api > 70) states2 = "🟢 Gut";
        if (api > 170) states2 = "🟡 Nicht schlecht";
        if (api > 350) states2 = "🔴 Sehr schlecht";


        EmbedBuilder pingEmbed = new EmbedBuilder()
                .setTitle(Emojis.pin()+" │ **Ping**")
                .setDescription("**Pong 🏓 !**")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(e.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField(Emojis.clock()+" - Message", msg+"ms | "+states, false)
                .addField(Emojis.earth()+" - API", api+"ms | "+states2, false);

        btn = Button.success("ping.btn.loading", Emoji.fromFormatted(Emojis.yes())).withDisabled(true);
        ih.editOriginal(" ").setEmbeds(pingEmbed.build()).setActionRow(btn).queue();

    }

}
