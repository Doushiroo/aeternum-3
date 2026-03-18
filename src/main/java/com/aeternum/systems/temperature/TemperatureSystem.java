package com.aeternum.systems.temperature;

import com.aeternum.data.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TemperatureSystem {

    public static final float NORMAL_TEMP = 37.0f;

    public static void tick(ServerPlayer player, PlayerData data, ServerLevel level) {
        BlockPos pos = player.blockPosition();
        float biomeTemp = getBiomeTemperature(level, pos);
        float worldTemp = applyWorldModifiers(biomeTemp, level);
        float armorInsulation = getArmorInsulation(player);

        // Target body temp based on environment and armor
        // Multiplier 0.25: human body regulates temperature well in normal conditions
        // Only extreme biomes (tundra, nether) should push into danger zone
        float targetTemp;
        if (worldTemp > NORMAL_TEMP) {
            targetTemp = NORMAL_TEMP + (worldTemp - NORMAL_TEMP) * 0.25f;
            targetTemp -= Math.max(0, armorInsulation) * 0.4f; // cooling armor helps
        } else {
            targetTemp = NORMAL_TEMP + (worldTemp - NORMAL_TEMP) * 0.25f;
            targetTemp -= Math.min(0, armorInsulation) * 0.5f; // warming armor helps
        }

        // Move body temp toward target slowly
        float current = data.getBodyTemperature();
        float delta = (targetTemp - current) * 0.003f;  // gentle change ~5min to reach extreme
        data.setBodyTemperature(current + delta);

        // Apply effects based on temp status
        applyTemperatureEffects(player, data);
    }

    private static float getBiomeTemperature(ServerLevel level, BlockPos pos) {
        float biomeTemp = level.getBiome(pos).value().getBaseTemperature();
        float celsius;
        if (biomeTemp <= 0)          celsius = -15.0f;
        else if (biomeTemp < 0.3f)   celsius = -10.0f + (biomeTemp / 0.3f * 10.0f);
        else if (biomeTemp < 0.5f)   celsius = 0.0f + ((biomeTemp - 0.3f) / 0.2f * 15.0f);
        else if (biomeTemp < 1.0f)   celsius = 15.0f + ((biomeTemp - 0.5f) / 0.5f * 15.0f);
        else if (biomeTemp < 1.5f)   celsius = 30.0f + ((biomeTemp - 1.0f) / 0.5f * 15.0f);
        else                          celsius = 45.0f;

        int heightAboveSea = pos.getY() - 62;
        if (heightAboveSea > 0) celsius -= (heightAboveSea / 50.0f) * 0.5f;

        if (level.dimension().equals(Level.NETHER)) celsius = 55.0f;
        if (level.dimension().equals(Level.END))    celsius = 5.0f;

        return celsius;
    }

    private static float applyWorldModifiers(float biomeTemp, ServerLevel level) {
        long time = level.getDayTime() % 24000;
        float timeModifier;
        if (time >= 6000 && time <= 12000) {
            float noonFactor = 1.0f - Math.abs((time - 6000) / 6000.0f);
            timeModifier = noonFactor * 6.0f;
        } else {
            timeModifier = -4.0f;
        }
        float weatherModifier = 0;
        if (level.isThundering()) weatherModifier = -6.0f;
        else if (level.isRaining()) weatherModifier = -3.0f;

        return biomeTemp + timeModifier + weatherModifier;
    }

    private static float getArmorInsulation(ServerPlayer player) {
        float insulation = 0;
        for (ItemStack armorPiece : player.getArmorSlots()) {
            if (armorPiece.isEmpty()) continue;
            if (!(armorPiece.getItem() instanceof ArmorItem)) continue;
            String material = armorPiece.getDescriptionId().toLowerCase();
            if (material.contains("leather"))    insulation -= 3.5f;  // best cold protection
            else if (material.contains("iron"))  insulation += 2.0f;
            else if (material.contains("gold"))  insulation -= 1.0f;
            else if (material.contains("diamond")) insulation += 3.0f;
            else if (material.contains("netherite")) insulation += 5.0f;
        }
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) insulation += 8.0f;
        return insulation;
    }

    private static void applyTemperatureEffects(ServerPlayer player, PlayerData data) {
        switch (data.getTemperatureStatus()) {
            case HEAT_STROKE -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                if (player.tickCount % 40 == 0) {
                    player.hurt(player.damageSources().generic(), 1.0f);
                    player.sendSystemMessage(Component.literal("§c⚠ Heat Stroke! Find shade and water!"));
                }
            }
            case HEAT_EXHAUSTION -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));  // more severe than HOT
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0, false, false));
            }
            case HOT -> {
                // Heat drains energy/strength, not speed
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
            }
            case COLD -> {
                // Cold stiffens muscles - stronger slowness than HOT
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
            }
            case HYPOTHERMIA -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, false));  // worse than COLD
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                if (player.tickCount % 120 == 0) {
                    player.hurt(player.damageSources().generic(), 0.5f);
                    player.sendSystemMessage(Component.literal("§9⚠ Hypothermia! Find warmth immediately!"));
                }
            }
            case FROSTBITE -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
                if (player.tickCount % 80 == 0) {
                    player.hurt(player.damageSources().generic(), 0.8f);
                    player.sendSystemMessage(Component.literal("§b⚠ Frostbite! You are freezing to death!"));
                }
            }
            case NORMAL -> {} // No effects
        }
    }
}
