package org.simpleskills.manager;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.simpleskills.SimpleSkillsPlugin;
import org.simpleskills.model.PlayerSkills;

public class AgilitySpeedEffectListener implements Listener {

    private static final String SPEED_SKILL_ID = "agility";
    private final SkillManager skillManager;

    public AgilitySpeedEffectListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getModifiedType() != PotionEffectType.SPEED) return;

        Player player = (Player) event.getEntity();
        PlayerSkills skills = skillManager.getPlayerSkills(player.getUniqueId());
        if (skills == null) return;

        int level = skills.getLevel(SPEED_SKILL_ID);
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;

        // Niveau 1 à 4 : effet interdit
        if (skills.isSkillEnabled(SPEED_SKILL_ID) && level >= 1 && level <= 4) {
            if (event.getAction() == EntityPotionEffectEvent.Action.ADDED || event.getAction() == EntityPotionEffectEvent.Action.CHANGED) {
                event.setCancelled(true);

                // Retirer l'effet juste après, pour éviter le conflit d'exécution
                Bukkit.getScheduler().runTaskLater(SimpleSkillsPlugin.getInstance(), () -> {
                    if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                        player.removePotionEffect(PotionEffectType.SPEED);
                    }
                }, 1L);

                player.sendMessage("§e[INFO] À ton niveau, Speed et Skills sont incompatibles. " +
                        "Améliore ton skill au niveau 5 ou désactive le skill pour profiter de Speed.");
            }
            return;
        }

        // Niveau 5+ : effet autorisé
        if (skills.isSkillEnabled(SPEED_SKILL_ID) && level >= 5) {
            if (event.getAction() == EntityPotionEffectEvent.Action.ADDED || event.getAction() == EntityPotionEffectEvent.Action.CHANGED) {
                attr.setBaseValue(0.105);
                player.sendMessage("§e[INFO] Speed actif. Vitesse ajustée à 0.11.");
            } else if (event.getAction() == EntityPotionEffectEvent.Action.REMOVED) {
                Bukkit.getScheduler().runTask(SimpleSkillsPlugin.getInstance(), () -> {
                    if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                        attr.setBaseValue(0.14);
                        player.sendMessage("§e[INFO] Effet Speed terminé. Vitesse restaurée à 0.14.");
                    }
                });
            }
        }
    }
}
