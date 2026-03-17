package com.aeternum.systems.classes;

/**
 * All player classes in Aeternum.
 * Players choose their class and customize it through skill point allocation.
 * Some classes require karma conditions or special quests to unlock.
 */
public enum PlayerClass {

    // ── STARTER ───────────────────────────────────────────────────────────────
    WANDERER("Wanderer",
        "The beginner class. Balanced stats, no restrictions.",
        ClassType.BALANCED, 0, false),

    // ── MELEE ─────────────────────────────────────────────────────────────────
    WARRIOR("Warrior",
        "Master of melee combat. High defense and health pool.",
        ClassType.MELEE, 0, false),

    BERSERKER("Berserker",
        "Sacrifices defense for devastating attack power. Rage fuels destruction.",
        ClassType.MELEE, 0, false),

    PALADIN("Paladin",
        "Holy warrior balancing offense with healing and light magic.",
        ClassType.HYBRID, 500, false),

    SHADOW_KNIGHT("Shadow Knight",
        "Dark warrior drawing power from corruption. Commands dark energy.",
        ClassType.HYBRID, -1000, false),

    MONK("Monk",
        "Disciplines the body to its limit. Lightning-fast combos, iron defense without armor.",
        ClassType.MELEE, 0, false),

    KNIGHT_OF_LIGHT("Knight of Light",
        "Divine champion. Supreme defense, holy auras, and celestial favor.",
        ClassType.MELEE, 2000, false),

    // ── RANGED ────────────────────────────────────────────────────────────────
    RANGER("Ranger",
        "Expert archer and tracker. High mobility, traps, and precision shots.",
        ClassType.RANGED, 0, false),

    ASSASSIN("Assassin",
        "Master of stealth and critical strikes. Kills from the shadows.",
        ClassType.RANGED, 0, false),

    HUNTER("Hunter",
        "Tames and fights alongside powerful beasts. Creature bond enhances all abilities.",
        ClassType.RANGED, 0, false),

    // ── MAGIC ─────────────────────────────────────────────────────────────────
    MAGE("Mage",
        "Unleashes raw elemental magic. Glass cannon with immense destructive potential.",
        ClassType.MAGIC, 0, false),

    WARLOCK("Warlock",
        "Pacts with dark forces grant incredible power. Corruption has its price.",
        ClassType.MAGIC, -1000, false),

    DRUID("Druid",
        "Shapeshifter attuned to nature. Controls terrain and heals with earth magic.",
        ClassType.MAGIC, 0, false),

    CLERIC("Cleric",
        "Holy healer and support. Divine intervention, powerful buffs, repels darkness.",
        ClassType.MAGIC, 500, false),

    BARD("Bard",
        "Music-based magic that buffs allies and destroys enemies with sound.",
        ClassType.MAGIC, 0, false),

    BLOOD_MAGE("Blood Mage",
        "Uses own health as resource for catastrophic magical power. Risk everything.",
        ClassType.MAGIC, -500, false),

    // ── SUMMONER ──────────────────────────────────────────────────────────────
    NECROMANCER("Necromancer",
        "Raises the dead to fight. Commands armies of undead and drains life.",
        ClassType.SUMMONER, -500, false),

    SUMMONER("Summoner",
        "Binds elemental and magical creatures. The more enemies fall, the stronger they become.",
        ClassType.SUMMONER, 0, false),

    // ── UTILITY ───────────────────────────────────────────────────────────────
    ALCHEMIST("Alchemist",
        "Master of potions and transmutations. Buffs, debuffs, battlefield control.",
        ClassType.UTILITY, 0, false),

    ENGINEER("Engineer",
        "Crafts mechanical contraptions for combat. Turrets, bombs, siege weapons.",
        ClassType.UTILITY, 0, false),

    // ── SPECIAL (unlocked via quests/titles) ──────────────────────────────────
    CELESTIAL_KNIGHT("Celestial Knight",
        "★ RARE — Forged through divine trial. Commands light and stellar magic.",
        ClassType.SPECIAL, 5000, true),

    VOID_WALKER("Void Walker",
        "★ RARE — Phases through reality, warps space, wields void energy.",
        ClassType.SPECIAL, 0, true),

