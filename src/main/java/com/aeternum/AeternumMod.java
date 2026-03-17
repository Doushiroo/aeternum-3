package com.aeternum;

import com.aeternum.client.ClientEvents;
import com.aeternum.commands.AeternumCommands;
import com.aeternum.events.AeternumEvents;
import com.aeternum.registry.ModAttachments;
import com.aeternum.registry.ModEntities;
import com.aeternum.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AeternumMod.MODID)
public class AeternumMod {

    public static final String MODID = "aeternum";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public AeternumMod(IEventBus modEventBus) {
        LOGGER.info("=== AETERNUM - THE DEFINITIVE WORLD IS LOADING ===");

        // Register deferred registries on the mod event bus
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        // Register game-side event handlers on the NeoForge bus
        NeoForge.EVENT_BUS.register(new AeternumEvents());
        NeoForge.EVENT_BUS.register(new AeternumCommands());

        // Register client-only event handlers
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new ClientEvents());
        }

        LOGGER.info("=== AETERNUM LOADED SUCCESSFULLY ===");
    }
}
