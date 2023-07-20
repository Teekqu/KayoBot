package functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.checkerframework.checker.units.qual.K;
import utils.Embeds;
import utils.Emojis;
import utils.Get;
import utils.KUser;

import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VoteReminder extends ListenerAdapter {

    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(!e.getButton().getId().equals("topgg.vote.reminder")) return;

        KUser u = new KUser(e.getUser());

        if(Get.voteReminders(false).contains(e.getUser())) {
            e.replyEmbeds(Embeds.error(u, "Du hast den VoteReminder bereits aktiviert")).setEphemeral(true).queue();
            return;
        }

        u.enableVoteReminder((u.getLastVote()+43200));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.yes() + " │ Erfolgreich!")
                .setDescription("Du wirst informiert, sobald du erneut voten kannst!")
                .setColor(Get.embedColor(true))
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl());
        Button btn1 = Button.success("topgg.vote.reminder", "Erinnerung").withEmoji(Emoji.fromFormatted(Emojis.clock())).withDisabled(true);
        e.editMessageEmbeds(e.getMessage().getEmbeds()).setActionRow(btn1).queue();
        e.getChannel().sendMessageEmbeds(embed.build()).queue();

    }

    public void onReady(ReadyEvent e) {

        try{
            ScheduledThreadPoolExecutor thread = new ScheduledThreadPoolExecutor(1);
            thread.scheduleWithFixedDelay(voteReminder(), 0, 5, TimeUnit.MINUTES);
        } catch (Exception ignored) { }

    }

    private Runnable voteReminder() {
        return () -> {

            Collection<User> users = Get.voteReminders(true);
            for(User user : users) {

                KUser u = new KUser(user);
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(Emojis.vote()+" | **Vote Reminder**")
                        .setDescription("Du kannst erneut für mich voten!")
                        .setColor(Get.embedColor())
                        .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                        .setFooter(u.getUsername())
                        .setThumbnail(u.getEffectiveAvatarUrl());
                Button btn = Button.link(Get.topggVoteLink(), "Voten").withEmoji(Emoji.fromFormatted(Emojis.vote()));
                try {
                    u.getUser().openPrivateChannel().complete().sendMessageEmbeds(embed.build()).setActionRow(btn).queue();
                } catch (Exception ignored) { }
                boolean success = u.disableVoteReminder();
                if(!success) {
                    System.out.println("Fehler beim deaktivieren des Vote Reminders ("+u.getUsername()+" | "+u.getId()+")");
                }

            }

        };
    }

}
