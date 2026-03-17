package com.aeternum.systems.skills;

public class SkillDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final double energyCost;
    private final long cooldownMs;
    private final int levelRequirement;
    private final String classRequirement; // null = any class can learn it
    private final int skillPointCost;

    public SkillDefinition(String id, String displayName, String description,
                           double energyCost, long cooldownMs,
                           int levelRequirement, String classRequirement,
                           int skillPointCost) {
        this.id               = id;
        this.displayName      = displayName;
        this.description      = description;
        this.energyCost       = energyCost;
        this.cooldownMs       = cooldownMs;
        this.levelRequirement = levelRequirement;
        this.classRequirement = classRequirement;
        this.skillPointCost   = skillPointCost;
    }

    public String getId()               { return id; }
    public String getDisplayName()      { return displayName; }
    public String getDescription()      { return description; }
    public double getEnergyCost()       { return energyCost; }
    public long getCooldownMs()         { return cooldownMs; }
    public int getLevelRequirement()    { return levelRequirement; }
    public String getClassRequirement() { return classRequirement; }
    public int getSkillPointCost()      { return skillPointCost; }

    /** Can a player of the given class and level learn this skill? */
    public boolean isLearnableBy(String playerClass, int playerLevel) {
        if (playerLevel < levelRequirement) return false;
        if (classRequirement == null) return true;
        return classRequirement.equalsIgnoreCase(playerClass);
    }
}
