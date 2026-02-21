package ru.guildforge.warps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import ru.guildforge.GuildForge;
import ru.guildforge.data.PlayerData;
import ru.guildforge.guilds.GuildType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final GuildForge plugin;
    private final Map<UUID, List<Warp>> playerWarps;
    private final Gson gson;
    private final File warpsFile;

    public WarpManager(GuildForge plugin) {
        this.plugin = plugin;
        this.playerWarps = new ConcurrentHashMap<>();
        this.gson = new Gson();
        this.warpsFile = new File(plugin.getDataFolder(), "warps.json");
        loadWarps();
    }

    /**
     * Создать новый варп
     */
    public boolean createWarp(Player player, String name) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);

        // Проверка на картографа (на всякий случай, хотя уже проверили в команде)
        if (data == null || data.getGuild() != GuildType.CARTOGRAPHER) {
            player.sendMessage("§cТолько картографы могут создавать варпы!");
            return false;
        }

        // Проверка на количество варпов
        int maxWarps = getMaxWarps(data);
        List<Warp> warps = playerWarps.getOrDefault(uuid, new ArrayList<>());

        if (warps.size() >= maxWarps) {
            player.sendMessage("§cВы достигли лимита варпов! Максимум: §e" + maxWarps);
            return false;
        }

        // Проверка на уникальность имени
        if (warps.stream().anyMatch(w -> w.getName().equalsIgnoreCase(name))) {
            player.sendMessage("§cВарп с таким именем уже существует!");
            return false;
        }

        // Создаем варп
        Warp warp = new Warp(name, player.getLocation(), System.currentTimeMillis());
        warps.add(warp);
        playerWarps.put(uuid, warps);

        saveWarps();
        player.sendMessage("§aВарп §e" + name + " §aуспешно создан! ("
                + warps.size() + "/" + maxWarps + ")");

        // Звуковой эффект
        player.getWorld().playSound(
                player.getLocation(),
                Sound.BLOCK_ENDER_CHEST_OPEN,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        return true;
    }

    /**
     * Телепортироваться к варпу (только для картографов)
     */
    public boolean teleportToWarp(Player player, String name) {
        UUID uuid = player.getUniqueId();

        // Дополнительная проверка (на случай если кто-то вызовет метод напрямую)
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);
        if (data == null || data.getGuild() != GuildType.CARTOGRAPHER) {
            player.sendMessage("§cТолько картографы могут использовать варпы!");
            return false;
        }

        List<Warp> warps = playerWarps.get(uuid);

        if (warps == null || warps.isEmpty()) {
            player.sendMessage("§cУ вас нет сохраненных варпов!");
            return false;
        }

        // Ищем варп по имени
        Warp warp = warps.stream()
                .filter(w -> w.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (warp == null) {
            player.sendMessage("§cВарп §e" + name + " §cне найден!");
            return false;
        }

        Location targetLocation = warp.getLocation();

        // Асинхронная телепортация для Folia
        player.teleportAsync(targetLocation).thenAccept(success -> {
            if (success) {
                // Телепортация успешна
                plugin.getTaskScheduler().runAtEntity(player, (taskPlayer) -> {
                    taskPlayer.sendMessage("§aТелепортация к варпу §e" + name + " §aвыполнена!");

                    taskPlayer.getWorld().playSound(
                            taskPlayer.getLocation(),
                            Sound.ENTITY_ENDERMAN_TELEPORT,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                });
            } else {
                player.sendMessage("§cОшибка телепортации! Локация недоступна.");
            }
        });

        return true;
    }

    /**
     * Удалить варп
     */
    public boolean deleteWarp(Player player, String name) {
        UUID uuid = player.getUniqueId();
        List<Warp> warps = playerWarps.get(uuid);

        if (warps == null || warps.isEmpty()) {
            player.sendMessage("§cУ вас нет сохраненных варпов!");
            return false;
        }

        boolean removed = warps.removeIf(w -> w.getName().equalsIgnoreCase(name));

        if (removed) {
            playerWarps.put(uuid, warps);
            saveWarps();
            player.sendMessage("§aВарп §e" + name + " §aудален!");

            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.BLOCK_ENDER_CHEST_CLOSE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );
            return true;
        } else {
            player.sendMessage("§cВарп §e" + name + " §cне найден!");
            return false;
        }
    }

    /**
     * Список варпов игрока
     */
    public List<Warp> getPlayerWarps(Player player) {
        return playerWarps.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * Показать список варпов
     */
    public void listWarps(Player player) {
        UUID uuid = player.getUniqueId();
        List<Warp> warps = playerWarps.get(uuid);
        PlayerData data = plugin.getGuildManager().getPlayerData(uuid);

        // Проверка на картографа
        if (data == null || data.getGuild() != GuildType.CARTOGRAPHER) {
            player.sendMessage("§cТолько картографы могут просматривать варпы!");
            return;
        }

        int maxWarps = getMaxWarps(data);

        player.sendMessage("§6=== Ваши варпы ===");

        if (warps == null || warps.isEmpty()) {
            player.sendMessage("§7У вас нет сохраненных варпов.");
            player.sendMessage("§7Используйте §e/warp create <название>§7 чтобы создать варп.");
        } else {
            player.sendMessage("§7Использовано: §e" + warps.size() + "§7/" + maxWarps);
            for (Warp warp : warps) {
                Location loc = warp.getLocation();
                player.sendMessage("§e" + warp.getName() + " §7- мир: §f"
                        + loc.getWorld().getName() + " §7X:§f" + loc.getBlockX()
                        + " Y:§f" + loc.getBlockY() + " Z:§f" + loc.getBlockZ());
            }
        }
        player.sendMessage("§7Команды: §e/warp create <название>§7, §e/warp <название>§7, §e/warp delete <название>");
    }

    /**
     * Получить максимальное количество варпов в зависимости от ранга
     */
    private int getMaxWarps(PlayerData data) {
        if (data.getGuild() != GuildType.CARTOGRAPHER) return 0;

        switch (data.getRank().getDisplayName()) {
            case "Землемер":
                return 3;
            case "Следопыт":
                return 4;
            case "Мастер-картограф":
                return 5;
            case "Хранитель Карт":
                return 6;
            default:
                return 3;
        }
    }
    /**
     * Сохранить варпы в файл
     */
    public void saveWarps() {
        try (FileWriter writer = new FileWriter(warpsFile)) {
            Map<UUID, List<WarpData>> saveData = new HashMap<>();

            for (Map.Entry<UUID, List<Warp>> entry : playerWarps.entrySet()) {
                List<WarpData> warpDataList = new ArrayList<>();
                for (Warp warp : entry.getValue()) {
                    warpDataList.add(new WarpData(
                            warp.getName(),
                            warp.getLocation().getWorld().getName(),
                            warp.getLocation().getX(),
                            warp.getLocation().getY(),
                            warp.getLocation().getZ(),
                            warp.getLocation().getYaw(),
                            warp.getLocation().getPitch(),
                            warp.getCreated()
                    ));
                }
                saveData.put(entry.getKey(), warpDataList);
            }

            gson.toJson(saveData, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения варпов: " + e.getMessage());
        }
    }

    /**
     * Загрузить варпы из файла
     */
    private void loadWarps() {
        if (!warpsFile.exists()) return;

        try (FileReader reader = new FileReader(warpsFile)) {
            Type type = new TypeToken<Map<UUID, List<WarpData>>>(){}.getType();
            Map<UUID, List<WarpData>> loadData = gson.fromJson(reader, type);

            if (loadData == null) return;

            for (Map.Entry<UUID, List<WarpData>> entry : loadData.entrySet()) {
                List<Warp> warps = new ArrayList<>();
                for (WarpData data : entry.getValue()) {
                    Location loc = new Location(
                            plugin.getServer().getWorld(data.world),
                            data.x, data.y, data.z,
                            data.yaw, data.pitch
                    );
                    warps.add(new Warp(data.name, loc, data.created));
                }
                playerWarps.put(entry.getKey(), warps);
            }

            plugin.getLogger().info("Загружено варпов для " + playerWarps.size() + " игроков");
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка загрузки варпов: " + e.getMessage());
        }
    }

    // ===== Внутренние классы =====

    /**
     * Класс варпа
     */
    public static class Warp {
        private final String name;
        private final Location location;
        private final long created;

        public Warp(String name, Location location, long created) {
            this.name = name;
            this.location = location;
            this.created = created;
        }

        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public long getCreated() {
            return created;
        }
    }

    /**
     * Класс для сохранения в JSON
     */
    private static class WarpData {
        String name;
        String world;
        double x, y, z;
        float yaw, pitch;
        long created;

        public WarpData(String name, String world, double x, double y, double z,
                        float yaw, float pitch, long created) {
            this.name = name;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.created = created;
        }
    }
}