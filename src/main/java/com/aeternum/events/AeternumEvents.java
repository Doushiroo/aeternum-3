package com.aeternum.events;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import com.aeternum.systems.karma.KarmaSystem;
import com.aeternum.systems.leveling.LevelingSystem;
import com.aeternum.systems.temperature.TemperatureSystem;
import com.aeternum.systems.titles.TitleSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class AeternumEvents {

    // ── PLAYER TICK ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // Every second

        PlayerData data = serverPlayer.getData(ModAttachments.PLAYER_DATA.get());

        // Check level up
        LevelingSystem.tick(serverPlayer, data);

        // Karma decay
        KarmaSystem.tickKarmaDecay(serverPlayer, data);

        // Karma passive effects (every 5 seconds)
        if (player.tickCount % 100 == 20) {
            KarmaSystem.applyKarmaPassiveEffects(serverPlayer, data);
            TitleSystem.applyActiveTitleEffects(serverPlayer, data);
        }

        // Temperature (every second)
        if (player.level() instanceof ServerLevel serverLevel) {
            TemperatureSystem.tick(serverPlayer, data, serverLevel);
        }

        // ── SYNC vanilla HP → PlayerData HP bar ─────────────────────────────
        // This ensures the HUD reflects real damage taken from mobs/environment
        if (serverPlayer.getMaxHealth() > 0) {
            double vanillaPercent = serverPlayer.getHealth() / serverPlayer.getMaxHealth();
            data.setCurrentHealth(data.getMaxHealth() * vanillaPercent);
        }

        // Clear expired skill cooldowns periodically
        if (player.tickCount % 200 == 80) {
            data.clearExpiredCooldowns();
        }

        // Energy regen
        if (data.getCurrentEnergy() < data.getMaxEnergy()) {
            double regen = data.getMaxEnergy() * 0.02; // 2% per second
            data.setCurrentEnergy(data.getCurrentEnergy() + regen);
        }

        // Stamina regen
        if (data.getCurrentStamina() < data.getMaxStamina()) {
            data.setCurrentStamina(data.getCurrentStamina() + 2.0);
        }

        // ── PASSIVE SKILL EFFECTS (every second) ─────────────────────────────
        // iron_will: reduce CC effects duration by 50%
        if (data.hasSkill("iron_will")) {
            // Copy to list to avoid ConcurrentModificationException
            var activeEffects = new java.util.ArrayList<>(serverPlayer.getActiveEffects());
            for (var effect : activeEffects) {
                // In 1.21.1, effect.getEffect() returns Holder<MobEffect>
                // Compare using .is() with the resource location
                var holder = effect.getEffect();
                boolean isCC = holder.is(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) ||
                               holder.is(net.minecraft.world.effect.MobEffects.WEAKNESS) ||
                               holder.is(net.minecraft.world.effect.MobEffects.NAUSEA) ||
                               holder.is(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
                if (isCC && effect.getDuration() > 20) {
                    serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        holder, effect.getDuration() / 2, effect.getAmplifier(), false, false));
                }
            }
        }
        // sprint_mastery: +15% speed when at full stamina
        if (data.hasSkill("sprint_mastery") && data.getCurrentStamina() >= data.getMaxStamina()) {
            serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, false, false));
        }
        // blood_rage: every 10% HP lost = +8% physical DMG (max +80% at 10% HP)
        if (data.hasSkill("blood_rage")) {
            double hpLostPct = 1.0 - data.getHealthPercent();
            int stacks = (int)(hpLostPct / 0.1); // 0-9 stacks
            if (stacks > 0) {
                serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 40, stacks - 1, false, false));
            }
        }
        // iron_skin: -15% physical DMG when NOT wearing heavy armor
        // getArmorValue() returns total armor points: diamond=20, iron=15, leather=7
        if (data.hasSkill("iron_skin") && serverPlayer.getArmorValue() <= 8) {
            serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false));
        }

        // Check title conditions (every 30 seconds)
        if (player.tickCount % 600 == 40) {
            TitleSystem.checkAndGrantTitles(serverPlayer, data);
        }

        // Random karma events (every 60 seconds)
        if (player.tickCount % 1200 == 60) {
            KarmaSystem.triggerRandomKarmaEvent(serverPlayer, data);
        }
    }

    // ── PLAYER LOGIN ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        PlayerData data = serverPlayer.getData(ModAttachments.PLAYER_DATA.get());

        if (data.isFirstLogin()) {
            data.setFirstLogin(false);
            serverPlayer.sendSystemMessage(Component.literal(""));
            serverPlayer.sendSystemMessage(Component.literal("§6§l  ╔══════════════════════════════════════╗"));
            serverPlayer.sendSystemMessage(Component.literal("§6§l  ║     WELCOME TO AETERNUM!              ║"));
            serverPlayer.sendSystemMessage(Component.literal("§e§l  ║  The Definitive World awaits you.     ║"));
            serverPlayer.sendSystemMessage(Component.literal("§7§l  ║  Use /aeternum to get started.        ║"));
            serverPlayer.sendSystemMessage(Component.literal("§6§l  ╚══════════════════════════════════════╝"));
            serverPlayer.sendSystemMessage(Component.literal(""));
        }

        // Fix #4: Clean up orphaned clan reference (clan wiped on restart)
        if (data.isInClan()) {
            com.aeternum.systems.clans.ClanData clan =
                com.aeternum.systems.clans.ClanSystem.getClanById(data.getClanId());
            if (clan == null) {
                data.setClanId("");
                data.setClanRank("RECRUIT");
                serverPlayer.sendSystemMessage(Component.literal(
                    "§7[Notice] Your clan data was reset (server restart). Use /clan create to rebuild."));
            }
        }

        // Show MOTD
        serverPlayer.sendSystemMessage(Component.literal("§eWelcome back! §7Karma: §e" +
            data.getKarma() + " §7| Level: §e" + data.getLevel() +
            " §7| Wallet: §e" + data.getWalletBalance() + " AU"));
    }

    // ── PLAYER DEATH ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Player died
        if (entity instanceof ServerPlayer deadPlayer) {
            PlayerData data = deadPlayer.getData(ModAttachments.PLAYER_DATA.get());
            data.incrementDeaths();
            data.addKarma(KarmaSystem.KARMA_DEATH_PENALTY);

            // Lose some wallet on death (configurable)
            long lost = (long)(data.getWalletBalance() * 0.05); // 5% wallet loss
            if (lost > 0) {
                data.setWalletBalance(data.getWalletBalance() - lost);
                deadPlayer.sendSystemMessage(Component.literal(
                    "§cYou lost §e" + lost + " AU §cfrom your wallet upon death."));
            }
        }

        // Player killed something
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer killer)) return;

        PlayerData killerData = killer.getData(ModAttachments.PLAYER_DATA.get());

        if (entity instanceof ServerPlayer killedPlayer) {
            // PvP kill
            PlayerData killedData = killedPlayer.getData(ModAttachments.PLAYER_DATA.get());
            killerData.incrementPlayerKills();

            // Karma based on victim's karma
            int karmaChange = switch (killedData.getKarmaLevel()) {
                case ABYSSAL, CORRUPT, WICKED -> KarmaSystem.KARMA_KILL_PLAYER_WITH_BAD_KARMA;
                case DIVINE, HOLY, VIRTUOUS   -> KarmaSystem.KARMA_KILL_PLAYER_GOOD_KARMA;
                default                        -> KarmaSystem.KARMA_KILL_PLAYER_NEUTRAL;
            };
            KarmaSystem.addKarma(killer, killerData, karmaChange, "PvP kill");

            // XP reward for PvP
            long xpReward = 50L + (killedData.getLevel() * 10L);
            LevelingSystem.addXp(killer, killerData, xpReward, "PvP kill");

            // Gold reward
            long goldReward = 20L + (killedData.getLevel() * 5L);
            killerData.receiveToWallet(goldReward);
            killer.sendSystemMessage(Component.literal("§a+§e" + goldReward + " AU §afrom PvP victory!"));

            TitleSystem.checkAndGrantTitles(killer, killerData);

        } else if (entity instanceof LivingEntity mob) {
            // Mob kill
            int mobLevel = 1 + (int)(mob.getMaxHealth() / 10);
            long xpReward = 5L + (mobLevel * 3L);
            LevelingSystem.addXp(killer, killerData, xpReward, "mob kill");

            // Small gold drop
            long goldDrop = 1L + mobLevel;
            killerData.receiveToWallet(goldDrop);

            // Karma for killing undead
            String mobType = entity.getType().toString().toLowerCase();
            if (mobType.contains("zombie") || mobType.contains("skeleton") || mobType.contains("wither")) {
                KarmaSystem.addKarma(killer, killerData, KarmaSystem.KARMA_KILL_UNDEAD, "Destroyed undead");
            }
        }
    }

    // ── CRAFTING XP ───────────────────────────────────────────────────────────

    // Cooldown map: prevents XP farming by spam-crafting (max 1 XP reward per 5s per player)
    private static final Map<UUID, Long> CRAFT_COOLDOWN = new HashMap<>();
    private static final long CRAFT_XP_COOLDOWN_MS = 5_000L;

    @SubscribeEvent
    public void onItemCrafted(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = System.currentTimeMillis();
        Long lastCraft = CRAFT_COOLDOWN.get(player.getUUID());
        if (lastCraft != null && now - lastCraft < CRAFT_XP_COOLDOWN_MS) return;
        CRAFT_COOLDOWN.put(player.getUUID(), now);
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        LevelingSystem.addXpForAction(player, data, LevelingSystem.XpAction.CRAFT_ITEM);
    }

    @SubscribeEvent
    public void onItemSmelted(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        LevelingSystem.addXpForAction(player, data, LevelingSystem.XpAction.SMELT_ITEM);
    }

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        // Rare catch bonus
        if (event.getDrops().stream().anyMatch(s -> s.getItem().toString().contains("treasure")
            || s.getItem().toString().contains("enchanted"))) {
            LevelingSystem.addXpForAction(player, data, LevelingSystem.XpAction.FISH_RARE);
        } else {
            LevelingSystem.addXp(player, data, 2, "Fishing");
        }
    }

    // ── PLAYER RESPAWN ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());

        // Restore health to max on respawn
        data.setCurrentHealth(data.getMaxHealth());
        data.setCurrentEnergy(data.getMaxEnergy());
        data.setCurrentStamina(data.getMaxStamina());
        // Body temperature resets to normal
        data.setBodyTemperature(37.0f);
    }
}
