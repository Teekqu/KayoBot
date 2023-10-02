package utils;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.UUID;

public class Config {

    public Config() {

        File file = new File("./config.json");
        if(!file.exists()) {

            Scanner scanner = new Scanner(System.in);
            boolean check;
            String result;

            String token;

            System.out.print("Please enter the discord bot token: ");
            result = scanner.next();
            token = result;

            try {
                boolean success = file.createNewFile();
                if(!success) return;

                FileWriter writer = new FileWriter("./config.json");
                writer.write("{\"token\": \""+token+"\"}");
                writer.close();

            } catch (Exception err) {
                err.printStackTrace();
                return;
            }

        }

    }

    public Object get(String key) {
        try {
            Scanner scanner = new Scanner(new FileInputStream(new File("./config.json")));

            StringBuilder sb = new StringBuilder();

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line);
            }

            JSONObject jo = new JSONObject(sb.toString());
            return jo.get(key);

        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

}
