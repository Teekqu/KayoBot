package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Emojis;
import utils.KGuild;
import utils.KUser;

public class Serverinfo extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("serverinfo")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.stats()+" | **"+g.getName()+"**")
                .setDescription("");

    }

}
