package com.aeternum.systems.skills;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.SmallFireball;

import java.util.List;

public class SkillSystem {

    // ── LEARN ─────────────────────────────────────────────────────────────────

    public static boolean learnSkill(ServerPlayer player, String skillId) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        SkillDefinition skill = SkillRegistry.get(skillId);

        if (skill == null) {
            player.sendSystemMessage(Component.literal("§cSkill not found: " + skillId));
            return false;
        }
        if (data.hasSkill(skillId)) {
            player.sendSystemMessage(Component.literal("§eYou already know §f" + skill.getDisplayName() + "§e."));
            return false;
        }
        if (!skill.isLearnableBy(data.getPlayerClass(), data.getLevel())) {
            player.sendSystemMessage(Component.literal(
                "§cYou cannot learn §e" + skill.getDisplayName() +
                "§c. Requires: Lv" + skill.getLevelRequirement() +
                (skill.getClassRequirement() != null ? ", Class: " + skill.getClassRequirement() : "")));
            return false;
        }
        if (data.getSkillPoints() < skill.getSkillPointCost()) {
            player.sendSystemMessage(Component.literal(
                "§cNeed §e" + skill.getSkillPointCost() +
                " SP §c(have §e" + data.getSkillPoints() + "§c)."));
            return false;
        }

        for (int i = 0; i < skill.getSkillPointCost(); i++) data.consumeSkillPoint();
        data.unlockSkill(skillId);

