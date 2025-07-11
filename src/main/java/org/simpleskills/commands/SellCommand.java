package org.simpleskills.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleskills.util.MoneyUtil;

public class SellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can sell.");
            return true;
        }
        Player player = (Player) sender;
        double baseGain = calculateSaleValue(player);  // Implement your shop logic
        double finalGain = MoneyUtil.applyWealthBoost(player, baseGain);
        // deposit via your economy plugin, e.g.:
        // economy.depositPlayer(player, finalGain);
        player.sendMessage("You sold items for " + finalGain + "$");
        return true;
    }

    private double calculateSaleValue(Player player) {
        // Placeholder: calculate the actual sale value from shop
        return 100.0;
    }
}
