package com.trifectumsoftware.avogadro.molecule;

import com.trifectumsoftware.avogadro.Avogadro;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public record MoleculeSync(Map<ResourceLocation, String> entries) implements CustomPacketPayload {
    public static final Type<MoleculeSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Avogadro.MODID, "molecules"));
    public static final StreamCodec<FriendlyByteBuf, MoleculeSync> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
            MoleculeSync::entries,
            MoleculeSync::new);

    private static final Map<ResourceLocation, String> RAW = new LinkedHashMap<>();

    @Override
    public Type<MoleculeSync> type() {
        return TYPE;
    }

    public static void storeRaw(Map<ResourceLocation, String> raw) {
        RAW.clear();
        RAW.putAll(raw);
    }

    public static void sendAll() {
        PacketDistributor.sendToAllPlayers(new MoleculeSync(Map.copyOf(RAW)));
    }

    public static void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new MoleculeSync(Map.copyOf(RAW)));
    }

    public static void handle(MoleculeSync message, IPayloadContext context) {
        context.enqueueWork(() -> com.trifectumsoftware.avogadro.client.MoleculeClientStore.applyServerJson(message.entries()));
    }
}
