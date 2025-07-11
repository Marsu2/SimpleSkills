package org.simpleskills.storage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.simpleskills.model.PlayerSkills;

public class DatabaseStorage implements DataStorage {
    @Override
    public PlayerSkills load(Player player) {
        return new PlayerSkills();
    }

    @Override
    public void save(Player player, PlayerSkills skills) {
    }

    @Override
    public PlayerSkills loadOffline(OfflinePlayer player) {
        return null;
    }

    @Override
    public void saveOffline(OfflinePlayer player, PlayerSkills skills) {

    }
}
