package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQL {

    private static final String ip = "37.114.32.239";
    private static final String port = "3306";
    private static final String db = "Kayo";
    private static final String user = "Kayo";
    private static final String password = "v[EkUI9KjV4fgcZv";

    public static Statement connect() {

        try {
            Connection con = DriverManager.getConnection("jdbc:mariadb://" + ip + ":" + port + "/" + db, user, password);
            return con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
