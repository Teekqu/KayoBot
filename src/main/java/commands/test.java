package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class test extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent e) {

        if(!e.getMessage().getContentRaw().equals("!test")) return;

        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File("C:\\Users\\Home\\OneDrive\\Bilder\\server-info-vorlage.png"));
        } catch (IOException err) {
            err.printStackTrace();
        }

        if (inputImage != null) {
            BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, null);


            Font font = new Font("Arial", Font.BOLD, 70);
            g2d.setFont(font);
            g2d.setColor(Color.WHITE);
            g2d.drawString(e.getGuild().getName(), 113, 650);
            g2d.drawString(String.valueOf(e.getGuild().getMembers().size()), 1078, 650);
            g2d.dispose();

            try {
                File outputImageFile = new File("server-info.png");
                ImageIO.write(outputImage, "png", outputImageFile);
                e.getMessage().replyFiles(FileUpload.fromData(outputImageFile)).queue();
                System.out.println("Bild mit Text erfolgreich erstellt!");
            } catch (IOException err) {
                err.printStackTrace();
            }
        } else {
            System.out.println("Fehler beim Laden des Eingangsbildes!");
        }

    }

}
