package org.simpleskills.storage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleskills.model.PlayerSkills;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Map;

public class YamlStorage implements DataStorage {
    private final File folder;
    private final Yaml yaml = new Yaml();

    public YamlStorage(JavaPlugin plugin) {
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public PlayerSkills load(Player player) {
        // appels la nouvelle méthode si player est null
        return loadOffline(player);
    }

    @Override
    public void save(Player player, PlayerSkills skills) {
        saveOffline(player, skills);
    }

    @Override
    public PlayerSkills loadOffline(OfflinePlayer offline) {
        File f = new File(folder, offline.getUniqueId() + ".yml");
        if (!f.exists()) return new PlayerSkills();
        try (FileInputStream in = new FileInputStream(f)) {
            Map<String, Object> m = yaml.load(in);
            return PlayerSkills.fromMap(m);
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayerSkills();
        }
    }

    @Override
    public void saveOffline(OfflinePlayer player, PlayerSkills skills) {
        File f = new File(folder, player.getUniqueId() + ".yml");
        try (FileWriter w = new FileWriter(f)) {
            yaml.dump(skills.toMap(), w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
