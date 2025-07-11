package org.simpleskills.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.menu.SkillsMenu;

public class SkillsMenuCommand implements CommandExecutor {
    private final SkillManager skillManager;

    public SkillsMenuCommand(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }
        Player player = (Player) sender;
        skillManager.load(player);
        // Ouvre le menu principal
        SkillsMenu.open(player);
        return true;
    }
}
