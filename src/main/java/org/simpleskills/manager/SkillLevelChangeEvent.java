package org.simpleskills.manager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

public class SkillLevelChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String skillId;
    private final int oldLevel;
    private final int newLevel;

    /**
     * @param player   le joueur dont le niveau change
     * @param skillId  l'ID du skill tel que défini dans config.yml
     * @param oldLevel niveau avant le changement
     * @param newLevel niveau après le changement
     */
    public SkillLevelChangeEvent(Player player,
                                 String skillId,
                                 int oldLevel,
                                 int newLevel) {
        this.player   = player;
        this.skillId  = skillId.toLowerCase();
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /** Le joueur concerné */
    public Player getPlayer() {
        return player;
    }

    /** L'ID du skill (clé dans config.yml) */
    public String getSkillId() {
        return skillId;
    }

    /**
     * Récupère la définition du skill via le SkillRegistry,
     * ou null si l'ID est invalide.
     */
    public SkillDefinition getSkillDefinition() {
        return SkillRegistry.get(skillId);
    }

    /** Niveau avant le changement */
    public int getOldLevel() {
        return oldLevel;
    }

    /** Niveau après le changement */
    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
