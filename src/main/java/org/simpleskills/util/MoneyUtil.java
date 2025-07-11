package org.simpleskills.util;

import org.bukkit.entity.Player;
import org.simpleskills.api.SkillsAPI;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

public class MoneyUtil {

    /**
     * Apply a money boost based on the "richesse" skill.
     * @param player the player
     * @param baseAmount the original amount
     * @return boosted amount
     */
    public static double applyWealthBoost(Player player, double baseAmount) {
        SkillDefinition def = SkillRegistry.get("richesse");
        if (def == null) return baseAmount;
        int lvl = SkillsAPI.getLevel(player, def);
        double rate = def.getAttributeValue(lvl);
        return baseAmount + (baseAmount * rate);
    }
}
