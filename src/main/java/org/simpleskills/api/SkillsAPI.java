package org.simpleskills.api;

import org.bukkit.entity.Player;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

import java.util.Collection;

/**
 * API publique : plus besoin de SkillType, on utilise l'ID ou la définition directement.
 */
public class SkillsAPI {
    private static SkillManager manager;

    public static void initialize(SkillManager skillManager) {
        manager = skillManager;
    }

    public static int getLevel(Player player, String skillId) {
        SkillDefinition def = SkillRegistry.get(skillId.toLowerCase());
        if (def == null) {
            throw new IllegalArgumentException("Skill non trouvé pour l'ID : " + skillId);
        }
        return manager.getPlayerSkills(player).getLevel(def.getId());
    }

    public static int getLevel(Player player, SkillDefinition def) {
        return manager.getPlayerSkills(player).getLevel(def.getId());
    }

    public static int getPoints(Player player) {
        return manager.getPlayerSkills(player).getPoints();
    }

    public static SkillManager getSkillManager() {
        return manager;
    }

    public static class SkillServiceImpl implements SkillService {
        private final SkillManager mgr;
        public SkillServiceImpl(SkillManager mgr) { this.mgr = mgr; }

        @Override
        public int getLevel(Player player, String skillId) {
            return SkillsAPI.getLevel(player, skillId);
        }

        @Override
        public int getPoints(Player player) {
            return SkillsAPI.getPoints(player);
        }

        @Override
        public Collection<SkillDefinition> getDefinitions() {
            return SkillRegistry.getAll();
        }
    }
    public static boolean isSkillEnabled(Player player, String skillId) {
        SkillDefinition def = SkillRegistry.get(skillId.toLowerCase());
        if (def == null) {
            throw new IllegalArgumentException("Skill non trouvé pour l'ID : " + skillId);
        }
        return manager.getPlayerSkills(player).isSkillEnabled(def.getId());
    }

}
