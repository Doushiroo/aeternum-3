package com.aeternum.systems.leveling;

import com.aeternum.data.PlayerData;
import com.aeternum.systems.titles.TitleSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LevelingSystem {

    public static void tick(ServerPlayer player, PlayerData data) {
        while (data.canLevelUp()) {
            data.levelUp();
            onLevelUp(player, data);
        }
    }

    public static void addXp(ServerPlayer player, PlayerData data, long amount, String source) {
        data.addExperience(amount);
        // Small feedback only if significant XP
        if (amount >= 50) {
            player.sendSystemMessage(Component.literal("§7+§e" + amount + " XP §8(" + source + ")"));
        }
    }

    private static void onLevelUp(ServerPlayer player, PlayerData data) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l  ══════ LEVEL UP! ══════"));
        player.sendSystemMessage(Component.literal("§eYou are now level §6§l" + data.getLevel() + "§e!"));
        player.sendSystemMessage(Component.literal("§7  +3 Skill Points  |  +1 Profession Point"));
        player.sendSystemMessage(Component.literal("§7  HP: §a+" + 15 + "  §7EN: §b+" + 8 + "  §7ATK: §c+" + 1.5));
        player.sendSystemMessage(Component.literal("§6§l  ═══════════════════════"));
        player.sendSystemMessage(Component.literal(""));

        // Heal on level up
        player.heal(10);

        // Check title conditions
        TitleSystem.checkAndGrantTitles(player, data);

        // Heal vanilla health too
        player.setHealth(player.getMaxHealth());
    }

    /** Called when completing a quest, crafting, taming, exploring etc. */
    public static void addXpForAction(ServerPlayer player, PlayerData data, XpAction action) {
        addXp(player, data, action.getAmount(), action.getLabel());
    }

    public enum XpAction {
        CRAFT_ITEM       ("Crafting",       5),
        SMELT_ITEM       ("Smelting",       3),
        BREW_POTION      ("Brewing",        8),
        ENCHANT_ITEM     ("Enchanting",    15),
        TAME_CREATURE    ("Taming",        50),
        DISCOVER_BIOME   ("New Biome",     30),
        COMPLETE_QUEST   ("Quest",        100),
        FISH_RARE        ("Rare Catch",    25),
        MINE_RARE_ORE    ("Mining",        10),
        BUILD_STRUCTURE  ("Building",       2),
        HEAL_ALLY        ("Healing Ally",  10),
        TRADE_SUCCESS    ("Trading",        5),
        BOSS_KILL        ("Boss Defeat", 1000),
        OLYMPIAD_WIN     ("Olympiad Win",  200),
        DIPLOMACY        ("Diplomacy",     40);

        private final String label;
        private final long amount;

        XpAction(String label, long amount) {
            this.label = label;
            this.amount = amount;
        }

        public String getLabel() { return label; }
        public long getAmount() { return amount; }
    }
}
