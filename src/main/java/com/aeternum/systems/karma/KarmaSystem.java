package com.aeternum.systems.karma;

import com.aeternum.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class KarmaSystem {

    public static final int KARMA_KILL_PLAYER_WITH_BAD_KARMA = +30;
    public static final int KARMA_KILL_PLAYER_NEUTRAL         = -40;
    public static final int KARMA_KILL_PLAYER_GOOD_KARMA      = -80;
    public static final int KARMA_KILL_UNDEAD                  = +5;
    public static final int KARMA_KILL_DEMON                   = +25;
    public static final int KARMA_KILL_ANGEL                   = -150;
    public static final int KARMA_KILL_PILLAGER                = +10;
    public static final int KARMA_HELP_PLAYER                  = +15;
    public static final int KARMA_BOSS_KILL                    = +35;
    public static final int KARMA_DEATH_PENALTY                = -10;
    public static final int KARMA_COMPLETE_HOLY_QUEST          = +100;
    public static final int KARMA_COMPLETE_DARK_QUEST          = -80;
    public static final int KARMA_COMPLETE_QUEST               = +10;
    public static final int KARMA_GRIEVE_PLAYER                = -50;
    public static final int KARMA_SUMMON_DEMON                 = -60;
    public static final int KARMA_STOP_RAID                    = +50;

    public static void addKarma(ServerPlayer player, PlayerData data, int amount, String reason) {
        int before = data.getKarma();
        data.addKarma(amount);
        int after = data.getKarma();

        // Only show message for significant karma changes (>=10) to avoid chat spam
        if (Math.abs(amount) >= 10) {
            String sign = amount >= 0 ? "§a+" : "§c";
            player.sendSystemMessage(Component.literal("§7[Karma] " + sign + amount + " §8(" + reason + ")"));
        }

        checkKarmaLevelChange(player, before, after);
    }

    /** Silent karma change - no chat message (for small/frequent changes) */
    public static void addKarmaSilent(PlayerData data, int amount) {
        data.addKarma(amount);
    }

    public static void tickKarmaDecay(ServerPlayer player, PlayerData data) {
        int timer = data.getKarmaDecayTimer() + 1;
        if (timer >= 1200) {
            timer = 0;
            int karma = data.getKarma();
            // Silent decay - no chat message for natural karma drift
            if (karma > 0) addKarmaSilent(data, -1);
            else if (karma < 0) addKarmaSilent(data, 1);
        }
        data.setKarmaDecayTimer(timer);
    }

    public static void applyKarmaPassiveEffects(ServerPlayer player, PlayerData data) {
        switch (data.getKarmaLevel()) {
            case DIVINE -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, false));
            }
            case HOLY -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
            }
            case CORRUPT -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            }
            case ABYSSAL -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            }
            default -> {}
        }
    }

    public static void triggerRandomKarmaEvent(ServerPlayer player, PlayerData data) {
        double chance = Math.random();
        switch (data.getKarmaLevel()) {
            case DIVINE -> {
                if (chance < 0.01) {
                    player.sendSystemMessage(Component.literal("§e✦ An angel watches over you! You feel a divine presence. ✦"));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 3));
                }
            }
            case ABYSSAL -> {
                if (chance < 0.01) {
                    player.sendSystemMessage(Component.literal("§4[A voice from the abyss whispers...] 'Your darkness serves us well.'"));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                    // Damage both vanilla and PlayerData for consistency
                    player.hurt(player.damageSources().magic(), (float)(player.getMaxHealth() * 0.05));
                    data.damage(data.getMaxHealth() * 0.05);
                }
            }
            default -> {}
        }
    }

    /**
     * Returns karma-based reaction level for villagers:
     *  2 = very friendly, 1 = friendly, 0 = neutral, -1 = hostile, -2 = flee
     * TODO: Hook into EntityInteractSpecific or VillagerTradesEvent to apply this
     */
    public static int getVillagerReaction(PlayerData data) {
        int karma = data.getKarma();
        if (karma >= 5000) return 2;
        if (karma >= 1000) return 1;
        if (karma >= -500) return 0;
        if (karma >= -2000) return -1;
        return -2;
    }

    /**
     * Returns karma-based reaction level for pillagers:
     *  2 = allied (won't attack), 1 = neutral, 0 = slightly hostile, -1 = attack
     * TODO: Hook into LivingEntityUseItemEvent or similar to apply this
     */
    public static int getPillagerReaction(PlayerData data) {
        int karma = data.getKarma();
        if (karma <= -5000) return 2;
        if (karma <= -2000) return 1;
        if (karma <= -500) return 0;
        return -1;
    }

    private static void checkKarmaLevelChange(ServerPlayer player, int before, int after) {
        PlayerData.KarmaLevel beforeLevel = levelFor(before);
        PlayerData.KarmaLevel afterLevel  = levelFor(after);
        if (beforeLevel == afterLevel) return;

        String msg = switch (afterLevel) {
            case DIVINE    -> "§e§l✦ You have reached DIVINE karma! Heaven smiles upon you! ✦";
            case HOLY      -> "§eYour karma has reached HOLY. You radiate with light.";
            case VIRTUOUS  -> "§aYou are now VIRTUOUS. The path of honor and light.";
            case GOOD      -> "§aYour karma is GOOD. Villagers appreciate your presence.";
            case NEUTRAL   -> "§7Your karma has returned to NEUTRAL.";
            case SHADY     -> "§cYou have entered SHADY karma. Villagers grow wary of you.";
            case WICKED    -> "§cYour karma is WICKED. Darkness grows around you.";
            case CORRUPT   -> "§4Your karma has become CORRUPT. Fear follows in your wake.";
            case ABYSSAL   -> "§4§l⚠ ABYSSAL karma. Pure darkness consumes you. ⚠";
        };
        player.sendSystemMessage(Component.literal(msg));
    }

    private static PlayerData.KarmaLevel levelFor(int karma) {
        if (karma >= 8000) return PlayerData.KarmaLevel.DIVINE;
        if (karma >= 5000) return PlayerData.KarmaLevel.HOLY;
        if (karma >= 2000) return PlayerData.KarmaLevel.VIRTUOUS;
        if (karma >= 500)  return PlayerData.KarmaLevel.GOOD;
        if (karma > -500)  return PlayerData.KarmaLevel.NEUTRAL;
        if (karma > -2000) return PlayerData.KarmaLevel.SHADY;
        if (karma > -5000) return PlayerData.KarmaLevel.WICKED;
        if (karma > -8000) return PlayerData.KarmaLevel.CORRUPT;
        return PlayerData.KarmaLevel.ABYSSAL;
    }
}
