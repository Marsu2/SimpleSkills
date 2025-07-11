// src/main/java/org/simpleskills/manager/AttributeListener.java
package org.simpleskills.manager;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.simpleskills.api.SkillsAPI;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttributeListener implements Listener {

    private final SkillManager skillManager;

    public AttributeListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    private static final Map<String, Attribute> ATTR_MAP = new HashMap<>();
    static {
        ATTR_MAP.put("ARMOR", Attribute.ARMOR);
        ATTR_MAP.put("ARMOR_TOUGHNESS", Attribute.ARMOR_TOUGHNESS);
        ATTR_MAP.put("ATTACK_DAMAGE", Attribute.ATTACK_DAMAGE);
        ATTR_MAP.put("ATTACK_KNOCKBACK", Attribute.ATTACK_KNOCKBACK);
        ATTR_MAP.put("ATTACK_SPEED", Attribute.ATTACK_SPEED);
        ATTR_MAP.put("BLOCK_BREAK_SPEED", Attribute.BLOCK_BREAK_SPEED);
        ATTR_MAP.put("BLOCK_INTERACTION_RANGE", Attribute.BLOCK_INTERACTION_RANGE);
        ATTR_MAP.put("BURNING_TIME", Attribute.BURNING_TIME);
        ATTR_MAP.put("ENTITY_INTERACTION_RANGE", Attribute.ENTITY_INTERACTION_RANGE);
        ATTR_MAP.put("EXPLOSION_KNOCKBACK_RESISTANCE", Attribute.EXPLOSION_KNOCKBACK_RESISTANCE);
        ATTR_MAP.put("FALL_DAMAGE_MULTIPLIER", Attribute.FALL_DAMAGE_MULTIPLIER);
        ATTR_MAP.put("FLYING_SPEED", Attribute.FLYING_SPEED);
        ATTR_MAP.put("FOLLOW_RANGE", Attribute.FOLLOW_RANGE);
        ATTR_MAP.put("GRAVITY", Attribute.GRAVITY);
        ATTR_MAP.put("JUMP_STRENGTH", Attribute.JUMP_STRENGTH);
        ATTR_MAP.put("KNOCKBACK_RESISTANCE", Attribute.KNOCKBACK_RESISTANCE);
        ATTR_MAP.put("LUCK", Attribute.LUCK);
        ATTR_MAP.put("MAX_ABSORPTION", Attribute.MAX_ABSORPTION);
        ATTR_MAP.put("MAX_HEALTH", Attribute.MAX_HEALTH);
        ATTR_MAP.put("MINING_EFFICIENCY", Attribute.MINING_EFFICIENCY);
        ATTR_MAP.put("MOVEMENT_EFFICIENCY", Attribute.MOVEMENT_EFFICIENCY);
        ATTR_MAP.put("MOVEMENT_SPEED", Attribute.MOVEMENT_SPEED);
        ATTR_MAP.put("OXYGEN_BONUS", Attribute.OXYGEN_BONUS);
        ATTR_MAP.put("SAFE_FALL_DISTANCE", Attribute.SAFE_FALL_DISTANCE);
        ATTR_MAP.put("SCALE", Attribute.SCALE);
        ATTR_MAP.put("SNEAKING_SPEED", Attribute.SNEAKING_SPEED);
        ATTR_MAP.put("SPAWN_REINFORCEMENTS", Attribute.SPAWN_REINFORCEMENTS);
        ATTR_MAP.put("STEP_HEIGHT", Attribute.STEP_HEIGHT);
        ATTR_MAP.put("SUBMERGED_MINING_SPEED", Attribute.SUBMERGED_MINING_SPEED);
        ATTR_MAP.put("SWEEPING_DAMAGE_RATIO", Attribute.SWEEPING_DAMAGE_RATIO);
        ATTR_MAP.put("TEMPT_RANGE", Attribute.TEMPT_RANGE);
        ATTR_MAP.put("WATER_MOVEMENT_EFFICIENCY", Attribute.WATER_MOVEMENT_EFFICIENCY);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // 1) Applique les attributs
        applyAllAttributes(p);

        //test ici
        skillManager.load(p);

        // 2) Rafraîchis immédiatement les placeholders PAPI
        //    pour que %simpleskills_points% et %simpleskills_top_X% soient à jour
        PlaceholderAPI.setPlaceholders(p, "%simpleskills_points%");
        for (int i = 1; i <= 10; i++) {
            PlaceholderAPI.setPlaceholders(p, "%simpleskills_top_" + i + "%");
        }
    }

    @EventHandler
    public void onSkillChange(SkillLevelChangeEvent e) {
        try {
            Player player = e.getPlayer();
            SkillDefinition def = SkillRegistry.get(e.getSkillId());
            if (def != null && def.getAttributeKey() != null
                    && !def.getAttributeKey().isBlank()
                    && !"EXP".equalsIgnoreCase(def.getAttributeKey())) {
                applyAttribute(player, def);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler
    public void onExpGain(PlayerExpChangeEvent e) {
        try {
            Player player = e.getPlayer();
            PlayerSkills ps = skillManager.getPlayerSkills(player);
            if (ps == null) return;
            for (SkillDefinition def : SkillRegistry.getAll()) {
                if ("EXP".equalsIgnoreCase(def.getAttributeKey())) {
                    int lvl = ps.getLevel(def.getId());
                    double mult = def.getAttributeValue(lvl);
                    e.setAmount(e.getAmount() + (int)(e.getAmount() * mult));
                    break;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void applyAllAttributes(Player player) {
        PlayerSkills ps = skillManager.getPlayerSkills(player);
        if (ps == null) return;
        skillManager.recalculatePlayerAttributes(player, ps);
    }

    private void applyAttribute(Player player, SkillDefinition def) {
        PlayerSkills ps = skillManager.getPlayerSkills(player);
        if (ps == null) return;
        int lvl = ps.getLevel(def.getId());
        if (lvl < 1) return;

        Attribute attr = ATTR_MAP.get(def.getAttributeKey().toUpperCase(Locale.ROOT));
        if (attr == null) return;

        double val;
        try {
            val = def.getAttributeValue(lvl);
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }

        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null) {
            inst.setBaseValue(val);
        }
    }
}
