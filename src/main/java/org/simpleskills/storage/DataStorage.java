// src/main/java/org/simpleskills/storage/DataStorage.java
package org.simpleskills.storage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.simpleskills.model.PlayerSkills;

public interface DataStorage {
    PlayerSkills load(Player player);
    void save(Player player, PlayerSkills skills);

    // Nouvelle méthode pour charger hors-ligne
    PlayerSkills loadOffline(OfflinePlayer player);
    void saveOffline(OfflinePlayer player, PlayerSkills skills);
}
