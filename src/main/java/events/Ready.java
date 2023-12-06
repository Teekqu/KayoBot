package events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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

         Kayo.Kayo.isReady = true;

    }

    private void loadCommands() {

        JDA jda = Kayo.Kayo.getJda();
        Guild g = jda.getGuildById("1132022985209954415");
        if(g == null) return;

        /* COMMANDS */
        jda.upsertCommand("info", "Siehe Informationen über den Bot an").setGuildOnly(true).queue();
        jda.upsertCommand("add-emoji", "Füge ein Emoji zum Server hinzu")
                .addOption(OptionType.STRING, "emoji", "Gebe ein Emoji an", true)
                .addOption(OptionType.STRING, "name", "Gebe einen Namen für das Emoji an", false)
                .setGuildOnly(true).queue();
        jda.upsertCommand("serverinfo", "Siehe Informationen über den Server an").setGuildOnly(true).queue();
        jda.upsertCommand("userinfo", "Siehe Informationen über einen User an")
                .addOption(OptionType.USER, "user", "Wähle einen User", false)
                .setGuildOnly(true).queue();

        /* FUNCTIONS */
        OptionData createautodeletedata1 = new OptionData(OptionType.CHANNEL, "channel", "Wähle einen Kanal", true)
                .setChannelTypes(ChannelType.TEXT);
        OptionData createautodeletedata2 = new OptionData(OptionType.INTEGER, "time", "Gebe eine Zeit an", true);
        OptionData createautodeletedata3 = new OptionData(OptionType.STRING, "unit", "Wähle eine Einheit", true)
                .addChoice("Sekunde/n", "s")
                .addChoice("Minute/n", "m")
                .addChoice("Stunde/n", "h")
                .addChoice("Tag/e", "d");
        SubcommandData createautodeletecmd = new SubcommandData("add", "Füge einen AutoDelete Kanal hinzu")
                .addOptions(createautodeletedata1, createautodeletedata2, createautodeletedata3);
        SubcommandData showautodeletecmd = new SubcommandData("show", "Siehe alle AutoDelete Channel und bearbeiten diese");
        jda.upsertCommand("autodelete", "Verwalte das AutoDelete System")
                .addSubcommands(createautodeletecmd, showautodeletecmd)
                .setGuildOnly(true).queue();

        OptionData createautoreactdata1 = new OptionData(OptionType.CHANNEL, "channel", "Wähle einen Kanal", true)
                .setChannelTypes(ChannelType.TEXT);
        OptionData createautoreactdata2 = new OptionData(OptionType.STRING, "emoji", "Gebe ein Emoji an", true);
        SubcommandData createautoreactcmd = new SubcommandData("add", "Füge AutoReact zu einem Kanal hinzu")
                .addOptions(createautoreactdata1, createautoreactdata2);
        SubcommandData showautoreactcmd = new SubcommandData("show", "Siehe alle AutoReact Channel und bearbeite diese");
        jda.upsertCommand("autoreact", "Verwalte das AutoReact System")
                .addSubcommands(createautoreactcmd, showautoreactcmd)
                .setGuildOnly(true).queue();

        OptionData createjoinroledata = new OptionData(OptionType.ROLE, "role", "Wähle eine Rolle", true);
        SubcommandData createjoinrolecmd = new SubcommandData("add", "Füge eine JoinRole hinzu").addOptions(createjoinroledata);
        jda.upsertCommand("joinrole", "Verwalte das JoinRole System")
                .addSubcommands(createjoinrolecmd)
                .setGuildOnly(true).queue();
        jda.upsertCommand("joinroles", "Siehe alle JoinRoles und bearbeite diese")
                .setGuildOnly(true).queue();


        SubcommandData automodrulescmd = new SubcommandData("rules", "Verwalte die AutoMod Regeln");
        jda.upsertCommand("automod", "Verwalte das AutoMod System von Discord").addSubcommands(automodrulescmd).setGuildOnly(true).queue();

        OptionData welcomeadddata1 = new OptionData(OptionType.CHANNEL, "channel", "Wähle einen Channel", true).setChannelTypes(ChannelType.TEXT);
        SubcommandData welcomeaddcmd = new SubcommandData("add", "Füge eine neue WelcomeMessage hinzu").addOptions(welcomeadddata1);
        SubcommandData welcomeshowcmd = new SubcommandData("show", "Siehe alle WelcomeMessages an und bearbeite diese");
        SubcommandData welcomevariablescmd = new SubcommandData("variables", "Siehe alle Verfügbaren Variablen an");
        jda.upsertCommand("welcome", "Verwalte das Welcome System")
                .addSubcommands(welcomeaddcmd, welcomeshowcmd, welcomevariablescmd)
                .setGuildOnly(true).queue();

        OptionData loggingadddata = new OptionData(OptionType.CHANNEL, "channel", "Wähle einen Channel", true).setChannelTypes(ChannelType.TEXT);
        SubcommandData loggingaddcmd = new SubcommandData("add", "Füge einen neuen Logging Kanal hinzu").addOptions(loggingadddata);
        SubcommandData loggingshowcmd = new SubcommandData("show", "Siehe alle Logging Kanäle");
        jda.upsertCommand("logging", "Verwalte das Logging System")
                .addSubcommands(loggingaddcmd, loggingshowcmd)
                .setGuildOnly(true).queue();

        jda.upsertCommand("ping", "Pong! Zeige den Ping des Bots an").setGuildOnly(true).queue();

        OptionData createjoinhubdata1 = new OptionData(OptionType.CHANNEL, "channel", "Wähle einen VoiceChannel", true).setChannelTypes(ChannelType.VOICE);
        OptionData createjoinhubdata2 = new OptionData(OptionType.CHANNEL, "category", "Wähle eine Kategorie", false).setChannelTypes(ChannelType.CATEGORY);
        SubcommandData joinhubaddcmd = new SubcommandData("add", "Füge ein JoinHub hinzu").addOptions(createjoinhubdata1, createjoinhubdata2);
        SubcommandData joinhubvariablescmd = new SubcommandData("variables", "Siehe alle JoinHub Variablen");
        jda.upsertCommand("joinhub", "Verwalte die JoinHubs des Servers").addSubcommands(joinhubaddcmd, joinhubvariablescmd).setGuildOnly(true).queue();
        jda.upsertCommand("joinhubs", "Siehe die JoinHubs des Servers").setGuildOnly(true).queue();

        SubcommandData tempchannelnamecmd = new SubcommandData("name", "Verwalte den TempChannel Namen").addOption(OptionType.STRING, "name", "Gebe den neuen Namen an", true);
        SubcommandData tempchannellimitcmd = new SubcommandData("limit", "Verwalte das TempChannel Limit").addOption(OptionType.INTEGER, "limit", "Gebe das neue Limit an", true);
        SubcommandData tempchannellockcmd = new SubcommandData("lock", "Sperre deinen TempChannel");
        SubcommandData tempchannelunlockcmd = new SubcommandData("unlock", "Entsperre deinen TempChannel");
        SubcommandData tempchannelhidecmd = new SubcommandData("hide", "Verstecke deinen TempChannel");
        SubcommandData tempchannelunhide = new SubcommandData("unhide", "Mache deinen TempChannel wieder sichtbar");
        SubcommandData tempchanneladdmodcmd = new SubcommandData("add-mod", "Füge einen TempChannel Mod hinzu").addOption(OptionType.USER, "user", "Wähle einen User", true);
        SubcommandData tempchannelremovemodcmd = new SubcommandData("remove-mod", "Entferne einen TempChannel Mod").addOption(OptionType.USER, "user", "Wähle einen User", true);
        SubcommandData tempchanneltransfercmd = new SubcommandData("transfer", "Übertrage die Eigentumsrechte des TempChannels").addOption(OptionType.USER, "user", "Wähle einen User", true);
        SubcommandData tempchannelkickcmd = new SubcommandData("kick", "Kick einen User aus dem TempChannel").addOption(OptionType.MENTIONABLE, "user", "Wähle einen User", true);
        SubcommandData tempchannelbancmd = new SubcommandData("ban", "Banne einen User aus dem TempChannel").addOption(OptionType.USER, "user", "Wähle einen User", true);
        SubcommandData tempchannelunbancmd = new SubcommandData("unban", "Entbanne einen User aus dem TempChannel").addOption(OptionType.USER, "user", "Wähle einen User", true);
        jda.upsertCommand("tempchannel", "Verwalte deinen TempChannel")
                .addSubcommands(tempchannelnamecmd, tempchannellimitcmd, tempchannellockcmd, tempchannelunlockcmd, tempchannelhidecmd, tempchannelunhide, tempchanneladdmodcmd, tempchannelremovemodcmd, tempchanneltransfercmd, tempchannelkickcmd, tempchannelbancmd, tempchannelunbancmd)
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
