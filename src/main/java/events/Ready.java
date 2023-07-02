package events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import utils.KUser;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Ready extends ListenerAdapter {

    public void onReady(ReadyEvent e) {

        KUser u = new KUser(e.getJDA().getSelfUser());

        System.out.println("----- [ SYSTEM ] -----");
        System.out.println("Bot: "+u.getName()+" ("+u.getUsername()+")");
        System.out.println("Server: "+e.getJDA().getGuilds().size());
        System.out.println("Commands: "+e.getJDA().retrieveCommands().complete().size());
        System.out.println("----- [ SYSTEM ] -----");

        loadCommands();

         try{
             ScheduledThreadPoolExecutor thread = new ScheduledThreadPoolExecutor(1);
             thread.scheduleWithFixedDelay(statusTask(e.getJDA()), 0, 1, TimeUnit.MINUTES);
         } catch (Exception err) {
             e.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("einen Bot"));
         }

    }

    private void loadCommands() {

        JDA jda = Kayo.Kayo.getJda();
        Guild g = jda.getGuildById("820343748244144169");

        g.upsertCommand("info", "Siehe Informationen über den Bot an").setGuildOnly(true).queue();
        g.upsertCommand("add-emoji", "Füge ein Emoji zum Server hinzu")
                .addOption(OptionType.STRING, "emoji", "Gebe ein Emoji an", true)
                .addOption(OptionType.STRING, "name", "Gebe einen Namen für das Emoji an", false)
                .setGuildOnly(true).queue();

    }

    private Runnable statusTask(JDA jda) {
        return () -> {

            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("auf "+jda.getGuilds().size()+" Servern"));
            try { Thread.sleep(60000); } catch (Exception ignored) { }
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("den Chef"));
            try { Thread.sleep(60000); } catch (Exception ignored) { }
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("mit "+jda.getUsers().size()+" Usern"));
            try { Thread.sleep(60000); } catch (Exception ignored) { }
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("mit Rechten rum"));
            try { Thread.sleep(60000); } catch (Exception ignored) { }
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("mit Slash Commands"));
            try { Thread.sleep(60000); } catch (Exception ignored) { }

        };
    }

}
