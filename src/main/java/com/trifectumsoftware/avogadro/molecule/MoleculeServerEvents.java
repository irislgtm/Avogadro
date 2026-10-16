package com.trifectumsoftware.avogadro.molecule;

import com.trifectumsoftware.avogadro.Avogadro;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Avogadro.MODID)
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
            MoleculeNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), MoleculeSync.snapshot());
        } else {
            MoleculeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), MoleculeSync.snapshot());
        }
    }
}
