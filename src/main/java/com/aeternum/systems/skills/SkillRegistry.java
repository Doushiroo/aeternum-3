package com.aeternum.systems.skills;

import java.util.*;

/**
 * Registry of all skills available in Aeternum.
 * classRequirement = null means ANY class can learn it.
 */
public class SkillRegistry {

    private static final Map<String, SkillDefinition> SKILLS = new LinkedHashMap<>();

    static {
        // ── UNIVERSAL SKILLS (any class) ──────────────────────────────────────
        add("second_wind",        "Second Wind",         "Recover 25% HP instantly.",
            0,   180_000, 1,  null, 2);
        add("sprint_mastery",     "Sprint Mastery",      "[Passive] +15% speed when at full stamina.",
            0,   0,        5,  null, 1);
        add("iron_will",          "Iron Will",           "[Passive] Reduce all CC duration by 50%.",
            0,   0,        20, null, 3);
        add("divine_blessing",    "Divine Blessing",     "Heal and remove debuffs. Requires GOOD karma.",
            50,  60_000,   15, null, 3);
        add("shadow_veil",        "Shadow Veil",         "Reduce detection for 20s. Requires negative karma.",
            30,  30_000,   10, null, 2);
        add("treasure_sense",     "Treasure Sense",      "[Passive] Detect nearby ores and chests.",
            0,   0,        10, null, 2);
        add("berserk_strike",     "Berserk Strike",      "A powerful melee hit dealing 200% damage.",
            30,  15_000,   5,  null, 2);

        // ── WARRIOR ───────────────────────────────────────────────────────────
        add("shield_bash",     "Shield Bash",      "Stun an enemy for 2 seconds.",
            20,  8_000,    1,  "WARRIOR", 2);
        add("battle_cry",      "Battle Cry",       "Empower nearby allies: +25% ATK for 15s.",
            35,  30_000,   5,  "WARRIOR", 3);
        add("whirlwind",       "Whirlwind",        "Spin dealing 180% damage to all nearby enemies.",
            40,  12_000,   10, "WARRIOR", 3);
        add("last_stand",      "Last Stand",       "Below 20% HP: gain 50% DMG reduction for 10s.",
            0,   60_000,   15, "WARRIOR", 3);
        add("titan_rage",      "Titan Rage",       "[ULTIMATE] Triple physical ATK for 20s.",
            80,  120_000,  25, "WARRIOR", 5);
        add("war_charge",      "War Charge",       "Dash forward knocking back enemies in the way.",
            25,  10_000,   3,  "WARRIOR", 2);

        // ── BERSERKER ─────────────────────────────────────────────────────────
        add("frenzy",          "Frenzy",           "+60% ATK speed, +40% DMG, -30% DEF for 12s.",
            30,  20_000,   1,  "BERSERKER", 2);
        add("blood_rage",      "Blood Rage",       "[Passive] Every 10% HP lost = +8% physical DMG.",
            0,   0,        8,  "BERSERKER", 3);
        add("cleave",          "Cleave",           "Wide arc swing hitting all enemies for 200% DMG.",
            35,  8_000,    3,  "BERSERKER", 2);
        add("devastation",     "Devastation",      "[ULTIMATE] Uncontrolled fury for 30s. Might hit allies.",
            0,   180_000,  25, "BERSERKER", 5);

        // ── PALADIN ───────────────────────────────────────────────────────────
        add("holy_strike",     "Holy Strike",      "Imbue weapon with holy light. Bonus vs dark entities.",
            20,  6_000,    1,  "PALADIN", 2);
        add("lay_on_hands",    "Lay on Hands",     "Channel divine energy: heal target for 40% max HP.",
            50,  45_000,   3,  "PALADIN", 3);
        add("divine_shield",   "Divine Shield",    "Become immune to all damage for 5 seconds.",
            60,  90_000,   12, "PALADIN", 4);
        add("consecrate",      "Consecrate",       "Sanctify ground: dark entities take continuous damage.",
            40,  20_000,   8,  "PALADIN", 3);
        add("resurrection",    "Resurrection",     "[ULTIMATE] Revive a fallen ally with 50% HP.",
            100, 300_000,  30, "PALADIN", 5);

        // ── SHADOW KNIGHT ─────────────────────────────────────────────────────
        add("soul_drain",      "Soul Drain",       "Drain life from target, heal yourself for half.",
            25,  6_000,    1,  "SHADOW_KNIGHT", 2);
        add("dark_pact",       "Dark Pact",        "Sacrifice 20% HP to double magic DMG for 15s.",
            0,   30_000,   5,  "SHADOW_KNIGHT", 3);
        add("death_mark",      "Death Mark",       "Mark target: kill them in 30s to reset all cooldowns.",
            50,  60_000,   12, "SHADOW_KNIGHT", 4);
        add("shadow_form",     "Shadow Form",      "[ULTIMATE] Become shadow: immune + massive DMG for 20s.",
            80,  180_000,  25, "SHADOW_KNIGHT", 5);

        // ── RANGER ────────────────────────────────────────────────────────────
        add("precise_shot",    "Precise Shot",     "Careful aim: 250% DMG, ignores 50% armor.",
            20,  8_000,    1,  "RANGER", 2);
        add("multishot",       "Multishot",        "Fire 5 arrows simultaneously in a spread.",
            35,  12_000,   5,  "RANGER", 3);
        add("snare_trap",      "Snare Trap",       "Place invisible trap that roots first enemy for 4s.",
            25,  15_000,   3,  "RANGER", 2);
        add("camouflage",      "Camouflage",       "Blend in: nearly invisible for 10 seconds.",
            30,  30_000,   8,  "RANGER", 3);
        add("storm_arrows",    "Storm of Arrows",  "[ULTIMATE] Rain 50 arrows on area over 5 seconds.",
            90,  120_000,  20, "RANGER", 5);

        // ── ASSASSIN ──────────────────────────────────────────────────────────
        add("shadow_step",     "Shadow Step",      "Teleport behind target: 300% crit damage.",
            30,  12_000,   1,  "ASSASSIN", 2);
        add("vanish",          "Vanish",           "Fully invisible 8s. Breaking stealth = +100% DMG.",
            40,  20_000,   3,  "ASSASSIN", 3);
        add("poison_blade",    "Poison Blade",     "Apply deadly poison dealing damage over 15 seconds.",
            20,  8_000,    5,  "ASSASSIN", 2);
        add("execution",       "Execution",        "[ULTIMATE] Instantly kill target below 20% HP.",
            70,  90_000,   20, "ASSASSIN", 5);

        // ── MAGE ──────────────────────────────────────────────────────────────
        add("fireball",        "Fireball",         "Hurling fireball exploding on impact, burning all nearby.",
            20,  3_000,    1,  "MAGE", 1);
        add("ice_lance",       "Ice Lance",        "Ice spear: heavy magic DMG + slow.",
            25,  5_000,    3,  "MAGE", 2);
        add("lightning_bolt",  "Lightning Bolt",   "Lightning chains to up to 3 nearby enemies.",
            30,  6_000,    5,  "MAGE", 2);
        add("arcane_shield",   "Arcane Shield",    "Magic barrier absorbing 500 damage.",
            50,  25_000,   8,  "MAGE", 3);
        add("blink",           "Blink",            "Teleport up to 20 blocks in look direction.",
            20,  8_000,    3,  "MAGE", 2);
        add("time_stop",       "Time Stop",        "[ULTIMATE] Freeze all enemies in large radius for 5s.",
            100, 180_000,  25, "MAGE", 5);

        // ── NECROMANCER ───────────────────────────────────────────────────────
        add("raise_dead",      "Raise Dead",       "Animate a nearby corpse as undead soldier.",
            30,  5_000,    1,  "NECROMANCER", 2);
        add("death_coil",      "Death Coil",       "Death energy: damages enemies, heals undead allies.",
            25,  5_000,    3,  "NECROMANCER", 2);
        add("bone_armor",      "Bone Armor",       "Bone fragments absorb damage, deal damage when hit.",
            35,  18_000,   5,  "NECROMANCER", 3);
        add("plague",          "Plague",           "Deadly disease that spreads to nearby enemies.",
            40,  20_000,   8,  "NECROMANCER", 3);
        add("lich_form",       "Lich Form",        "[ULTIMATE] Transform into Lich: massive dark power.",
            100, 300_000,  30, "NECROMANCER", 5);

        // ── SUMMONER ──────────────────────────────────────────────────────────
        add("fire_elemental",  "Fire Elemental",   "Summon a fire elemental that burns enemies.",
            40,  60_000,   1,  "SUMMONER", 2);
        add("earth_golem",     "Earth Golem",      "Call a stone golem: heavy hits, absorbs damage.",
            60,  90_000,   5,  "SUMMONER", 3);
        add("spirit_pack",     "Spirit Pack",      "Summon 3 spirit wolves tracking your target.",
            50,  30_000,   8,  "SUMMONER", 3);
        add("ancient_dragon",  "Ancient Dragon",   "[ULTIMATE] Summon an ancient dragon for 60 seconds.",
            100, 600_000,  35, "SUMMONER", 5);

        // ── CLERIC ────────────────────────────────────────────────────────────
        add("heal",            "Heal",             "Restore 30% max HP to target ally.",
            25,  5_000,    1,  "CLERIC", 1);
        add("holy_nova",       "Holy Nova",        "Burst: heals allies and damages undead nearby.",
            40,  10_000,   5,  "CLERIC", 3);
        add("smite",           "Smite",            "Channel divine wrath: heavy holy damage to one enemy.",
            25,  6_000,    3,  "CLERIC", 2);
        add("mass_resurrect",  "Mass Resurrection","[ULTIMATE] Revive all fallen allies within 30 blocks.",
            100, 600_000,  35, "CLERIC", 5);

        // ── DRUID ─────────────────────────────────────────────────────────────
        add("bear_form",       "Bear Form",        "Transform: +80% HP, +40% DEF, powerful melee attacks.",
            30,  15_000,   1,  "DRUID", 2);
        add("cat_form",        "Cat Form",         "Transform: +60% speed, stealth, claw attacks.",
            25,  12_000,   3,  "DRUID", 2);
        add("entangle",        "Entangle",         "Root all enemies in area with magical vines for 4s.",
            35,  15_000,   5,  "DRUID", 3);
        add("regrowth",        "Regrowth",         "Healing-over-time effect: restores HP for 20 seconds.",
            25,  10_000,   3,  "DRUID", 2);
        add("hurricane",       "Hurricane",        "[ULTIMATE] Massive hurricane: knockback + wind damage.",
            100, 180_000,  25, "DRUID", 5);

        // ── MONK ──────────────────────────────────────────────────────────────
        add("tiger_palm",      "Tiger Palm",       "Swift strike 120% DMG. Next skill CD -50%.",
            15,  4_000,    1,  "MONK", 1);
        add("crane_kick",      "Spinning Crane Kick","Kick in circle hitting all enemies for 150% DMG.",
            25,  8_000,    3,  "MONK", 2);
        add("iron_skin",       "Iron Skin",        "[Passive] -15% physical DMG when not in heavy armor.",
            0,   0,        5,  "MONK", 2);
        add("thousand_fists",  "Thousand Fists",   "[ULTIMATE] 20 rapid strikes over 3 seconds.",
            60,  120_000,  20, "MONK", 5);

        // ── ALCHEMIST ─────────────────────────────────────────────────────────
        add("alch_bomb",       "Alchemical Bomb",  "Throw an explosive bottle dealing area damage.",
            20,  6_000,    1,  "ALCHEMIST", 2);
        add("transmute",       "Transmute",        "Convert one material to another (with 20% loss).",
            40,  30_000,   5,  "ALCHEMIST", 3);
        add("super_potion",    "Super Potion",     "Brew an instant potion of maximum potency.",
            60,  60_000,   10, "ALCHEMIST", 3);

        // ── BARD ──────────────────────────────────────────────────────────────
        add("song_of_war",     "Song of War",      "Battle anthem: +30% DMG to all allies for 30s.",
            30,  40_000,   1,  "BARD", 2);
        add("dissonance",      "Dissonance",       "Cacophonous melody: confuses enemies, random attacks.",
            35,  20_000,   5,  "BARD", 3);
        add("healing_hymn",    "Healing Hymn",     "Restorative song: heals all nearby allies for 5s.",
            40,  25_000,   3,  "BARD", 2);
        add("ballad_heroes",   "Ballad of Heroes", "[ULTIMATE] Inspire team: all stats +50% for 45s.",
            100, 300_000,  25, "BARD", 5);
    }

    private static void add(String id, String name, String desc,
                             double energy, long cd, int lvl,
                             String cls, int cost) {
        SKILLS.put(id, new SkillDefinition(id, name, desc, energy, cd, lvl, cls, cost));
    }

    public static SkillDefinition get(String id) {
        return SKILLS.get(id);
    }

    public static Collection<SkillDefinition> getAll() {
        return Collections.unmodifiableCollection(SKILLS.values());
    }

    public static List<SkillDefinition> getForClass(String playerClass) {
        List<SkillDefinition> result = new ArrayList<>();
        for (SkillDefinition skill : SKILLS.values()) {
            String req = skill.getClassRequirement();
            if (req == null || req.equalsIgnoreCase(playerClass)) {
                result.add(skill);
            }
        }
        return result;
    }

    public static boolean exists(String id) {
        return SKILLS.containsKey(id);
    }
}
