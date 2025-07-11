// src/main/java/org/simpleskills/placeholder/SkillsPlaceholderExpansion.java
package org.simpleskills.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleskills.api.SkillsAPI;
import org.simpleskills.manager.SkillManager;
import org.simpleskills.model.PlayerSkills;
import org.simpleskills.model.SkillDefinition;
import org.simpleskills.registry.SkillRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class SkillsPlaceholderExpansion extends PlaceholderExpansion {

    private final SkillManager skillManager;

    public SkillsPlaceholderExpansion() {
        this.skillManager = SkillsAPI.getSkillManager();
    }

    @Override public @NotNull String getIdentifier() { return "simpleskills"; }
    @Override public @NotNull String getAuthor()     { return "Ouistitiw"; }
    @Override public @NotNull String getVersion()    { return "1.1"; }
    @Override public boolean persist()    { return true; }
    @Override public boolean canRegister() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        String key = params.toLowerCase(Locale.ROOT);

        if ("points".equals(key)) {
            return String.valueOf(SkillsAPI.getPoints(player));
        }
        if ("total_level".equals(key)) {
            int total = SkillRegistry.getAll().stream()
                    .mapToInt(def -> SkillsAPI.getLevel(player, def.getId()))
                    .sum();
            return String.valueOf(total);
        }
        if (key.startsWith("level_")) {
            String skillId = key.substring("level_".length());
            return String.valueOf(SkillsAPI.getLevel(player, skillId));
        }
        if (key.startsWith("top_")) {
            try {
                int rank = Integer.parseInt(key.substring("top_".length()));
                return getTopPlayerEntry(rank);
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }

    private String getTopPlayerEntry(int rank) {
        if (rank < 1) return "";
        List<Map.Entry<OfflinePlayer, PlayerSkills>> sorted = skillManager.getAllPlayerSkills().stream()
                .map(ps -> Map.entry(skillManager.getOfflinePlayer(ps), ps))
                .filter(e -> e.getKey() != null)
                .sorted((e1, e2) -> {
                    int sum1 = e1.getValue().getSkillIds().stream()
                            .mapToInt(id -> e1.getValue().getLevel(id)).sum();
                    int sum2 = e2.getValue().getSkillIds().stream()
                            .mapToInt(id -> e2.getValue().getLevel(id)).sum();
                    return Integer.compare(sum2, sum1);
                })
                .collect(Collectors.toList());

        if (rank > sorted.size()) return "";
        var entry = sorted.get(rank - 1);
        String name = entry.getKey().getName();
        int total = entry.getValue().getSkillIds().stream()
                .mapToInt(id -> entry.getValue().getLevel(id)).sum();
        return name + ": " + total;
    }
}
