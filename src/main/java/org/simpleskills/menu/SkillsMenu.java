package org.simpleskills.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.lone.itemsadder.api.CustomStack;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.SimpleSkillsPlugin;
import org.simpleskills.manager.SkillManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillsMenu {
    private static final String TITLE = "Vos Skills";

    public static void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        Map<String, Integer> skillSlots = Map.of(
                "strength", 11,
                "agility", 15,
                "vigor", 22,
                "wealth", 29,
                "minage", 33
        );

        SkillManager skillManager = SimpleSkillsPlugin.getSkillManager();
        PlayerSkills data = skillManager.getPlayerSkills(p);
        int totalLevel = skillManager.getTotalLevel(data);
        int skillPoints = data.getPoints();

        // Tête du joueur
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(p);
        meta.setDisplayName(ChatColor.AQUA + "Profil de " + p.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Points disponibles : " + ChatColor.GREEN + skillPoints);
        lore.add(ChatColor.GOLD + "Niveaux de skills : " + ChatColor.GREEN + totalLevel);
        meta.setLore(lore);
        skull.setItemMeta(meta);
        inv.setItem(4, skull);

        // Affichage des skills
        for (Map.Entry<String, Integer> entry : skillSlots.entrySet()) {
            String key = entry.getKey();
            int slot = entry.getValue();

            SkillDefinition def = SkillRegistry.get(key);
            if (def == null) continue;

            ItemStack item;
            String iaId = def.getIcon();
            if (iaId != null) {
                CustomStack custom = CustomStack.getInstance(iaId);
                item = (custom != null) ? custom.getItemStack() : new ItemStack(def.getMaterial());
            } else {
                item = new ItemStack(def.getMaterial());
            }

            ItemMeta m = item.getItemMeta();
            m.setDisplayName(ChatColor.GOLD + def.getDisplay());

            List<String> itemlore = new ArrayList<>();
            itemlore.add(ChatColor.WHITE + "Niveaux: 1–" + def.getMaxLevel());
            List<String> descLines = def.getDescriptionForLevel(1);
            for (String line : descLines) {
                itemlore.add(ChatColor.GRAY + line);
            }
            itemlore.add("");
            itemlore.add(ChatColor.YELLOW + "Cliquez pour voir la progression");

            m.setLore(itemlore);
            item.setItemMeta(m);

            inv.setItem(slot, item);
        }

        p.openInventory(inv);
    }

    public static boolean isOpen(InventoryView view) {
        return TITLE.equals(view.getTitle());
    }

    public static SkillDefinition getDefinitionFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return SkillRegistry.getAll().stream()
                .filter(def -> def.getDisplay().equals(display))
                .findFirst().orElse(null);
    }
}
