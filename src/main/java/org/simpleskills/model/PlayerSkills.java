package org.simpleskills.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerSkills {
    private int points;
    private final Map<String, Integer> levels = new LinkedHashMap<>();
    private final Map<String, Boolean> enabled = new LinkedHashMap<>();

    public PlayerSkills() { }

    public int getPoints() { return points; }
    public void setPoints(int p) { points = p; }
    public void addPoints(int a) { points += a; }

    public int getLevel(String id) { return levels.getOrDefault(id, 0); }
    public void setLevel(String id, int level) { levels.put(id, level); }
    public Collection<String> getSkillIds() { return levels.keySet(); }

    public boolean isSkillEnabled(String id) { return enabled.getOrDefault(id, true); }
    public void setSkillEnabled(String id, boolean value) { enabled.put(id, value); }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("points", points);
        m.put("levels", new LinkedHashMap<>(levels));
        m.put("enabled", new LinkedHashMap<>(enabled));
        return m;
    }

    @SuppressWarnings("unchecked")
    public static PlayerSkills fromMap(Map<String, Object> m) {
        PlayerSkills ps = new PlayerSkills();
        ps.points = (Integer) m.getOrDefault("points", 0);
        Map<String, Integer> lvls = (Map<String, Integer>) m.getOrDefault("levels", Map.of());
        ps.levels.putAll(lvls);
        Map<String, Boolean> en = (Map<String, Boolean>) m.getOrDefault("enabled", Map.of());
        ps.enabled.putAll(en);
        return ps;
    }
}
