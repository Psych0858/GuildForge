package ru.guildforge;

import org.bukkit.plugin.java.JavaPlugin;
import ru.guildforge.api.GuildForgeAPI;
import ru.guildforge.api.GuildForgeAPIImpl;
import ru.guildforge.commands.GuildCommand;
import ru.guildforge.commands.GuildAdminCommand;
import ru.guildforge.commands.WarpCommand;
import ru.guildforge.data.DatabaseManager;
import ru.guildforge.listeners.GUIListener;
import ru.guildforge.guilds.GuildManager;
import ru.guildforge.guilds.AlchemistEffect;
import ru.guildforge.hooks.PlaceholderAPIHook;
import ru.guildforge.listeners.*;
import ru.guildforge.metrics.Metrics;
import ru.guildforge.recipes.RecipeManager;
import ru.guildforge.tasks.FoliaTaskScheduler;
import ru.guildforge.utils.ConfigManager;
import ru.guildforge.utils.MessageManager;
import ru.guildforge.utils.CooldownManager;
import ru.guildforge.warps.WarpManager;

public class GuildForge extends JavaPlugin {

    private static GuildForge instance;
    private static GuildForgeAPI api;

    private DatabaseManager databaseManager;
    private GuildManager guildManager;
    private WarpManager warpManager;
    private RecipeManager recipeManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private CooldownManager cooldownManager;
    private FoliaTaskScheduler taskScheduler;
    private Metrics metrics;

    private GuildForgeAPIImpl apiImpl;

    @Override
    public void onEnable() {
        instance = this;

        // Инициализация менеджеров
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.taskScheduler = new FoliaTaskScheduler(this);
        this.cooldownManager = new CooldownManager(this);

        // Загрузка конфигов
        configManager.loadConfigs();

        // Подключение к БД
        this.databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        // Инициализация гильдий и варпов
        this.guildManager = new GuildManager(this);
        this.warpManager = new WarpManager(this);

        // Инициализация API
        this.apiImpl = new GuildForgeAPIImpl(this);
        api = this.apiImpl;

        // Регистрация команд
        if (getCommand("guild") != null) {
            getCommand("guild").setExecutor(new GuildCommand(this));
        }
        if (getCommand("guildadmin") != null) {
            getCommand("guildadmin").setExecutor(new GuildAdminCommand(this));
        }
        if (getCommand("warp") != null) {
            getCommand("warp").setExecutor(new WarpCommand(this));
        }

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new GuildListener(this), this);
        getServer().getPluginManager().registerEvents(new VanillaBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestLootListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AlchemistEffect(this), this);
        getServer().getPluginManager().registerEvents(new BrewingListener(this), this);
        getServer().getPluginManager().registerEvents(new ExclusiveItemListener(this), this);

        // Рецепты
        this.recipeManager = new RecipeManager(this);
        recipeManager.registerRecipes();

        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI hook registered!");
        }

        // Метрики
        try {
            this.metrics = new Metrics(this, 12345);
        } catch (Exception e) {
            getLogger().warning("Metrics not available: " + e.getMessage());
        }

        getLogger().info("GuildForge v" + getDescription().getVersion() + " успешно запущен!");
        getLogger().info("GuildForge API доступен для аддонов!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        if (warpManager != null) {
            warpManager.saveWarps();
        }
        if (guildManager != null) {
            guildManager.saveAllData();
        }
        getLogger().info("GuildForge остановлен.");
    }

    public static GuildForge getInstance() {
        return instance;
    }

    public static GuildForgeAPI getAPI() {
        return api;
    }

    // Геттеры
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public GuildManager getGuildManager() { return guildManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public FoliaTaskScheduler getTaskScheduler() { return taskScheduler; }

    public GuildForgeAPIImpl getAPIImpl() { return apiImpl; }
}