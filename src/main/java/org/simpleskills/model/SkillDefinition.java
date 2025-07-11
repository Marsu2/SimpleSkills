// src/main/java/org/simpleskills/model/SkillDefinition.java
package org.simpleskills.model;

import org.bukkit.Material;
import org.simpleskills.util.ExpressionEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class SkillDefinition {
    private final String id;
    private final String display;
    private final Material material;
    private final int maxLevel;
    private final int baseCost;
    private final int costIncrement;
    private final String attributeKey;
    private final String costFormula;
    private final String attributeFormula;
    private final List<String> description;
    private final String icon;


    public SkillDefinition(String id,
                           String display,
                           Material material,
                           int maxLevel,
                           String icon,
                           int baseCost,
                           int costIncrement,
                           String attributeKey,
                           String costFormula,
                           String attributeFormula,
                           List<String> description) {
        this.id               = id;
        this.display          = display;
        this.material         = material;
        this.maxLevel         = maxLevel;
        this.icon             = icon;
        this.baseCost         = baseCost;
        this.costIncrement    = costIncrement;
        this.attributeKey     = attributeKey;
        this.costFormula      = costFormula;
        this.attributeFormula = attributeFormula;
        this.description      = description;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
    public Material getMaterial() { return material; }
    public int getMaxLevel() { return maxLevel; }
    public String getAttributeKey() { return attributeKey; }

    public int getCostForLevel(int level) {
        if (costFormula != null && !costFormula.isBlank()) {
            try {
                return (int) Math.round(ExpressionEvaluator.eval(costFormula, level));
            } catch (Exception e) {
                System.err.println("Error evaluating costFormula for “" + id + "”: " + e.getMessage());
            }
        }
        return baseCost + (level - 1) * costIncrement;
    }

    public double getAttributeValue(int level) {
        if (attributeFormula != null && !attributeFormula.isBlank()) {
            try {
                double value = ExpressionEvaluator.eval(attributeFormula, level);

                // Correction spécifique pour le skill "agility" au niveau 5 et plus
                if ("agility".equalsIgnoreCase(id) && level >= 5) {
                    value = 0.14;
                }

                return value;
            } catch (Exception e) {
                System.err.println("Error evaluating attributeFormula for \"" + id + "\": " + e.getMessage());
            }
        }
        return 0;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getDescriptionForLevel(int level) {
        double value = getAttributeValue(level);
        return description.stream()
                .map(line -> line.replace("%value%", String.format("%.2f", value)))
                .collect(Collectors.toList());
    }
    public String getIcon() {
        return icon;
    }
}
