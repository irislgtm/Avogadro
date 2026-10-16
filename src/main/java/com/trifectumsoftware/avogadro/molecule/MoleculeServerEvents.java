package com.trifectumsoftware.avogadro.molecule;

import com.trifectumsoftware.avogadro.Avogadro;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Avogadro.MODID)
public final class MoleculeServerEvents {
    private MoleculeServerEvents() {
    }

    @SubscribeEvent
    public static void reloads(AddReloadListenerEvent event) {
        event.addListener(new MoleculeLoader());
    }

    @SubscribeEvent
    public static void sync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            MoleculeSync.sendAll();
        } else {
            MoleculeSync.sendTo(player);
        }
    }
}
