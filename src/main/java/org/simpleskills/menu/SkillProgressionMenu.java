package org.simpleskills.menu;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

import java.util.UUID;

public class SkillProgressionMenu {
    private static final int COLS = 9;
    private static final int LINES = 6;
    private static final String PREFIX = "Progression: ";

    private static final int[] SLOTS = {
            0, 9, 18, 27, 36, 37,
            38, 29, 20, 11, 2, 3,
            4, 13, 22, 31, 40, 41,
            42, 33, 24, 15, 6, 7,
            8, 17, 26, 35, 44
    };

    private static final int SLOT_PREV = 48;
    private static final int SLOT_BUY = 49;
    private static final int SLOT_NEXT = 50;
    private static final int SLOT_TOGGLE = 52;
    private static final int SLOT_BACK = 45;

    public static void open(Player p, SkillDefinition d, SkillManager mgr) {
        open(p, d, mgr, 0);
    }

    public static void open(Player p, SkillDefinition d, SkillManager mgr, int page) {
        int max = d.getMaxLevel();
        int perPage = SLOTS.length;
        int total = (max + perPage - 1) / perPage;
        page = Math.max(0, Math.min(page, total - 1));

        Inventory inv = Bukkit.createInventory(null, COLS * LINES,
                PREFIX + d.getDisplay() + " (" + (page + 1) + "/" + total + ")");

        int playerLvl = mgr.getPlayerSkills(p).getLevel(d.getId());
        int startLevel = page * perPage + 1;

        for (int i = 0; i < perPage; i++) {
            int lvl = startLevel + i;
            if (lvl > max) break;

            Material mat = lvl < playerLvl
                    ? Material.GREEN_STAINED_GLASS_PANE
                    : lvl == playerLvl
                    ? Material.YELLOW_STAINED_GLASS_PANE
                    : Material.RED_STAINED_GLASS_PANE;

            ItemStack pane = new ItemStack(mat);
            ItemMeta pm = pane.getItemMeta();
            pm.setDisplayName((lvl <= playerLvl ? ChatColor.GREEN : ChatColor.RED)
                    + " Niveau " + lvl);
            pane.setItemMeta(pm);
            inv.setItem(SLOTS[i], pane);
        }

        // ✅ Item IA dynamique
        ItemStack buyItem;
        String iaId = d.getIcon();
        if (iaId != null) {
            CustomStack cs = CustomStack.getInstance(iaId);
            buyItem = (cs != null) ? cs.getItemStack() : new ItemStack(d.getMaterial());
        } else {
            buyItem = new ItemStack(d.getMaterial());
        }

        ItemMeta bm = buyItem.getItemMeta();
        if (playerLvl < max) {
            int cost = d.getCostForLevel(playerLvl + 1);
            bm.setDisplayName(ChatColor.AQUA + "Améliorer (" + cost + " pts)");
        } else {
            bm.setDisplayName(ChatColor.GRAY + "Niveau MAX");
        }
        buyItem.setItemMeta(bm);
        inv.setItem(SLOT_BUY, buyItem);

        if (page > 0) {
            inv.setItem(SLOT_PREV, createHeadWithTexture(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=",
                    ChatColor.YELLOW + "Page précédente"
            ));
        }
        if (page < total - 1) {
            inv.setItem(SLOT_NEXT, createHeadWithTexture(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=",
                    ChatColor.YELLOW + "Page suivante"
            ));
        }

        boolean isEnabled = mgr.getPlayerSkills(p).isSkillEnabled(d.getId());
        ItemStack toggle = new ItemStack(isEnabled ? Material.LIME_WOOL : Material.GRAY_WOOL);
        ItemMeta tm = toggle.getItemMeta();
        tm.setDisplayName(isEnabled ? ChatColor.GREEN + "Skill activé" : ChatColor.RED + "Skill désactivé");
        toggle.setItemMeta(tm);
        inv.setItem(SLOT_TOGGLE, toggle);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bmBack = back.getItemMeta();
        bmBack.setDisplayName(ChatColor.RED + "Retour");
        back.setItemMeta(bmBack);
        inv.setItem(SLOT_BACK, back);

        p.openInventory(inv);
    }

    public static boolean isOpen(InventoryView v) {
        return v.getTitle().startsWith(PREFIX);
    }

    public static void handleClick(int slot, Player p, SkillManager mgr) {
        InventoryView view = p.getOpenInventory();
        String title = view.getTitle().substring(PREFIX.length());
        String[] parts = title.split(" \\(");
        String display = parts[0].trim();
        int page = Integer.parseInt(parts[1].replace(")", "").split("/")[0]) - 1;

        SkillDefinition d = SkillRegistry.getAll().stream()
                .filter(def -> def.getDisplay().equals(display))
                .findFirst().orElse(null);
        if (d == null) return;

        int perPage = SLOTS.length;
        int total = (d.getMaxLevel() + perPage - 1) / perPage;

        if (slot == SLOT_PREV && page > 0) {
            open(p, d, mgr, page - 1);
            return;
        }
        if (slot == SLOT_NEXT && page < total - 1) {
            open(p, d, mgr, page + 1);
            return;
        }
        if (slot == SLOT_BUY) {
            if (mgr.increase(p, d)) {
                open(p, d, mgr, page);
            } else {
                p.sendMessage(ChatColor.RED + "Pas assez de points.");
            }
            return;
        }
        if (slot == SLOT_TOGGLE) {
            boolean current = mgr.getPlayerSkills(p).isSkillEnabled(d.getId());
            mgr.getPlayerSkills(p).setSkillEnabled(d.getId(), !current);
            mgr.addPoints(p, 0);
            mgr.recalculatePlayerAttributes(p, mgr.getPlayerSkills(p));
            open(p, d, mgr, page);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }
        if (slot == SLOT_BACK) {
            SkillsMenu.open(p);
        }
    }

    private static ItemStack createHeadWithTexture(String base64Texture, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(displayName);

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.getProperties().add(new ProfileProperty("textures", base64Texture));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }
}