    DRAGON_KNIGHT("Dragon Knight",
        "★ RARE — Bonded with a dragon spirit. Breathes fire, grows scales.",
        ClassType.SPECIAL, 0, true),

    CHAOS_LORD("Chaos Lord",
        "★ RARE — Pure entropy incarnate. Every battle is completely unpredictable.",
        ClassType.SPECIAL, -3000, true);

    // ── CLASS DATA ────────────────────────────────────────────────────────────

    private final String displayName;
    private final String description;
    private final ClassType type;
    private final int karmaRequirement; // minimum karma needed
    private final boolean requiresQuest;

    PlayerClass(String displayName, String description, ClassType type,
                int karmaRequirement, boolean requiresQuest) {
        this.displayName     = displayName;
        this.description     = description;
        this.type            = type;
        this.karmaRequirement = karmaRequirement;
        this.requiresQuest   = requiresQuest;
    }

    public String getDisplayName()    { return displayName; }
    public String getDescription()    { return description; }
    public ClassType getType()        { return type; }
    public int getKarmaRequirement()  { return karmaRequirement; }
    public boolean requiresQuest()    { return requiresQuest; }
    public boolean isSpecial()        { return type == ClassType.SPECIAL; }
    public boolean isDarkAligned()    { return karmaRequirement < 0; }

    /**
     * Check if a player can pick this class based on their karma.
     */
    public boolean isUnlockable(int playerKarma) {
        if (requiresQuest) return false; // Need quest completion
        if (karmaRequirement >= 0) return playerKarma >= karmaRequirement;
        return playerKarma <= karmaRequirement;
    }

    /**
     * Base stat bonuses applied when choosing this class.
     * Returns array: [maxHealth, maxEnergy, physAtk, magAtk, physDef, magDef]
     */
    public double[] getBaseStatBonuses() {
        return switch (this) {
            case WARRIOR        -> new double[]{ 50,  0,  10,  0,  8,  0};
            case BERSERKER      -> new double[]{ 30,  0,  20,  0, -2,  0};
            case PALADIN        -> new double[]{ 40, 20,   5,  5, 10,  5};
            case SHADOW_KNIGHT  -> new double[]{ 20, 15,  15, 10,  5,  0};
            case MONK           -> new double[]{ 20, 15,  10,  0,  5,  0};
            case KNIGHT_OF_LIGHT-> new double[]{ 60, 10,   5,  0, 15, 10};
            case RANGER         -> new double[]{ 10,  0,   8,  0,  0,  0};
            case ASSASSIN       -> new double[]{-10,  0,  12,  0,  0,  0};
            case HUNTER         -> new double[]{ 10,  0,   8,  0,  3,  0};
            case MAGE           -> new double[]{-15, 50,   0, 25, -3,  0};
            case WARLOCK        -> new double[]{ 0,  30,   0, 20,  0,  0};
            case DRUID          -> new double[]{ 20, 20,   5, 10,  0,  0};
            case CLERIC         -> new double[]{ 10, 25,   0,  8,  0, 15};
            case BARD           -> new double[]{ 0,  20,   5,  5,  3,  3};
            case BLOOD_MAGE     -> new double[]{-20, 20,   0, 30,  0,  0};
            case NECROMANCER    -> new double[]{ 0,  40,   0, 15,  0,  0};
            case SUMMONER       -> new double[]{ 0,  60,   0, 10,  0,  0};
            case ALCHEMIST      -> new double[]{ 0,  20,   0,  0,  0,  0};
            case ENGINEER       -> new double[]{ 10,  0,   5,  0,  5,  0};
            case CELESTIAL_KNIGHT->new double[]{ 80, 40,  20, 20, 20, 20};
            case VOID_WALKER    -> new double[]{ 30, 50,  15, 15,  5,  5};
            case DRAGON_KNIGHT  -> new double[]{ 60, 20,  25,  0, 15,  0};
            case CHAOS_LORD     -> new double[]{ 20, 30,  25, 25,  0,  0};
            default             -> new double[]{  0,  0,   0,  0,  0,  0};
        };
    }

    public enum ClassType {
        MELEE, RANGED, MAGIC, SUMMONER, HYBRID, UTILITY, BALANCED, SPECIAL
    }
}
