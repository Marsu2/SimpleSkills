// src/main/java/org/simpleskills/registry/SkillRegistry.java
package org.simpleskills.registry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.simpleskills.model.SkillDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillRegistry {
    private static final Map<String, SkillDefinition> SKILLS = new LinkedHashMap<>();

    public static void reload(Plugin plugin) {
        SKILLS.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection sec = cfg.getConfigurationSection("skills");
        for (String key : sec.getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection(key);

            String costFormula = s.getString("formula-cost", "").trim();
            String attrFormula = s.getString("formula-attribute", "").trim();

            plugin.getLogger().info("Loaded skill “" + key + "” formulas: cost=“"
                    + costFormula + "”, attribute=“" + attrFormula + "”");

            SkillDefinition def = new SkillDefinition(
                    key,
                    s.getString("display"),
                    Material.valueOf(s.getString("material")),
                    s.getInt("max-level"),
                    s.getString("icon"),
                    s.getInt("base-cost"),
                    s.getInt("cost-increment"),
                    s.getString("attribute"),
                    costFormula,
                    attrFormula,
                    s.getStringList("description")
            );
            SKILLS.put(key, def);
        }
    }

    public static Collection<SkillDefinition> getAll() {
        return SKILLS.values();
    }

    public static SkillDefinition get(String id) {
        return SKILLS.get(id);
    }
}
