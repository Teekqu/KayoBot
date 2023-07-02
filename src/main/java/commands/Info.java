package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Info extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("info")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        

    }

}
