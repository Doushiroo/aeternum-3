package com.aeternum.registry;

import com.aeternum.AeternumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, AeternumMod.MODID);

    // Currency items
    public static final DeferredHolder<Item, Item> AURUM_COIN =
        ITEMS.register("aurum_coin", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> AURUM_INGOT =
        ITEMS.register("aurum_ingot", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> AURUM_BAR =
        ITEMS.register("aurum_bar", () -> new Item(new Item.Properties().stacksTo(16)));

    // Class change items
    public static final DeferredHolder<Item, Item> CLASS_CHANGE_SCROLL =
        ITEMS.register("class_change_scroll", () -> new Item(new Item.Properties().stacksTo(1)));

    // Olympiad items
    public static final DeferredHolder<Item, Item> OLYMPIAD_TOKEN =
        ITEMS.register("olympiad_token", () -> new Item(new Item.Properties().stacksTo(64)));

    // Nobility items
    public static final DeferredHolder<Item, Item> OLIVE_BRANCH =
        ITEMS.register("olive_branch", () -> new Item(new Item.Properties().stacksTo(1)));

    // Boss drops
    public static final DeferredHolder<Item, Item> BOSS_SOUL_FRAGMENT =
        ITEMS.register("boss_soul_fragment", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> DRAGON_SCALE =
        ITEMS.register("dragon_scale", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> ANGEL_FEATHER =
        ITEMS.register("angel_feather", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> DEMON_HORN =
        ITEMS.register("demon_horn", () -> new Item(new Item.Properties().stacksTo(16)));

    // Taming items
    public static final DeferredHolder<Item, Item> BEAST_TAMING_CHARM =
        ITEMS.register("beast_taming_charm", () -> new Item(new Item.Properties().stacksTo(16)));

    // Teleport items
    public static final DeferredHolder<Item, Item> NOBLE_TELEPORT_SCROLL =
        ITEMS.register("noble_teleport_scroll", () -> new Item(new Item.Properties().stacksTo(5)));

    // Temperature items
    public static final DeferredHolder<Item, Item> WARM_CLOAK =
        ITEMS.register("warm_cloak", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> COOLING_POTION =
        ITEMS.register("cooling_potion", () -> new Item(new Item.Properties().stacksTo(16)));
}
