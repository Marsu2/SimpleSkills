package org.simpleskills.manager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;
import org.simpleskills.storage.DataStorage;
import org.simpleskills.storage.YamlStorage;
import org.simpleskills.upgrade.UpgradeRewardManager;



import java.util.*;
import java.util.stream.Collectors;

public class SkillManager {
    private final DataStorage storage;
    private final Plugin plugin;
    private final Map<String, PlayerSkills> cache = new LinkedHashMap<>();

    public SkillManager(Plugin plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(JavaPlugin.getProvidingPlugin(SkillManager.class));

        // Initialise une entrée vide pour chaque joueur en ligne
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            cache.put(p.getUniqueId().toString(), new PlayerSkills());
        }
    }

    public PlayerSkills getPlayerSkills(UUID uuid) {
        return cache.get(uuid.toString());
    }

    /** Charge tous les joueurs, en ligne **et** hors ligne */
    public void loadAll() {
        cache.clear();
        for (OfflinePlayer op : plugin.getServer().getOfflinePlayers()) {
            PlayerSkills ps;
            if (op.isOnline()) {
                ps = storage.load((Player) op);
            } else {
                ps = storage.loadOffline(op);
            }
            cache.put(op.getUniqueId().toString(), ps);
        }
    }

    /** Charge (ou recharge) les données du joueur depuis le stockage */
    public void load(OfflinePlayer op) {
        PlayerSkills ps;
        if (op.isOnline()) {
            ps = storage.load((Player) op);
        } else {
            ps = storage.loadOffline(op);
        }
        cache.put(op.getUniqueId().toString(), ps);
    }

    public void saveAll() {
        for (Map.Entry<String, PlayerSkills> e : cache.entrySet()) {
            UUID uuid = UUID.fromString(e.getKey());
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.isOnline()) {
                storage.save((Player) op, e.getValue());
            } else {
                storage.saveOffline(op, e.getValue());
            }
        }
    }

    /** Ajoute des points de compétence et sauvegarde */
    public void addPoints(Player p, int pts) {
        PlayerSkills ps = cache.get(p.getUniqueId().toString());
        ps.addPoints(pts);
        storage.save(p, ps);
    }

    /**
     * Tente d'augmenter le niveau d'un skill pour le joueur.
     * @return true si le niveau a été augmenté, false sinon.
     */
    public boolean increase(Player p, SkillDefinition def) {
        PlayerSkills ps = cache.get(p.getUniqueId().toString());
        int oldLevel = ps.getLevel(def.getId());
        if (oldLevel >= def.getMaxLevel()) return false;

        int cost = def.getCostForLevel(oldLevel + 1);
        if (ps.getPoints() < cost) return false;

        // Retire les points et met à jour le niveau
        ps.addPoints(-cost);
        int newLevel = oldLevel + 1;
        ps.setLevel(def.getId(), newLevel);

        // Déclenche l'événement
        SkillLevelChangeEvent evt = new SkillLevelChangeEvent(p, def.getId(), oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(evt);

        // Sauvegarde et récompense
        storage.save(p, ps);
        UpgradeRewardManager.handleReward(plugin, def.getId(), newLevel, p.getName());

        return true;
    }

    /** Récupère les données de compétences d'un joueur en ligne */
    public PlayerSkills getPlayerSkills(Player p) {
        return cache.get(p.getUniqueId().toString());
    }

    /** Récupère toutes les données chargées, pour le leaderboard */
    public Collection<PlayerSkills> getAllPlayerSkills() {
        return cache.values();
    }

    /**
     * Pour une instance de PlayerSkills, retrouve l'OfflinePlayer associé.
     * @return l'OfflinePlayer ou null si introuvable.
     */
    public OfflinePlayer getOfflinePlayer(PlayerSkills ps) {
        for (Map.Entry<String, PlayerSkills> e : cache.entrySet()) {
            if (e.getValue() == ps) {
                return Bukkit.getOfflinePlayer(UUID.fromString(e.getKey()));
            }
        }
        return null;
    }

    /** Calcule le total des niveaux pour un PlayerSkills donné */
    public int getTotalLevel(PlayerSkills ps) {
        return SkillRegistry.getAll().stream()
                .mapToInt(def -> ps.getLevel(def.getId()))
                .sum();
    }

    /**
     * Calcule et renvoie le top n‑ième joueur au classement
     */
    public Optional<Map.Entry<OfflinePlayer, Integer>> getTopEntry(int rank) {
        List<Map.Entry<OfflinePlayer, Integer>> sorted = cache.entrySet().stream()
                .map(e -> {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(e.getKey()));
                    int total = getTotalLevel(e.getValue());
                    return Map.entry(op, total);
                })
                .filter(entry -> entry.getKey() != null && entry.getKey().getName() != null)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        if (rank < 1 || rank > sorted.size()) {
            return Optional.empty();
        }
        return Optional.of(sorted.get(rank - 1));
    }

    /**
     * Recalcule les attributs du joueur en fonction de ses skills.
     */
    public void recalculatePlayerAttributes(Player player, PlayerSkills skills) {
        for (SkillDefinition skillDef : SkillRegistry.getAll()) {
            String skillId = skillDef.getId();
            String attributeKey = skillDef.getAttributeKey();
            if (attributeKey != null && !attributeKey.isBlank()) {
                try {
                    Attribute bukkitAttribute = Attribute.valueOf(attributeKey.toUpperCase());
                    if (bukkitAttribute != null) {
                        AttributeInstance attribute = player.getAttribute(bukkitAttribute);
                        if (attribute != null) {
                            int level = skills.isSkillEnabled(skillId) ? skills.getLevel(skillId) : 0;
                            double baseValue = skillDef.getAttributeValue(level);
                            attribute.setBaseValue(baseValue);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}
