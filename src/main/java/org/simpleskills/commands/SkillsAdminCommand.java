// src/main/java/org/simpleskills/commands/SkillsAdminCommand.java
package org.simpleskills.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;
import org.simpleskills.SimpleSkillsPlugin;

import java.util.ArrayList;
import java.util.List;

public class SkillsAdminCommand implements CommandExecutor {
    private final SkillManager manager;

    public SkillsAdminCommand(SkillManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simpleskills.admin")) {
            sender.sendMessage(ChatColor.RED + "Permission refusée.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        if ("see".equals(sub)) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin see <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
            manager.load(target);
            PlayerSkills skills = manager.getPlayerSkills(target);
            sender.sendMessage(ChatColor.GOLD + "Compétences de " + target.getName() + ":");
            for (SkillDefinition def : SkillRegistry.getAll()) {
                int lvl = skills.getLevel(def.getId());
                sender.sendMessage(ChatColor.YELLOW + "- " + def.getDisplay() + ": " + ChatColor.GREEN + lvl);
            }
            sender.sendMessage(ChatColor.GRAY + "Points restants: " + ChatColor.AQUA + skills.getPoints());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin <subcommand> <player> [args]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return true;
        }

        // load then get
        manager.load(target);
        PlayerSkills skills = manager.getPlayerSkills(target);

        switch (sub) {
            case "resetlevel":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin resetlevel <player> <skillId|all>");
                    return true;
                }
                if (args[2].equalsIgnoreCase("all")) {
                    for (SkillDefinition def : SkillRegistry.getAll()) {
                        skills.setLevel(def.getId(), 0);
                    }
                    manager.saveAll();
                    manager.load(target);
                    SimpleSkillsPlugin.getAttributeListener().applyAllAttributes(target);
                    sender.sendMessage("Tous les niveaux de " + target.getName() + " ont été réinitialisés.");
                } else {
                    String id = args[2].toLowerCase();
                    SkillDefinition def = SkillRegistry.get(id);
                    if (def == null) {
                        sender.sendMessage(ChatColor.RED + "Skill non trouvé : " + args[2]);
                        return true;
                    }
                    skills.setLevel(def.getId(), 0);
                    manager.saveAll();
                    manager.load(target);
                    sender.sendMessage("Le skill " + def.getDisplay() + " de " +
                            target.getName() + " a été réinitialisé au niveau " +
                            skills.getLevel(def.getId()) + ".");
                }
                break;

            case "removelevel":
                // Usage : /skillsadmin removelevel <player> <skillId|all> <amount>
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin removelevel <player> <skillId|all> <amount>");
                    return true;
                }

                String targetSkill = args[2].toLowerCase();
                int amountToRemove;
                try {
                    amountToRemove = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Le montant doit être un entier.");
                    return true;
                }
                if (amountToRemove <= 0) {
                    sender.sendMessage(ChatColor.RED + "Le montant doit être strictement positif.");
                    return true;
                }

                if (targetSkill.equals("all")) {
                    // retire sur tous les skills, comme avant
                    int remaining = amountToRemove;
                    for (SkillDefinition def : SkillRegistry.getAll()) {
                        int lvl = skills.getLevel(def.getId());
                        if (lvl > 0 && remaining > 0) {
                            int toRemove = Math.min(lvl, remaining);
                            skills.setLevel(def.getId(), lvl - toRemove);
                            remaining -= toRemove;
                        }
                    }
                    sender.sendMessage("Retiré jusqu'à " + amountToRemove + " niveaux de tous les skills de " + target.getName() + ".");
                } else {
                    // retire seulement sur le skill spécifié
                    SkillDefinition def = SkillRegistry.get(targetSkill);
                    if (def == null) {
                        sender.sendMessage(ChatColor.RED + "Skill introuvable : " + args[2]);
                        return true;
                    }
                    int lvl = skills.getLevel(def.getId());
                    int actualRemoved = Math.min(lvl, amountToRemove);
                    skills.setLevel(def.getId(), lvl - actualRemoved);
                    sender.sendMessage("Retiré " + actualRemoved + " niveau(s) de " + def.getDisplay() + " pour " + target.getName() + ".");
                }

                // sauvegarde, reload et réapplique
                manager.saveAll();
                manager.load(target);
                SimpleSkillsPlugin.getAttributeListener().applyAllAttributes(target);
                break;

            case "removepoints":
                int amount;
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin removepoints <player> <amount>");
                    return true;
                }
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Le nombre doit être un entier.");
                    return true;
                }
                int current = skills.getPoints();
                skills.setPoints(Math.max(0, current - amount));
                manager.saveAll();
                manager.load(target);
                sender.sendMessage(amount + " points de compétences retirés de " + target.getName() + ".");
                sender.sendMessage(target.getName() + " est désormais à " + skills.getPoints() + " points.");
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Commandes disponibles:");
        sender.sendMessage(ChatColor.GRAY + "/skillsadmin resetlevel <player> <skillId|all>");
        sender.sendMessage(ChatColor.GRAY + "/skillsadmin removelevel <player> <amount>");
        sender.sendMessage(ChatColor.GRAY + "/skillsadmin removepoints <player> <amount>");
        sender.sendMessage(ChatColor.GRAY + "/skillsadmin see <player>");
    }
}
