package com.trifectumsoftware.avogadro.molecule;

import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.client.ClientMoleculeStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class MoleculeNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Avogadro.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId = 0;

    private MoleculeNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(MoleculeSync.class, nextId++)
                .encoder(MoleculeSync::encode)
                .decoder(MoleculeSync::new)
                .consumerMainThread(MoleculeNetwork::handle)
                .add();
    }

    private static void handle(MoleculeSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientMoleculeStore.applyServerJson(message.entries()));
        ctx.get().setPacketHandled(true);
    }
}
