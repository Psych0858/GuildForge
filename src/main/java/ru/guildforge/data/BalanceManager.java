package ru.guildforge.data;

import ru.guildforge.guilds.GuildType;
import ru.guildforge.guilds.Rank;
import java.util.HashMap;
import java.util.Map;

public class BalanceManager {
    private final Map<GuildType, Map<String, Integer>> contributionValues = new HashMap<>();
    private final Map<GuildType, Map<Rank, Integer>> exclusivePrices = new HashMap<>();

    // Средний заработок в час по гильдиям
    private final Map<GuildType, Integer> hourlyIncome = new HashMap<>();

    public BalanceManager() {
        loadDefaultValues();
        calculateHourlyIncome();
    }

    private void loadDefaultValues() {
        // Базовые значения Contribution за действия
        Map<String, Integer> minerValues = new HashMap<>();
        minerValues.put("COAL_ORE", 1);
        minerValues.put("IRON_ORE", 2);
        minerValues.put("GOLD_ORE", 3);
        minerValues.put("DIAMOND_ORE", 5);
        minerValues.put("DEEPSLATE_COAL_ORE", 2);
        minerValues.put("DEEPSLATE_IRON_ORE", 3);
        minerValues.put("DEEPSLATE_GOLD_ORE", 4);
        minerValues.put("DEEPSLATE_DIAMOND_ORE", 6);
        minerValues.put("ANCIENT_DEBRIS", 10);
        contributionValues.put(GuildType.MINER, minerValues);

        Map<String, Integer> farmerValues = new HashMap<>();
        farmerValues.put("WHEAT", 1);
        farmerValues.put("CARROTS", 1);
        farmerValues.put("POTATOES", 1);
        farmerValues.put("BEETROOTS", 1);
        farmerValues.put("SWEET_BERRY_BUSH", 2);
        farmerValues.put("COCOA", 2);
        farmerValues.put("NETHER_WART", 3);
        contributionValues.put(GuildType.FARMER, farmerValues);

        // Цены на эксклюзивные предметы (сбалансированные) для каждой гильдии

        // Кузнецы
        Map<Rank, Integer> smithPrices = new HashMap<>();
        smithPrices.put(Rank.SMITH_APPRENTICE, 2000);
        smithPrices.put(Rank.SMITH_JOURNEYMAN, 3000);
        smithPrices.put(Rank.SMITH_MASTER, 4000);
        smithPrices.put(Rank.SMITH_ELDER, 5000);
        exclusivePrices.put(GuildType.SMITH, smithPrices);

        // Алхимики
        Map<Rank, Integer> alchemistPrices = new HashMap<>();
        alchemistPrices.put(Rank.ALCHEMIST_TRAINEE, 4000);
        alchemistPrices.put(Rank.ALCHEMIST_BREWER, 6000);
        alchemistPrices.put(Rank.ALCHEMIST_MASTER, 8000);
        alchemistPrices.put(Rank.ALCHEMIST_HIGH, 10000);
        exclusivePrices.put(GuildType.ALCHEMIST, alchemistPrices);

        // Картографы
        Map<Rank, Integer> cartographerPrices = new HashMap<>();
        cartographerPrices.put(Rank.CARTOGRAPHER_SURVEYOR, 3000);
        cartographerPrices.put(Rank.CARTOGRAPHER_PATHFINDER, 4500);
        cartographerPrices.put(Rank.CARTOGRAPHER_MASTER, 6000);
        cartographerPrices.put(Rank.CARTOGRAPHER_KEEPER, 7500);
        exclusivePrices.put(GuildType.CARTOGRAPHER, cartographerPrices);

        // Фермеры
        Map<Rank, Integer> farmerPrices = new HashMap<>();
        farmerPrices.put(Rank.FARMER_GARDENER, 2500);
        farmerPrices.put(Rank.FARMER_FARMER, 3500);
        farmerPrices.put(Rank.FARMER_MASTER, 4500);
        farmerPrices.put(Rank.FARMER_KEEPER, 5500);
        exclusivePrices.put(GuildType.FARMER, farmerPrices);

        // Шахтеры
        Map<Rank, Integer> minerPrices = new HashMap<>();
        minerPrices.put(Rank.MINER_ORE, 3500);
        minerPrices.put(Rank.MINER_MINER, 5000);
        minerPrices.put(Rank.MINER_MASTER, 6500);
        minerPrices.put(Rank.MINER_KEEPER, 8000);
        exclusivePrices.put(GuildType.MINER, minerPrices);
    }

    private void calculateHourlyIncome() {
        // Примерный расчёт: игрок добывает X ресурсов в час
        hourlyIncome.put(GuildType.MINER, 150);  // 150 руд/час
        hourlyIncome.put(GuildType.FARMER, 200); // 200 урожая/час
        hourlyIncome.put(GuildType.SMITH, 80);   // 80 крафтов/час
        hourlyIncome.put(GuildType.CARTOGRAPHER, 100); // 100 чанков/час
        hourlyIncome.put(GuildType.ALCHEMIST, 60); // 60 зелий/час
    }

    /**
     * Получить количество часов для накопления на эксклюзивный предмет
     */
    public int getRequiredHours(GuildType guild, Rank rank) {
        int price = getPriceForRank(guild, rank);
        int income = hourlyIncome.getOrDefault(guild, 100);
        return price / income;
    }

    /**
     * Проверить, сбалансирована ли цена (не более 20 часов)
     */
    public boolean isPriceBalanced(GuildType guild, Rank rank) {
        int hours = getRequiredHours(guild, rank);
        return hours <= 20;
    }

    /**
     * Получить цену для конкретного ранга
     */
    public int getPriceForRank(GuildType guild, Rank rank) {
        Map<Rank, Integer> prices = exclusivePrices.get(guild);
        if (prices == null) return 5000; // Значение по умолчанию

        // Ищем точное совпадение ранга
        if (prices.containsKey(rank)) {
            return prices.get(rank);
        }

        // Если точного нет, берём максимальную цену для этой гильдии
        return prices.values().stream().max(Integer::compareTo).orElse(5000);
    }

    /**
     * Получить Contribution за блок
     */
    public int getContributionForBlock(GuildType guild, String blockType) {
        Map<String, Integer> values = contributionValues.get(guild);
        if (values == null) return 0;
        return values.getOrDefault(blockType, 0);
    }

    /**
     * Рекомендация по настройке цены
     */
    public String getPriceRecommendation(GuildType guild) {
        int income = hourlyIncome.getOrDefault(guild, 100);
        int recommendedPrice = income * 10; // 10 часов игры

        return String.format(
                "§7Рекомендуемая цена для §e%s§7: §6%d §7(%d часов)",
                guild.getDisplayName(),
                recommendedPrice,
                recommendedPrice / income
        );
    }
}