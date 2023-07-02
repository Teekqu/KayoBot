package events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

         try{
             ScheduledThreadPoolExecutor thread = new ScheduledThreadPoolExecutor(1);
             thread.scheduleWithFixedDelay(statusTask(e.getJDA()), 0, 1, TimeUnit.MINUTES);
         } catch (Exception err) {
             e.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("einen Bot"));
         }

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
