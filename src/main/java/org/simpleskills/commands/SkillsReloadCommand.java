// src/main/java/org/simpleskills/commands/SkillsReloadCommand.java
package org.simpleskills.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleskills.SimpleSkillsPlugin;
import org.simpleskills.manager.AttributeListener;
import org.simpleskills.registry.SkillRegistry;
import org.simpleskills.upgrade.UpgradeRewardManager;

public class SkillsReloadCommand implements CommandExecutor {
    private final SimpleSkillsPlugin plugin;

    public SkillsReloadCommand(SimpleSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 1) reload config + registries
        plugin.reloadConfig();
        SkillRegistry.reload(plugin);
        UpgradeRewardManager.reload(plugin);

        // 2) reapply attributes to all online players
        AttributeListener listener = plugin.getAttributeListener();
        for (Player p : Bukkit.getOnlinePlayers()) {
            listener.applyAllAttributes(p);
        }

        sender.sendMessage("§aSimpleSkills rechargé, attributs réappliqués.");
        return true;
    }
}
