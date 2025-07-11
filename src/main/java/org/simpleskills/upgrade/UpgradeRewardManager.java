package org.simpleskills.upgrade;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

/**
 * Charge les récompenses d'upgrades depuis upgrades.yml,
 * en stockant pour chaque skill une liste de chaînes (une par niveau).
 */
public class UpgradeRewardManager {
    private static final Map<String, List<String>> REWARDS = new HashMap<>();

    /**
     * Recharge le fichier upgrades.yml.
     * Structure attendue :
     *
     * upgrades:
     *   strength:
     *     rewards:
     *       - "reward1"
     *       - "reward2"
     *   vigor:
     *     rewards:
     *       - "..."
     */
    public static void reload(Plugin plugin) {
        REWARDS.clear();

        // Charge ou crée upgrades.yml
        File file = new File(plugin.getDataFolder(), "upgrades.yml");
        if (!file.exists()) {
            plugin.saveResource("upgrades.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection top = cfg.getConfigurationSection("upgrades");
        if (top == null) return;

        for (String skillId : top.getKeys(false)) {
            ConfigurationSection sec = top.getConfigurationSection(skillId);
            if (sec == null) continue;

            // Récupère la liste des rewards en toute sécurité
            List<String> rewards = sec.getStringList("rewards");
            // getStringList never returns null; empty list if absent

            REWARDS.put(skillId.toLowerCase(), new ArrayList<>(rewards));
        }
    }

    /**
     * Récupère les récompenses configurées pour un skill à un niveau donné.
     * @param skillId l'ID du skill
     * @param level   le niveau (1-indexé)
     * @return liste des rewards pour ce niveau, ou vide
     */
    public static List<String> getRewards(String skillId, int level) {
        List<String> list = REWARDS.get(skillId.toLowerCase());
        if (list == null || list.isEmpty()) return Collections.emptyList();
        if (level < 1 || level > list.size()) return Collections.emptyList();
        // Retourne une liste à un seul élément ; étendez selon vos besoins
        return Collections.singletonList(list.get(level - 1));
    }

    public static void handleReward(Plugin plugin, String skillId, int level, String playerName) {
        List<String> rewards = getRewards(skillId, level);
        if (rewards.isEmpty()) return;

        for (String cmd : rewards) {
            // on remplace le placeholder %player% par le nom du joueur
            String command = cmd.replace("%player%", playerName);
            // exécute en tant que console
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    command
            );
        }
    }
}
