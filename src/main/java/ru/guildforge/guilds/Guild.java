package ru.guildforge.guilds;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class Guild {
    private final GuildType type;
    private final String displayName;
    private final ChatColor color;
    private final Material icon;
    private final List<String> mechanics;
    private final ExclusiveItem exclusiveItem;

    public Guild(GuildType type) {
        this.type = type;
        this.displayName = type.getDisplayName();
        this.color = type.getColor();
        this.mechanics = new ArrayList<>();

        // Настройка иконки и механик в зависимости от типа
        switch (type) {
            case SMITH:
                this.icon = Material.ANVIL;
                this.mechanics.add("repair_without_loss");
                this.mechanics.add("reforge_items");
                this.exclusiveItem = new ExclusiveItem("Книга Починки", 1, 50, 5000);
                break;

            case ALCHEMIST:
                this.icon = Material.BREWING_STAND;
                this.mechanics.add("longer_potions");
                this.mechanics.add("triple_brew");
                this.exclusiveItem = new ExclusiveItem("Вечный Флакон", 3, 100, 10000);
                break;

            case CARTOGRAPHER:
                this.icon = Material.MAP;
                this.mechanics.add("warps");
                this.mechanics.add("treasure_maps");
                this.exclusiveItem = new ExclusiveItem("Карта Возвращения", 2, 50, 10000);
                break;

            case FARMER:
                this.icon = Material.WHEAT;
                this.mechanics.add("fast_growth");
                this.mechanics.add("fast_breeding");
                this.exclusiveItem = new ExclusiveItem("Золотое Яблоко Изобилия", 3, 30, 8000);
                break;

            case MINER:
                this.icon = Material.DIAMOND_PICKAXE;
                this.mechanics.add("extra_ore_chance");
                this.mechanics.add("hammer_3x3");
                this.exclusiveItem = new ExclusiveItem("Сердце Горы", 7, 100, 20000);
                break;

            default:
                this.icon = Material.BARRIER;
                this.exclusiveItem = null;
                break;
        }
    }

    public GuildType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getMechanics() {
        return mechanics;
    }

    public boolean hasMechanic(String mechanic) {
        return mechanics.contains(mechanic);
    }

    public ExclusiveItem getExclusiveItem() {
        return exclusiveItem;
    }

    /**
     * Внутренний класс для эксклюзивного предмета
     */
    public static class ExclusiveItem {
        private final String name;
        private final int cooldownDays;
        private final int requiredLevel;
        private final int requiredContribution;

        public ExclusiveItem(String name, int cooldownDays, int requiredLevel, int requiredContribution) {
            this.name = name;
            this.cooldownDays = cooldownDays;
            this.requiredLevel = requiredLevel;
            this.requiredContribution = requiredContribution;
        }

        public String getName() {
            return name;
        }

        public int getCooldownDays() {
            return cooldownDays;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public int getRequiredContribution() {
            return requiredContribution;
        }
    }
}