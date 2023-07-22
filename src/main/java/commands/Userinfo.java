package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import utils.Emojis;
import utils.Get;
import utils.KGuild;
import utils.KUser;

import java.util.Collection;

public class Userinfo extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {

        if(!e.getName().equals("userinfo")) return;
        if(e.getGuild() == null || e.getMember() == null) return;

        KGuild g = new KGuild(e.getGuild());
        KUser u = new KUser(e.getUser());

        InteractionHook ih = e.deferReply(false).complete();

        Member m = e.getMember();
        if(e.getOption("user") != null) m = e.getOption("user").getAsMember();
        if(m == null) m = e.getMember();

        KUser mU = new KUser(m.getUser());


        // VARIABLES
        String nick = m.getNickname();
        if(nick == null) nick = "None";
        String booster = Emojis.no();
        if(g.getBoosters().contains(m)) booster = Emojis.yes()+" (<t:"+m.getTimeBoosted().toInstant().getEpochSecond()+":R>)";
        String badges = this.getBadgeEmojisFromUser(mU);


        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Emojis.user()+" │ **User - Informationen**")
                .setDescription("In diesem Menü findest du aktuelle Informationen über den User!")
                .setColor(Get.embedColor())
                .setTimestamp(TimeFormat.RELATIVE.now().toInstant())
                .setFooter(u.getUsername(), u.getEffectiveAvatarUrl())
                .setThumbnail(m.getEffectiveAvatarUrl())

                .addField(Emojis.crone()+" - Name", mU.getName(), true)
                .addField(Emojis.idea()+" - ID", mU.getId(), true)
                .addField(Emojis.pen()+" - Nickname", nick, true)
                .addBlankField(false)
                .addField(Emojis.clock()+" - Erstellt", "<t:"+mU.getTimeCreated().toInstant().getEpochSecond()+"> (<t:"+mU.getTimeCreated().toInstant().getEpochSecond()+":R>)", true)
                .addField(Emojis.join()+" - Beigetreten", "<t:"+m.getTimeJoined().toInstant().getEpochSecond()+"> (<t:"+m.getTimeJoined().toInstant().getEpochSecond()+":R>)", true)
                .addBlankField(false)
                .addField(Emojis.awardcup()+" - Badges", badges, true)
                .addField(Emojis.boost()+" - Booster", booster, true);

        String bannerUrl = mU.getProfile().getBannerUrl();
        if(bannerUrl == null) bannerUrl = "https://kayo.timo1005.de/";
        Button btn1 = Button.link(bannerUrl, "Banner").withEmoji(Emoji.fromFormatted(Emojis.link())).withDisabled(bannerUrl.equals("https://kayo.timo1005.de/"));
        Button btn2 = Button.link(mU.getEffectiveAvatarUrl(), "Avatar").withEmoji(Emoji.fromFormatted(Emojis.link()));

        ih.editOriginalEmbeds(embed.build()).setActionRow(btn1, btn2).queue();

    }

    private String getBadgeEmojisFromUser(KUser u) {

        Collection<User.UserFlag> flags = u.getUserFlags();

        StringBuilder sb = new StringBuilder();
        for(User.UserFlag flag : flags) {

            if(flag.equals(User.UserFlag.STAFF)) {
                sb.append(Emojis.staff());
            } else if(flag.equals(User.UserFlag.PARTNER)) {
                sb.append(Emojis.partnerBadge());
            } else if(flag.equals(User.UserFlag.HYPESQUAD)) {
                sb.append(Emojis.hypesquad());
            } else if(flag.equals(User.UserFlag.BUG_HUNTER_LEVEL_1)) {
                sb.append(Emojis.bugHunter());
            } else if(flag.equals(User.UserFlag.BUG_HUNTER_LEVEL_2)) {
                sb.append(Emojis.bugHunterGold());
            } else if(flag.equals(User.UserFlag.HYPESQUAD_BRAVERY)) {
                sb.append(Emojis.bravery());
            } else if(flag.equals(User.UserFlag.HYPESQUAD_BRILLIANCE)) {
                sb.append(Emojis.brilliance());
            } else if(flag.equals(User.UserFlag.HYPESQUAD_BALANCE)) {
                sb.append(Emojis.balance());
            } else if(flag.equals(User.UserFlag.EARLY_SUPPORTER)) {
                sb.append(Emojis.earlySup());
            } else if(flag.equals(User.UserFlag.VERIFIED_BOT)) {
                sb.append(Emojis.verifiedBot());
            } else if(flag.equals(User.UserFlag.VERIFIED_DEVELOPER)) {
                sb.append(Emojis.verifiedDev());
            } else if(flag.equals(User.UserFlag.CERTIFIED_MODERATOR)) {
                sb.append(Emojis.modAlumni());
            } else if(flag.equals(User.UserFlag.BOT_HTTP_INTERACTIONS)) {
                sb.append(Emojis.slashBadge());
            } else if(flag.equals(User.UserFlag.ACTIVE_DEVELOPER)) {
                sb.append(Emojis.activeDev());
            }

        }

        return sb.toString();

    }

}
