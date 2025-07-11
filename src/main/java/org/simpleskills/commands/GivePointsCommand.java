package org.simpleskills.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleskills.manager.SkillManager;

public class GivePointsCommand implements CommandExecutor {
    private final SkillManager skillManager;

    public GivePointsCommand(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /giveSkills <joueur> <points>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("Joueur introuvable.");
            return true;
        }

        int points;
        try {
            points = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Le nombre de points doit être un entier.");
            return true;
        }

        skillManager.load(target);
        skillManager.addPoints(target, points);
        sender.sendMessage("Vous avez donné " + points + " point(s) à " + target.getName() + ".");
        target.sendMessage("Vous avez reçu " + points + " point(s) de compétence.");

        return true;
    }
}
