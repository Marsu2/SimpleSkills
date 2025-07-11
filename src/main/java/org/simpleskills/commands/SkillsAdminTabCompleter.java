package org.simpleskills.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsAdminTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS =
            Arrays.asList("resetlevel", "removelevel", "removepoints", "see");

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String alias,
                                      String[] args) {

        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("resetlevel")) {
            List<String> options = new ArrayList<>();
            options.add("all");
            for (SkillDefinition def : SkillRegistry.getAll()) {
                options.add(def.getId());
            }
            return options.stream()
                    .filter(opt -> opt.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // no suggestions for other numeric args
        return List.of();
    }
}
