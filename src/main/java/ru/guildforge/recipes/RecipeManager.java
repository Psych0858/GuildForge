package ru.guildforge.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import ru.guildforge.GuildForge;

public class RecipeManager {
    private final GuildForge plugin;

    public RecipeManager(GuildForge plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        // Удаляем ванильный рецепт починки (если нужно)
        removeVanillaMending();

        // Регистрируем эксклюзивные рецепты
        registerExclusiveRecipes();
    }

    private void removeVanillaMending() {
        // Удаление ванильных рецептов с починкой
        // Временно отключено для совместимости
    }

    private void registerExclusiveRecipes() {
        // Здесь будут регистрироваться особые рецепты
        // Например, рецепт книги починки только для кузнецов
    }

    public void addRecipe(Recipe recipe) {
        if (recipe instanceof Keyed) {
            NamespacedKey key = ((Keyed) recipe).getKey();
            if (Bukkit.getRecipe(key) == null) {
                Bukkit.addRecipe(recipe);
            }
        }
    }
}