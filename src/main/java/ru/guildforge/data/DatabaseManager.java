package ru.guildforge.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.guilds.GuildType;

import java.io.File;
import java.sql.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final GuildForge plugin;
    private Connection connection;
    private final Gson gson = new Gson();
    private final Type cooldownType = new TypeToken<Map<String, Long>>(){}.getType();

    public DatabaseManager(GuildForge plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // ПРОСТОЕ ПОДКЛЮЧЕНИЕ БЕЗ JDBC КЛАССА
            String url = "jdbc:sqlite:" + new File(dataFolder, "guildforge.db").getAbsolutePath();
            connection = DriverManager.getConnection(url);

            createTables();
            plugin.getLogger().info("✅ Подключение к SQLite установлено");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createPlayerDataTable = """
            CREATE TABLE IF NOT EXISTS player_data (
                uuid VARCHAR(36) PRIMARY KEY,
                guild VARCHAR(20),
                contribution INT DEFAULT 0,
                cooldowns TEXT DEFAULT '{}',
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerDataTable);
            plugin.getLogger().info("✅ Таблицы созданы/проверены");
        }
    }

    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM player_data WHERE uuid = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String guildStr = rs.getString("guild");
                    GuildType guild = guildStr != null ? GuildType.valueOf(guildStr) : null;
                    int contribution = rs.getInt("contribution");
                    String cooldownsJson = rs.getString("cooldowns");

                    Map<String, Long> cooldowns = gson.fromJson(cooldownsJson, cooldownType);
                    if (cooldowns == null) cooldowns = new HashMap<>();

                    return new PlayerData(uuid, guild, contribution, cooldowns);
                } else {
                    PlayerData newData = new PlayerData(uuid);
                    savePlayerData(newData).join(); // Сохраняем нового игрока
                    return newData;
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка загрузки данных игрока " + uuid + ": " + e.getMessage());
                return new PlayerData(uuid);
            }
        });
    }

    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String query = """
                INSERT OR REPLACE INTO player_data (uuid, guild, contribution, cooldowns) 
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, data.getPlayerUUID().toString());
                stmt.setString(2, data.getGuild() != null ? data.getGuild().name() : null);
                stmt.setInt(3, data.getContribution());
                stmt.setString(4, gson.toJson(data.getCooldowns()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка сохранения данных игрока " + data.getPlayerUUID() + ": " + e.getMessage());
            }
        });
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("✅ Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия соединения с БД: " + e.getMessage());
        }
    }

    public void saveAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getGuildManager().getPlayerData(player.getUniqueId());
            if (data != null) {
                savePlayerData(data).join();
            }
        }
        plugin.getLogger().info("✅ Данные всех игроков сохранены");
    }
}