package utils;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private String ip;
    private int port;
    private String database;
    private String username;
    private String password;
    private int maxConnections;

    private HikariDataSource dataSource;

    public Database(String ip, int port, String database, String username, String password, int maxConnections) {
        this.ip = ip;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
    }

    public void connect() {
        this.dataSource = new HikariDataSource();
        this.dataSource.setJdbcUrl("jdbc:mariadb://" + ip + ":" + port + "/" + database);
        this.dataSource.setUsername(this.username);
        this.dataSource.setPassword(this.password);
        this.dataSource.setMaximumPoolSize(this.maxConnections);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        try (Connection conn = this.dataSource.getConnection()) {
            Statement stm = conn.createStatement();
            return stm.executeQuery(sql);
        }
    }

    public boolean execute(String sql) throws SQLException {
        try (Connection conn = this.dataSource.getConnection()) {
            Statement stm = conn.createStatement();
            return stm.execute(sql);
        }
    }

    public void close() {
        this.dataSource.close();
    }

}
