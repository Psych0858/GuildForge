package ru.guildforge.guilds;

import java.util.HashMap;
import java.util.Map;

public enum Rank {
    // Кузнецы
    SMITH_APPRENTICE("Ученик", 0, 100, "Кузнецы"),
    SMITH_JOURNEYMAN("Подмастерье", 100, 500, "Кузнецы"),
    SMITH_MASTER("Мастер-Кузнец", 500, 1500, "Кузнецы"),
    SMITH_ELDER("Старейшина", 1500, Integer.MAX_VALUE, "Кузнецы"),

    // Алхимики
    ALCHEMIST_TRAINEE("Стажёр", 0, 100, "Алхимики"),
    ALCHEMIST_BREWER("Зельевар", 100, 500, "Алхимики"),
    ALCHEMIST_MASTER("Мастер-зелий", 500, 1500, "Алхимики"),
    ALCHEMIST_HIGH("Верховный Алхимик", 1500, Integer.MAX_VALUE, "Алхимики"),

    // Картографы
    CARTOGRAPHER_SURVEYOR("Землемер", 0, 100, "Картографы"),
    CARTOGRAPHER_PATHFINDER("Следопыт", 100, 500, "Картографы"),
    CARTOGRAPHER_MASTER("Мастер-картограф", 500, 1500, "Картографы"),
    CARTOGRAPHER_KEEPER("Хранитель Карт", 1500, Integer.MAX_VALUE, "Картографы"),

    // Фермеры
    FARMER_GARDENER("Садовод", 0, 100, "Фермеры"),
    FARMER_FARMER("Фермер", 100, 500, "Фермеры"),
    FARMER_MASTER("Мастер-урожая", 500, 1500, "Фермеры"),
    FARMER_KEEPER("Хранитель Сада", 1500, Integer.MAX_VALUE, "Фермеры"),

    // Шахтеры
    MINER_ORE("Рудокоп", 0, 100, "Шахтеры"),
    MINER_MINER("Горняк", 100, 500, "Шахтеры"),
    MINER_MASTER("Мастер-глубин", 500, 1500, "Шахтеры"),
    MINER_KEEPER("Хранитель Недр", 1500, Integer.MAX_VALUE, "Шахтеры");

    private final String displayName;
    private final int minContribution;
    private final int maxContribution;
    private final String guildName;

    Rank(String displayName, int minContribution, int maxContribution, String guildName) {
        this.displayName = displayName;
        this.minContribution = minContribution;
        this.maxContribution = maxContribution;
        this.guildName = guildName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinContribution() {
        return minContribution;
    }

    public int getMaxContribution() {
        return maxContribution;
    }

    public String getGuildName() {
        return guildName;
    }

    public static Rank getRank(GuildType guild, int contribution) {
        String guildName = guild.getDisplayName();

        for (Rank rank : values()) {
            if (rank.getGuildName().equals(guildName) &&
                    contribution >= rank.getMinContribution() &&
                    contribution < rank.getMaxContribution()) {
                return rank;
            }
        }

        // Возвращаем начальный ранг по умолчанию
        switch (guild) {
            case SMITH: return SMITH_APPRENTICE;
            case ALCHEMIST: return ALCHEMIST_TRAINEE;
            case CARTOGRAPHER: return CARTOGRAPHER_SURVEYOR;
            case FARMER: return FARMER_GARDENER;
            case MINER: return MINER_ORE;
            default: return SMITH_APPRENTICE;
        }
    }

    public boolean isMaster() {
        return this.name().contains("MASTER") || this.name().contains("HIGH") ||
                this.name().contains("KEEPER") || this.name().contains("ELDER");
    }
}