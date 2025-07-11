package org.simpleskills.manager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.simpleskills.manager.AttributeListener;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.SimpleSkillsPlugin;

public class SkillJoinListener implements Listener {

    private final SkillManager skillManager;
    private final AttributeListener attributeListener;

    public SkillJoinListener(SkillManager manager, AttributeListener attrListener) {
        this.skillManager = manager;
        this.attributeListener = attrListener;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Charger les skills du joueur
        skillManager.load(event.getPlayer());

        // Réappliquer les attributs après chargement
        attributeListener.applyAllAttributes(event.getPlayer());


    }
}
