package com.borekkst.armshands;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final ArmsHands plugin;
    private Connection connection;

    public DatabaseManager(ArmsHands plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        if (!plugin.getConfig().getBoolean("settings.enable-sqlite-database", true)) return;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        File dbFile = new File(dataFolder, "database.db");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS armor_stands (id INTEGER PRIMARY KEY AUTOINCREMENT, world TEXT, x REAL, y REAL, z REAL)");
            }
            plugin.getLogger().info("SQLite подключена!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка SQLite: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
