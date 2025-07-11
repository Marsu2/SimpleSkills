package org.simpleskills.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EventWorldSkillResetListener implements Listener {

    private final SkillManager skillManager;
    private final List<String> eventWorlds = Arrays.asList("Event", "boatrace");

    private final Map<UUID, Map<String, Integer>> savedSkills = new HashMap<>();
    private final File savedSkillsFile;
    private final YamlConfiguration savedSkillsConfig;

    public EventWorldSkillResetListener(SkillManager skillManager, File dataFolder) {
        this.skillManager = skillManager;
        this.savedSkillsFile = new File(dataFolder, "saved_skills.yml");
        this.savedSkillsConfig = YamlConfiguration.loadConfiguration(savedSkillsFile);
        loadSavedSkills();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();

        PlayerSkills skills = skillManager.getPlayerSkills(uuid);
        if (skills == null) return;

        // Entrée dans un monde event
        if (!eventWorlds.contains(fromWorld) && eventWorlds.contains(toWorld)) {
            if (!savedSkills.containsKey(uuid)) {
                Map<String, Integer> levels = new HashMap<>();
                for (SkillDefinition skillDef : SkillRegistry.getAll()) {
                    String skillId = skillDef.getId();
                    levels.put(skillId, skills.getLevel(skillId));
                    skills.setLevel(skillId, 0);
                }
                savedSkills.put(uuid, levels);
                savePlayerSkills(uuid, levels);

                YamlConfiguration enabledSection = new YamlConfiguration();
                for (SkillDefinition skillDef : SkillRegistry.getAll()) {
                    String skillId = skillDef.getId();
                    boolean enabled = skills.isSkillEnabled(skillId);
                    enabledSection.set(skillId, enabled);
                    skills.setSkillEnabled(skillId, false);
                }
                savedSkillsConfig.set(uuid.toString() + ".enabled", enabledSection);
                saveConfig();

                skillManager.recalculatePlayerAttributes(player, skills);
                player.sendActionBar(Component.text("[EVENT] Tous tes skills ont été désactivés pour l'événement.").color(NamedTextColor.RED));
            }
        }

        // Sortie d'un monde event
        else if (eventWorlds.contains(fromWorld) && !eventWorlds.contains(toWorld)) {
            if (savedSkills.containsKey(uuid)) {
                Map<String, Integer> levels = savedSkills.remove(uuid);
                for (Map.Entry<String, Integer> entry : levels.entrySet()) {
                    skills.setLevel(entry.getKey(), entry.getValue());
                }

                YamlConfiguration enabledSection = (YamlConfiguration) savedSkillsConfig.getConfigurationSection(uuid.toString() + ".enabled");
                if (enabledSection != null) {
                    for (String skillId : enabledSection.getKeys(false)) {
                        boolean wasEnabled = enabledSection.getBoolean(skillId, true);
                        skills.setSkillEnabled(skillId, wasEnabled);
                    }
                }

                removePlayerSkills(uuid);
                skillManager.recalculatePlayerAttributes(player, skills);
                player.sendActionBar(Component.text("[EVENT] Tes skills ont été restaurés !").color(NamedTextColor.RED));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentWorld = player.getWorld().getName();

        PlayerSkills skills = skillManager.getPlayerSkills(uuid);
        if (skills == null) return;

        if (eventWorlds.contains(currentWorld)) {
            if (savedSkillsConfig.contains(uuid.toString())) {
                if (!savedSkills.containsKey(uuid)) {
                    Map<String, Integer> levels = new HashMap<>();
                    for (String skillId : savedSkillsConfig.getConfigurationSection(uuid.toString()).getKeys(false)) {
                        if (!"enabled".equals(skillId)) {
                            int level = savedSkillsConfig.getInt(uuid.toString() + "." + skillId);
                            levels.put(skillId, level);
                        }
                    }
                    savedSkills.put(uuid, levels);
                }

                for (SkillDefinition skillDef : SkillRegistry.getAll()) {
                    skills.setLevel(skillDef.getId(), 0);
                    skills.setSkillEnabled(skillDef.getId(), false);
                }

                skillManager.recalculatePlayerAttributes(player, skills);
                player.sendActionBar(Component.text("[EVENT] Tous tes skills ont été désactivés (reconnexion).").color(NamedTextColor.RED));
            }
        }
    }

    private void savePlayerSkills(UUID uuid, Map<String, Integer> levels) {
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            savedSkillsConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
        }
        saveConfig();
    }

    private void removePlayerSkills(UUID uuid) {
        savedSkillsConfig.set(uuid.toString(), null);
        savedSkillsConfig.set(uuid.toString() + ".enabled", null);
        saveConfig();
    }

    private void saveConfig() {
        try {
            savedSkillsConfig.save(savedSkillsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedSkills() {
        for (String uuidString : savedSkillsConfig.getKeys(false)) {
            if ("enabled".equals(uuidString)) continue;
            UUID uuid = UUID.fromString(uuidString);
            Map<String, Integer> levels = new HashMap<>();
            for (String skillId : savedSkillsConfig.getConfigurationSection(uuidString).getKeys(false)) {
                if (!"enabled".equals(skillId)) {
                    int level = savedSkillsConfig.getInt(uuidString + "." + skillId);
                    levels.put(skillId, level);
                }
            }
            savedSkills.put(uuid, levels);
        }
    }
}
