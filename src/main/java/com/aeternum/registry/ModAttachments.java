package com.aeternum.registry;

import com.aeternum.AeternumMod;
import com.aeternum.data.PlayerData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AeternumMod.MODID);

    /**
     * PlayerData uses INBTSerializable<CompoundTag> so we can use
     * AttachmentType.serializable() which handles save/load automatically.
     * copyOnDeath() ensures data is kept when the player respawns.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerData>> PLAYER_DATA =
        ATTACHMENT_TYPES.register("player_data", () ->
            AttachmentType.serializable(PlayerData::new)
                .copyOnDeath()
                .build()
        );
}