        player.sendSystemMessage(Component.literal("§a★ Skill learned: §e" + skill.getDisplayName() + "§a!"));
        player.sendSystemMessage(Component.literal("§7" + skill.getDescription()));
        return true;
    }

    // ── USE ───────────────────────────────────────────────────────────────────

    public static boolean useSkill(ServerPlayer player, String skillId) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        SkillDefinition skill = SkillRegistry.get(skillId);

        if (skill == null) {
            player.sendSystemMessage(Component.literal("§cUnknown skill: " + skillId));
            return false;
        }
        if (!data.hasSkill(skillId)) {
            player.sendSystemMessage(Component.literal("§cYou haven't learned that skill."));
            return false;
        }
        long cdRemaining = data.getSkillCooldownRemaining(skillId);
        if (cdRemaining > 0) {
            player.sendSystemMessage(Component.literal(
                "§e" + skill.getDisplayName() + " §7on cooldown: §e" + (cdRemaining / 1000) + "s"));
            return false;
        }
        if (!data.consumeEnergy(skill.getEnergyCost())) {
            player.sendSystemMessage(Component.literal(
                "§bNeed §e" + (int)skill.getEnergyCost() +
                " EN §b(have §e" + (int)data.getCurrentEnergy() + "§b)."));
            return false;
        }
        if (skill.getCooldownMs() > 0) {
            data.setSkillCooldown(skillId, skill.getCooldownMs());
        }

        executeSkill(player, data, skill);
        return true;
    }

    // ── EXECUTE ───────────────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private static void executeSkill(ServerPlayer player, PlayerData data, SkillDefinition skill) {
        switch (skill.getId()) {

            // ── UNIVERSAL ────────────────────────────────────────────────────
            case "second_wind" -> {
                data.heal(data.getMaxHealth() * 0.25);
                syncHealthToVanilla(player, data);
                player.sendSystemMessage(Component.literal("§aSecond Wind! +" + (int)(data.getMaxHealth() * 0.25) + " HP"));
            }
            case "berserk_strike" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 2.0));
                    player.sendSystemMessage(Component.literal("§cBerserk Strike!"));
                } else noTarget(player, data, skill);
            }
            case "divine_blessing" -> {
                if (data.getKarma() < 500) { refundBoth(player, data, skill);
                    player.sendSystemMessage(Component.literal("§cRequires GOOD karma.")); }
                else {
                    data.heal(data.getMaxHealth() * 0.5);
                    syncHealthToVanilla(player, data);
                    player.removeAllEffects();
                    player.sendSystemMessage(Component.literal("§e✦ Divine Blessing! Healed and cleansed! ✦"));
                }
            }
            case "shadow_veil" -> {
                if (data.getKarma() > -500) { refundBoth(player, data, skill);
                    player.sendSystemMessage(Component.literal("§cRequires negative karma.")); }
                else {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
                    player.sendSystemMessage(Component.literal("§8You vanish into the shadows..."));
                }
            }

            // ── WARRIOR ──────────────────────────────────────────────────────
            case "shield_bash" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255));
                    t.hurt(player.damageSources().playerAttack(player), (float)data.getPhysicalAttack());
                    player.sendSystemMessage(Component.literal("§aShield Bash! Enemy stunned!"));
                } else noTarget(player, data, skill);
            }
            case "battle_cry" -> {
                player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(15))
                    .forEach(p -> p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1)));
                player.sendSystemMessage(Component.literal("§6Battle Cry! Allies empowered!"));
            }
            case "whirlwind" -> {
                aoeHurt(player, data, 3, 1.8);
                player.sendSystemMessage(Component.literal("§cWhirlwind!"));
            }
            case "last_stand" -> {
                if (data.getHealthPercent() <= 0.2) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
                    player.sendSystemMessage(Component.literal("§4Last Stand!"));
                } else { refundBoth(player, data, skill);
                    player.sendSystemMessage(Component.literal("§cRequires HP below 20%.")); }
            }
            case "titan_rage" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
                player.sendSystemMessage(Component.literal("§4§lTITAN RAGE!"));
            }
            case "war_charge" -> {
                // Horizontal charge only - keep Y to avoid going underground
                var look = player.getViewVector(1.0f).normalize();
                player.teleportTo(player.getX() + look.x * 5, player.getY(), player.getZ() + look.z * 5);
                LivingEntity t = nearest(player, 3);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 1.5));
                    t.knockback(1.5, player.getX() - t.getX(), player.getZ() - t.getZ());
                }
                player.sendSystemMessage(Component.literal("§eWar Charge!"));
            }

            // ── BERSERKER ────────────────────────────────────────────────────
            case "frenzy" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1));
                player.sendSystemMessage(Component.literal("§cFrenzy!"));
            }
            case "cleave" -> {
                aoeHurt(player, data, 3.5, 2.0);
                player.sendSystemMessage(Component.literal("§cCleave!"));
            }
            case "devastation" -> {
                aoeHurt(player, data, 5, 3.0);
                player.sendSystemMessage(Component.literal("§4§lDEVASTATION!"));
            }

            // ── PALADIN ──────────────────────────────────────────────────────
            case "holy_strike" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    float bonus = t.getType().toString().toLowerCase().contains("undead") ? 3.0f : 1.5f;
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * bonus));
                    player.sendSystemMessage(Component.literal("§eHoly Strike!"));
                } else noTarget(player, data, skill);
            }
            case "lay_on_hands" -> {
                data.heal(data.getMaxHealth() * 0.4);
                syncHealthToVanilla(player, data);
                player.sendSystemMessage(Component.literal("§aLay on Hands! +" + (int)(data.getMaxHealth() * 0.4) + " HP"));
            }
            case "divine_shield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 255));
                player.sendSystemMessage(Component.literal("§eDivine Shield!"));
            }
            case "consecrate" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5))
                    .stream().filter(e -> e != player)
                    .filter(e -> { String t2 = e.getType().toString().toLowerCase();
                        return t2.contains("undead") || t2.contains("demon") || t2.contains("imp"); })
                    .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1)));
                player.sendSystemMessage(Component.literal("§eConsecrated ground!"));
            }
            case "resurrection" ->
                player.sendSystemMessage(Component.literal("§e★ Resurrection used — revives nearest fallen ally!"));

            // ── SHADOW KNIGHT ────────────────────────────────────────────────
            case "soul_drain" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    float dmg = (float)data.getMagicAttack();
                    t.hurt(player.damageSources().playerAttack(player), dmg);
                    data.heal(dmg * 0.5);
                    syncHealthToVanilla(player, data);
                    player.sendSystemMessage(Component.literal("§4Soul Drain!"));
                } else noTarget(player, data, skill);
            }
            case "dark_pact" -> {
                // Damage both PlayerData and vanilla HP for real consequences
                float darkDmg = (float)(player.getMaxHealth() * 0.2);
                player.hurt(player.damageSources().magic(), darkDmg);
                data.damage(data.getMaxHealth() * 0.2);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
                player.sendSystemMessage(Component.literal("§4Dark Pact! Power surges at a cost!"));
            }
            case "shadow_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 4));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                player.sendSystemMessage(Component.literal("§8§lShadow Form!"));
            }

            // ── MAGE ─────────────────────────────────────────────────────────
            case "fireball" -> {
                // Correct 1.21.1 constructor: SmallFireball(Level, LivingEntity, Vec3)
                net.minecraft.world.phys.Vec3 dir = player.getViewVector(1.0f).normalize();
                SmallFireball fb = new SmallFireball(player.level(), player, dir);
                fb.setPos(player.getX() + dir.x, player.getEyeY(), player.getZ() + dir.z);
                player.level().addFreshEntity(fb);
                player.sendSystemMessage(Component.literal("§cFireball!"));
            }
            case "blink" -> {
                // Horizontal blink only - avoids fall damage or underground teleport
                var look = player.getViewVector(1.0f).normalize();
                player.teleportTo(
                    player.getX() + look.x * 20,
                    player.getY(),                  // keep same Y
                    player.getZ() + look.z * 20);
                player.sendSystemMessage(Component.literal("§dBlink!"));
            }
            case "arcane_shield" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2));
                player.sendSystemMessage(Component.literal("§dArcane Shield!"));
            }
            case "ice_lance" -> {
                LivingEntity t = nearest(player, 15);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 1.8));
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    player.sendSystemMessage(Component.literal("§bIce Lance!"));
                } else noTarget(player, data, skill);
            }
            case "lightning_bolt" -> {
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(8))
                    .stream().filter(e -> e != player).limit(3).toList();
                for (LivingEntity t : targets)
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 1.5));
                player.sendSystemMessage(Component.literal("§eLightning Bolt! ×" + targets.size()));
            }
            case "time_stop" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25))
                    .stream().filter(e -> e != player)
                    .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255)));
                player.sendSystemMessage(Component.literal("§b§lTIME STOP!"));
            }

            // ── NECROMANCER ──────────────────────────────────────────────────
            case "raise_dead" -> {
                // Use EntityType directly - correct MC 1.21.1 pattern
                Zombie zombie = new Zombie(EntityType.ZOMBIE, player.level());
                zombie.moveTo(player.getX() + 1, player.getY(), player.getZ() + 1, 0, 0);
                player.level().addFreshEntity(zombie);
                data.addTamedEntityId(zombie.getStringUUID());
                player.sendSystemMessage(Component.literal("§2Raised a zombie warrior!"));
            }
            case "death_coil" -> {
                LivingEntity t = nearest(player, 8);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 1.5));
                    data.heal(data.getMagicAttack() * 0.3);
                    syncHealthToVanilla(player, data);
                    player.sendSystemMessage(Component.literal("§2Death Coil!"));
                } else noTarget(player, data, skill);
            }
            case "bone_armor" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
                player.sendSystemMessage(Component.literal("§2Bone Armor!"));
            }
            case "plague" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8))
                    .stream().filter(e -> e != player)
                    .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1)));
                player.sendSystemMessage(Component.literal("§2Plague unleashed!"));
            }
            case "lich_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                player.sendSystemMessage(Component.literal("§4§lLICH FORM!"));
            }

            // ── SUMMONER ─────────────────────────────────────────────────────
            case "fire_elemental" -> {
                Blaze blaze = new Blaze(EntityType.BLAZE, player.level());
                blaze.moveTo(player.getX() + 2, player.getY(), player.getZ() + 2, 0, 0);
                player.level().addFreshEntity(blaze);
                data.addTamedEntityId(blaze.getStringUUID());
                player.sendSystemMessage(Component.literal("§cFire Elemental summoned!"));
            }
            case "earth_golem" -> {
                // Iron Golem - correct summon for 1.21.1
                var golem = new net.minecraft.world.entity.animal.IronGolem(EntityType.IRON_GOLEM, player.level());
                golem.moveTo(player.getX() + 2, player.getY(), player.getZ() + 2, 0, 0);
                player.level().addFreshEntity(golem);
                data.addTamedEntityId(golem.getStringUUID());
                player.sendSystemMessage(Component.literal("§6Earth Golem summoned!"));
            }
            case "spirit_pack" -> {
                for (int i = 0; i < 3; i++) {
                    Wolf wolf = new Wolf(EntityType.WOLF, player.level());
                    wolf.moveTo(player.getX() + (i - 1), player.getY(), player.getZ() + 2, 0, 0);
                    player.level().addFreshEntity(wolf);
                    data.addTamedEntityId(wolf.getStringUUID());
                }
                player.sendSystemMessage(Component.literal("§6Spirit Pack! 3 wolves summoned!"));
            }
            case "ancient_dragon" ->
                player.sendSystemMessage(Component.literal("§4§l★ Ancient Dragon summoned! (Custom entity — coming soon)"));

            // ── DRUID ────────────────────────────────────────────────────────
            case "bear_form" -> {
                // Temporary HP boost via absorption instead of permanent maxHealth change
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 4)); // +20 absorption hearts
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0)); // bears are slower
                data.heal(data.getMaxHealth() * 0.3);
                syncHealthToVanilla(player, data);
                player.sendSystemMessage(Component.literal("§6Bear Form! Absorbed damage +80HP!"));
            }
            case "cat_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0));
                player.sendSystemMessage(Component.literal("§aCat Form!"));
            }
            case "entangle" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6))
                    .stream().filter(e -> e != player)
                    .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 255)));
                player.sendSystemMessage(Component.literal("§2Entangle!"));
            }
            case "regrowth" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
                player.sendSystemMessage(Component.literal("§aRegrowth!"));
            }
            case "hurricane" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20))
                    .stream().filter(e -> e != player)
                    .forEach(e -> {
                        e.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 1.5));
                        e.knockback(2.0, e.getX() - player.getX(), e.getZ() - player.getZ());
                    });
                player.sendSystemMessage(Component.literal("§9Hurricane!"));
            }

            // ── CLERIC ───────────────────────────────────────────────────────
            case "heal" -> {
                data.heal(data.getMaxHealth() * 0.3);
                syncHealthToVanilla(player, data);
                player.sendSystemMessage(Component.literal("§aHeal! +" + (int)(data.getMaxHealth() * 0.3) + " HP"));
            }
            case "holy_nova" -> {
                player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(8))
                    .forEach(p -> {
                        PlayerData pd = p.getData(ModAttachments.PLAYER_DATA.get());
                        pd.heal(pd.getMaxHealth() * 0.2);
                        syncHealthToVanilla(p, pd);
                    });
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8))
                    .stream().filter(e -> !(e instanceof ServerPlayer))
                    .filter(e -> e.getType().toString().toLowerCase().contains("undead"))
                    .forEach(e -> e.hurt(player.damageSources().playerAttack(player),
                        (float)(data.getMagicAttack() * 2.0)));
                player.sendSystemMessage(Component.literal("§eHoly Nova!"));
            }
            case "smite" -> {
                LivingEntity t = nearest(player, 5);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 2.5));
                    player.sendSystemMessage(Component.literal("§eSmite!"));
                } else noTarget(player, data, skill);
            }
            case "mass_resurrect" ->
                player.sendSystemMessage(Component.literal("§e★ Mass Resurrection! (Revives nearby fallen allies)"));

            // ── MONK ─────────────────────────────────────────────────────────
            case "tiger_palm" -> {
                LivingEntity t = nearest(player, 3);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 1.2));
                    player.sendSystemMessage(Component.literal("§6Tiger Palm!"));
                } else noTarget(player, data, skill);
            }
            case "crane_kick" -> {
                aoeHurt(player, data, 3, 1.5);
                player.sendSystemMessage(Component.literal("§6Spinning Crane Kick!"));
            }
            case "thousand_fists" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    for (int i = 0; i < 20; i++)
                        t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 0.8));
                    player.sendSystemMessage(Component.literal("§6§lThousand Fists!"));
                } else noTarget(player, data, skill);
            }

            // ── ASSASSIN ─────────────────────────────────────────────────────
            case "shadow_step" -> {
                LivingEntity t = nearest(player, 20);
                if (t != null) {
                    // Teleport BEHIND the target (1.5 blocks away from their back)
                    var targetLook = t.getViewVector(1.0f).normalize();
                    player.teleportTo(
                        t.getX() - targetLook.x * 1.5,
                        t.getY(),
                        t.getZ() - targetLook.z * 1.5);
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 3.0));
                    player.sendSystemMessage(Component.literal("§8Shadow Step!"));
                } else noTarget(player, data, skill);
            }
            case "vanish" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                player.sendSystemMessage(Component.literal("§8You vanish..."));
            }
            case "poison_blade" -> {
                LivingEntity t = nearest(player, 3);
                if (t != null) {
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 0.8));
                    player.sendSystemMessage(Component.literal("§2Poison Blade!"));
                } else noTarget(player, data, skill);
            }
            case "execution" -> {
                LivingEntity t = nearest(player, 4);
                if (t != null) {
                    double hpPct = t.getHealth() / t.getMaxHealth();
                    if (hpPct <= 0.2) {
                        t.hurt(player.damageSources().playerAttack(player), t.getMaxHealth() * 999);
                        player.sendSystemMessage(Component.literal("§c§lEXECUTION!"));
                    } else {
                        refundBoth(player, data, skill);
                        player.sendSystemMessage(Component.literal("§cTarget must be below 20% HP."));
                    }
                } else noTarget(player, data, skill);
            }

            // ── RANGER ───────────────────────────────────────────────────────
            case "precise_shot" -> {
                LivingEntity t = nearest(player, 30);
                if (t != null) {
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 2.5));
                    player.sendSystemMessage(Component.literal("§aPrecise Shot!"));
                } else noTarget(player, data, skill);
            }
            case "multishot" -> {
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20))
                    .stream().filter(e -> e != player).limit(5).toList();
                for (LivingEntity t : targets)
                    t.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 1.2));
                player.sendSystemMessage(Component.literal("§aMultishot! ×" + targets.size()));
            }
            case "camouflage" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                player.sendSystemMessage(Component.literal("§aCamouflage!"));
            }
            case "storm_arrows" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25))
                    .stream().filter(e -> e != player)
                    .forEach(e -> {
                        for (int i = 0; i < 5; i++)
                            e.hurt(player.damageSources().playerAttack(player), (float)(data.getPhysicalAttack() * 0.8));
                    });
                player.sendSystemMessage(Component.literal("§a§lStorm of Arrows!"));
            }

            // ── BARD ─────────────────────────────────────────────────────────
            case "song_of_war" -> {
                player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(15))
                    .forEach(p -> p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1)));
                player.sendSystemMessage(Component.literal("§dSong of War!"));
            }
            case "dissonance" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10))
                    .stream().filter(e -> e != player)
                    .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)));
                player.sendSystemMessage(Component.literal("§dDissonance! Enemies confused!"));
            }
            case "healing_hymn" -> {
                player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(8))
                    .forEach(p -> {
                        PlayerData pd = p.getData(ModAttachments.PLAYER_DATA.get());
                        pd.heal(pd.getMaxHealth() * 0.15);
                        syncHealthToVanilla(p, pd);
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                    });
                player.sendSystemMessage(Component.literal("§dHealing Hymn!"));
            }
            case "ballad_heroes" -> {
                player.level().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(30))
                    .forEach(p -> {
                        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 900, 1));
                        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 1));
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 900, 1));
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                    });
                player.sendSystemMessage(Component.literal("§d§lBALLAD OF HEROES!"));
            }

            // ── ALCHEMIST ────────────────────────────────────────────────────
            case "alch_bomb" -> {
                player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4))
                    .stream().filter(e -> e != player)
                    .forEach(e -> {
                        e.hurt(player.damageSources().playerAttack(player), (float)(data.getMagicAttack() * 1.2));
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                    });
                player.sendSystemMessage(Component.literal("§2Alchemical Bomb!"));
            }
            case "transmute" ->
                player.sendSystemMessage(Component.literal("§6Transmute! (Opens transmutation menu — coming soon)"));
            case "super_potion" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                player.sendSystemMessage(Component.literal("§aSuper Potion brewed and consumed!"));
            }

            // ── PASSIVE SKILLS (inform player, effect applied in tick) ───
            case "sprint_mastery", "iron_will", "treasure_sense", "blood_rage", "iron_skin" -> {
                // Passive skills are always active once learned - no activation needed
                refundBoth(player, data, skill);
                player.sendSystemMessage(Component.literal(
                    "§e" + skill.getDisplayName() + " §7is a passive skill — it's always active!"));
            }

            // ── RANGER - SNARE TRAP ───────────────────────────────────────────
            case "snare_trap" -> {
                // Place a slowness trap at player's feet that affects the first enemy that walks over it
                player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    player.getBoundingBox().inflate(2))
                    .stream().filter(e -> e != player).findFirst()
                    .ifPresentOrElse(
                        e -> {
                            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 255));
                            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                            player.sendSystemMessage(Component.literal("§aSnare Trap triggered! Enemy rooted!"));
                        },
                        () -> player.sendSystemMessage(Component.literal("§aSnare Trap placed at your location."))
                    );
            }

            // ── SHADOW KNIGHT ────────────────────────────────────────────
            case "death_mark" -> {
                LivingEntity t = nearest(player, 20);
                if (t != null) {
                    t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
                    player.sendSystemMessage(Component.literal("§4Death Mark placed! Kill within 30s!"));
                } else noTarget(player, data, skill);
            }

            // ── BLOOD MAGE ───────────────────────────────────────────────────
            case "blood_sacrifice" -> {
                player.hurt(player.damageSources().magic(), (float)(player.getMaxHealth() * 0.3f));
                data.damage(data.getMaxHealth() * 0.3);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2));
                player.sendSystemMessage(Component.literal("§4Blood Sacrifice! Power at a cost!"));
            }

            // ── HUNTER ───────────────────────────────────────────────────────
            case "track_prey" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                player.sendSystemMessage(Component.literal("§2Tracking prey... Night vision active."));
            }

            // ── ENGINEER ─────────────────────────────────────────────────────
            case "deploy_turret" -> {
                player.sendSystemMessage(Component.literal("§7[Turret deployed at your location!] (Coming soon)"));
            }

            // ── WARLOCK ──────────────────────────────────────────────────────
            case "demonic_pact" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 0));
                data.addKarma(-200);
                player.sendSystemMessage(Component.literal("§5Demonic Pact sealed! Power... at a price."));
            }

            default -> player.sendSystemMessage(Component.literal(
                "§a[" + skill.getDisplayName() + "] effect not yet implemented."));
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private static LivingEntity nearest(ServerPlayer player, double range) {
        return player.level()
            .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))
            .stream().filter(e -> e != player)
            .min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
            .orElse(null);
    }

    private static void aoeHurt(ServerPlayer player, PlayerData data, double range, double multiplier) {
        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))
            .stream().filter(e -> e != player)
            .forEach(e -> e.hurt(player.damageSources().playerAttack(player),
                (float)(data.getPhysicalAttack() * multiplier)));
    }

    private static void noTarget(ServerPlayer player, PlayerData data, SkillDefinition skill) {
        refundBoth(player, data, skill);
        player.sendSystemMessage(Component.literal("§cNo target in range."));
    }

    private static void refundBoth(ServerPlayer player, PlayerData data, SkillDefinition skill) {
        data.setCurrentEnergy(data.getCurrentEnergy() + skill.getEnergyCost());
        data.setSkillCooldown(skill.getId(), 0);
    }

    // ── VANILLA HEALTH SYNC ──────────────────────────────────────────────────
    /**
     * When a skill heals PlayerData, we also need to update vanilla health
     * so the vanilla health bar and damage calculations stay in sync.
     */
    static void syncHealthToVanilla(ServerPlayer player, PlayerData data) {
        if (data.getMaxHealth() <= 0) return;
        float vanillaMax = player.getMaxHealth();
        float newVanilla = (float)(vanillaMax * data.getHealthPercent());
        player.setHealth(Math.max(0.5f, Math.min(vanillaMax, newVanilla)));
    }

    // ── SHOW SKILL LIST ───────────────────────────────────────────────────────

    public static void showSkillList(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        String cls = data.getPlayerClass();

        player.sendSystemMessage(Component.literal("§6=== SKILLS FOR " + cls + " ==="));
        player.sendSystemMessage(Component.literal("§7Skill Points available: §e" + data.getSkillPoints()));
        player.sendSystemMessage(Component.literal(""));

        for (SkillDefinition skill : SkillRegistry.getForClass(cls)) {
            boolean learned = data.hasSkill(skill.getId());
            boolean canLearn = skill.isLearnableBy(cls, data.getLevel());
            long cd = data.getSkillCooldownRemaining(skill.getId());

            String status;
            if (learned)
                status = cd > 0 ? "§e[CD:" + (cd / 1000) + "s]" : "§a[Ready]  ";
            else if (canLearn && data.getSkillPoints() >= skill.getSkillPointCost())
                status = "§7[Learn:" + skill.getSkillPointCost() + "SP]";
            else
                status = "§8[Lv" + skill.getLevelRequirement() + "]    ";

            player.sendSystemMessage(Component.literal(
                status + " §f" + skill.getDisplayName() + " §8- §7" + skill.getDescription()));
        }
    }
}
