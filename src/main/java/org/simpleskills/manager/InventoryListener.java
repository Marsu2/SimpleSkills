// src/main/java/org/simpleskills/manager/InventoryListener.java
package org.simpleskills.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.simpleskills.menu.SkillProgressionMenu;
import org.simpleskills.menu.SkillsMenu;
import org.simpleskills.model.SkillDefinition;

public class InventoryListener implements Listener {
    private final SkillManager manager;

    public InventoryListener(SkillManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        try {
            String title = e.getView().getTitle();
            int rawSlot = e.getRawSlot();
           // Bukkit.getLogger().info("DEBUG InventoryClickEvent: title='" + title + "', rawSlot=" + rawSlot);

            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();

            // Menu principal (/skills)
            if (SkillsMenu.isOpen(e.getView())) {
                e.setCancelled(true);
                SkillDefinition def = SkillsMenu.getDefinitionFromItem(e.getCurrentItem());
              //  Bukkit.getLogger().info("DEBUG in SkillsMenu, clicked: " + def);
                if (def != null) {
                    SkillProgressionMenu.open(p, def, manager);
                }
                return;
            }

            // Menu de progression
            if (SkillProgressionMenu.isOpen(e.getView())) {
                e.setCancelled(true);
               // Bukkit.getLogger().info("DEBUG in SkillProgressionMenu, delegating to handleClick");
                SkillProgressionMenu.handleClick(rawSlot, p, manager);
            }
        } catch (Throwable t) {
           // Bukkit.getLogger().severe("Error in InventoryListener: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
