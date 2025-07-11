// src/main/java/org/simpleskills/menu/LevelPaneFactory.java
package org.simpleskills.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Crée un glass pane pour un niveau donné.
 */
public class LevelPaneFactory {
    /**
     * @param level  numéro du niveau
     * @param achieved si le joueur a déjà atteint ce niveau
     */
    public static ItemStack create(int level, boolean achieved) {
        Material mat = achieved
                ? Material.GREEN_STAINED_GLASS_PANE
                : Material.RED_STAINED_GLASS_PANE;

        ItemStack pane = new ItemStack(mat);
        ItemMeta meta = pane.getItemMeta();
        String color = achieved ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
        meta.setDisplayName(color + "Niveau " + level);
        pane.setItemMeta(meta);
        return pane;
    }
}
