package com.aeternum.systems.titles;

import com.aeternum.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.*;

public class TitleSystem {

    public static final Map<String, AeternumTitle> ALL_TITLES = new LinkedHashMap<>();

    static {
        reg("adventurer",       "§aAdventurer",          "Reach level 25");
        reg("veteran",          "§6Veteran",             "Reach level 50");
        reg("champion",         "§eChampion",            "Reach level 75");
        reg("legend",           "§6§lLegend",            "Reach level 100");
        reg("the_slayer",       "§cThe Slayer",          "Kill 100 players");
        reg("warlord",          "§4Warlord",             "Kill 500 players");
        reg("boss_hunter",      "§aBoss Hunter",         "Defeat 10 bosses");
        reg("world_ender",      "§4§lWorld Ender",       "Defeat all raid bosses");
        reg("merchant",         "§6Merchant",            "Complete 100 trades");
        reg("merchant_prince",  "§6Merchant Prince",     "Accumulate 1,000,000 AU");
        reg("champion_of_light","§e§l✦ Champion of Light","Reach Holy karma");
        reg("avatar_of_divinity","§e§l★ Avatar of Divinity","Reach Divine karma");
        reg("lord_of_darkness", "§4§l☠ Lord of Darkness","Reach Corrupt karma");
        reg("the_abyss",        "§4§l⚠ The Abyss",       "Reach Abyssal karma");
        reg("noble",            "§e§lNoble",             "Achieve Noble status");
        reg("hero",             "§6§l⚔ Hero",            "Win the Grand Olympiad");
        reg("grand_champion",   "§6§l★ Grand Champion",  "Win the Olympiad overall");
        reg("wanderer",         "§aWanderer",            "Travel 10,000 blocks");
        reg("world_walker",     "§2World Walker",        "Discover 20 biomes");
        reg("beast_master",     "§2Beast Master",        "Tame 5 creatures");
        reg("redeemed",         "§aRedeemed",            "Return from negative to positive karma");
        reg("transcendent",     "§d§lTranscendent",      "Rebirth at max level");
    }

    private static void reg(String id, String display, String req) {
        ALL_TITLES.put(id, new AeternumTitle(id, display, req));
    }

    public static void grantTitle(ServerPlayer player, String titleId) {
        AeternumTitle title = ALL_TITLES.get(titleId);
        if (title == null) return;
        PlayerData data = player.getData(com.aeternum.registry.ModAttachments.PLAYER_DATA.get());
        if (data.getUnlockedTitles().contains(titleId)) return;

        data.unlockTitle(titleId);
        player.sendSystemMessage(Component.literal("§6§l★ NEW TITLE UNLOCKED: " + title.displayName() + " §6★"));
    }

    public static void revokeTitle(ServerPlayer player, String titleId) {
        PlayerData data = player.getData(com.aeternum.registry.ModAttachments.PLAYER_DATA.get());
        data.removeTitle(titleId);
    }

    public static void applyActiveTitleEffects(ServerPlayer player, PlayerData data) {
        String titleId = data.getActiveTitle();
        if (titleId.isEmpty()) return;

        switch (titleId) {
            case "champion_of_light", "avatar_of_divinity" ->
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
            case "lord_of_darkness", "the_abyss" ->
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, false, false));
            case "legend", "transcendent" ->
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 0, false, false));
            case "hero", "grand_champion" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, false));
            }
            default -> {}
        }
    }

    public static void checkAndGrantTitles(ServerPlayer player, PlayerData data) {
        if (data.getLevel() >= 25)  grantTitle(player, "adventurer");
        if (data.getLevel() >= 50)  grantTitle(player, "veteran");
        if (data.getLevel() >= 75)  grantTitle(player, "champion");
        if (data.getLevel() >= 100) grantTitle(player, "legend");
        if (data.getTotalPlayerKills() >= 100) grantTitle(player, "the_slayer");
        if (data.getTotalPlayerKills() >= 500) grantTitle(player, "warlord");
        if (data.getBossesKilled() >= 10)      grantTitle(player, "boss_hunter");
        if (data.getBankBalance() + data.getWalletBalance() >= 1_000_000L) grantTitle(player, "merchant_prince");
        if (data.getKarma() >= 5000)  grantTitle(player, "champion_of_light");
        if (data.getKarma() >= 8000)  grantTitle(player, "avatar_of_divinity");
        if (data.getKarma() <= -5000) grantTitle(player, "lord_of_darkness");
        if (data.getKarma() <= -8000) grantTitle(player, "the_abyss");
        if (data.isNoble())            grantTitle(player, "noble");
        if (data.getRebirthCount() >= 1) grantTitle(player, "transcendent");
        // Stat-based titles
        if (data.getTotalTradesCompleted() >= 100)  grantTitle(player, "merchant");
        if (data.getDiscoveredBiomes().size() >= 20) grantTitle(player, "world_walker");
        if (data.getTamedEntityIds().size() >= 5)    grantTitle(player, "beast_master");
        if (data.getDistanceTraveled() >= 10000L)    grantTitle(player, "wanderer");
        // Redeemed: reached GOOD karma with a history of deaths (proxy for having been in dark times)
        if (data.getKarma() >= 2000 && data.getTotalDeaths() >= 3) grantTitle(player, "redeemed");
    }

    public record AeternumTitle(String id, String displayName, String requirement) {}
}
