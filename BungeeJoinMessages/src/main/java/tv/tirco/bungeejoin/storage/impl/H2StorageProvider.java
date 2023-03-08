package tv.tirco.bungeejoin.storage.impl;

import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.storage.StorageHandler;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class H2StorageProvider implements StorageHandler {
    private Connection connection;
    private boolean enabled = false;
    private final File dbFile = new File(Main.getInstance().getDataFolder(), "data.db");

    @Override
    public void init(Main plugin) {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("H2 Driver not found! First time joins will not be tracked!");
            enabled = false;
            return;
        }
        enabled = true;
        try {
            getConnection();
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to connect to H2 database! First time joins will not be tracked!");
            enabled = false;
            throw new RuntimeException(e);
        }
        String sql = "CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), firstjoin BIGINT, lastjoin BIGINT)";
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to create H2 players database table!");
            enabled = false;
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            return DriverManager.getConnection(
                    "jdbc:h2:" + dbFile.getAbsolutePath()
            );
        }
        return connection;
    }

    @Override
    public void close(Main plugin) {
        enabled = false;
        try {
            if (connection != null && !connection.isClosed()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    plugin.getLogger().info("Failed to close H2 database connection!");
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().info("Failed to close H2 database connection!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesPlayerExist(UUID uuid) {
        if (!enabled) {
            return false;
        }
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().info("Failed to check if player exists in H2 database!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createPlayer(UUID uuid, String name) {
        if (!enabled) {
            return;
        }
        String sql = "INSERT INTO players (uuid, name, firstjoin, lastjoin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setLong(3, System.currentTimeMillis());
            statement.setLong(4, System.currentTimeMillis());
            statement.execute();
        } catch (SQLException e) {
            Main.getInstance().getLogger().info("Failed to create player in H2 database!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLastJoin(UUID uuid, long time) {
        if (!enabled) return;
        String sql = "UPDATE players SET lastjoin = ? WHERE uuid = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, time);
            statement.setString(2, uuid.toString());
            statement.execute();
        } catch (SQLException e) {
            Main.getInstance().getLogger().info("Failed to update last join time in H2 database!");
            throw new RuntimeException(e);
        }
    }
}
