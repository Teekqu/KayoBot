package functions;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.KGuild;
import utils.KUser;

public class AutoMod extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("automod")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KUser u = new KUser(e.getUser());
        KGuild g = new KGuild(e.getGuild());

        

    }

}
