package com.aeternum.registry;

import com.aeternum.AeternumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AeternumMod.MODID);

    // Celestials
    public static final DeferredHolder<EntityType<?>, EntityType<?>> GUARDIAN_ANGEL =
        ENTITY_TYPES.register("guardian_angel", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(0.8f, 2.0f).build("guardian_angel"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> SERAPH =
        ENTITY_TYPES.register("seraph", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(1.0f, 2.5f).build("seraph"));

    // Infernals
    public static final DeferredHolder<EntityType<?>, EntityType<?>> IMP =
        ENTITY_TYPES.register("imp", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(0.5f, 0.8f).fireImmune().build("imp"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> DEMON_WARRIOR =
        ENTITY_TYPES.register("demon_warrior", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(0.9f, 2.2f).fireImmune().build("demon_warrior"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> HELLHOUND =
        ENTITY_TYPES.register("hellhound", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(1.2f, 1.2f).fireImmune().build("hellhound"));

    // Ocean creatures
    public static final DeferredHolder<EntityType<?>, EntityType<?>> SEA_SERPENT =
        ENTITY_TYPES.register("sea_serpent", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.WATER_CREATURE)
                .sized(1.5f, 3.0f).build("sea_serpent"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> GIANT_CRAB =
        ENTITY_TYPES.register("giant_crab", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.WATER_CREATURE)
                .sized(1.5f, 0.8f).build("giant_crab"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> KRAKEN =
        ENTITY_TYPES.register("kraken", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(3.0f, 2.0f).build("kraken"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> MERFOLK =
        ENTITY_TYPES.register("merfolk", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.WATER_CREATURE)
                .sized(0.8f, 1.8f).build("merfolk"));

    // Sky creatures
    public static final DeferredHolder<EntityType<?>, EntityType<?>> THUNDERBIRD =
        ENTITY_TYPES.register("thunderbird", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(2.0f, 1.5f).build("thunderbird"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> SKY_WHALE =
        ENTITY_TYPES.register("sky_whale", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(4.0f, 2.0f).build("sky_whale"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> GRYPHON =
        ENTITY_TYPES.register("gryphon", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(1.5f, 1.5f).build("gryphon"));

    // Tameable creatures
    public static final DeferredHolder<EntityType<?>, EntityType<?>> DIREWOLF =
        ENTITY_TYPES.register("direwolf", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(1.0f, 1.0f).build("direwolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> PHOENIX =
        ENTITY_TYPES.register("phoenix", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(1.0f, 1.5f).fireImmune().build("phoenix"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> UNICORN =
        ENTITY_TYPES.register("unicorn", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(1.3f, 1.6f).build("unicorn"));

    // Undead
    public static final DeferredHolder<EntityType<?>, EntityType<?>> WIGHT =
        ENTITY_TYPES.register("wight", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(0.8f, 2.0f).build("wight"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> WRAITH =
        ENTITY_TYPES.register("wraith", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(0.6f, 2.0f).build("wraith"));

    // Bosses
    public static final DeferredHolder<EntityType<?>, EntityType<?>> FROST_DRAGON =
        ENTITY_TYPES.register("frost_dragon", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(2.5f, 2.0f).build("frost_dragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> DEMON_WARLORD =
        ENTITY_TYPES.register("demon_warlord", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(1.5f, 3.5f).fireImmune().build("demon_warlord"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> ABYSSAL_LEVIATHAN =
        ENTITY_TYPES.register("abyssal_leviathan", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.MONSTER)
                .sized(5.0f, 5.0f).build("abyssal_leviathan"));

    // NPCs
    public static final DeferredHolder<EntityType<?>, EntityType<?>> AETERNUM_BANKER =
        ENTITY_TYPES.register("aeternum_banker", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(0.6f, 1.95f).build("aeternum_banker"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> CLASS_TRAINER =
        ENTITY_TYPES.register("class_trainer", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(0.6f, 1.95f).build("class_trainer"));

    public static final DeferredHolder<EntityType<?>, EntityType<?>> DARK_MERCHANT =
        ENTITY_TYPES.register("dark_merchant", () ->
            EntityType.Builder.of((type, level) -> null, MobCategory.CREATURE)
                .sized(0.6f, 1.95f).build("dark_merchant"));
}
