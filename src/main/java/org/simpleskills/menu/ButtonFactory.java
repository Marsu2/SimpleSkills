// src/main/java/org/simpleskills/menu/ButtonFactory.java
package org.simpleskills.menu;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.simpleskills.model.SkillDefinition;

import java.util.ArrayList;
import java.util.List;

public class ButtonFactory {

    /**
     * Crée un ItemStack pour un skill donné, affichant :
     * - le nom du skill
     * - le niveau courant / niveau max
     * - le coût du prochain niveau (si disponible)
     * - la description formatée pour le niveau courant
     *
     * @param def     la définition du skill
     * @param current le niveau actuel du joueur pour ce skill
     * @param cost    le coût calculé pour passer au prochain niveau
     */
    public static ItemStack create(SkillDefinition def, int current, int cost) {
        ItemStack item = new ItemStack(def.getMaterial());
        ItemMeta meta = item.getItemMeta();

        // Titre
        meta.setDisplayName(ChatColor.GOLD + def.getDisplay());

        // Lore
        List<String> lore = new ArrayList<>();
        // Niveau courant / max
        lore.add(ChatColor.WHITE + "Niveau: " +
                ChatColor.GREEN + current +
                ChatColor.GRAY + "/" + def.getMaxLevel());
        // Coût du prochain niveau, si pas déjà au max
        if (current < def.getMaxLevel()) {
            lore.add(ChatColor.WHITE + "Coût: " +
                    ChatColor.RED + cost + " pts");
        }
        lore.add(""); // ligne vide avant la description

        // Description contextualisée pour le niveau courant
        for (String line : def.getDescriptionForLevel(current)) {
            lore.add(ChatColor.GRAY + line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
