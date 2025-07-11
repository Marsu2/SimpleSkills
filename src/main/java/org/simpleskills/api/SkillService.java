package org.simpleskills.api;

import org.bukkit.entity.Player;
import org.simpleskills.model.SkillDefinition;
import java.util.Collection;

public interface SkillService {
    int   getLevel(Player player, String skillId);
    int   getPoints(Player player);
    Collection<SkillDefinition> getDefinitions();
}
