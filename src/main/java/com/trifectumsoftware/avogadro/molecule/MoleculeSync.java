package com.trifectumsoftware.avogadro.molecule;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MoleculeSync {
    private final Map<ResourceLocation, String> entries;

    public MoleculeSync(Map<ResourceLocation, String> entries) {
        this.entries = Map.copyOf(entries);
    }

    public MoleculeSync(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, String> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) map.put(buf.readResourceLocation(), buf.readUtf());
        this.entries = Map.copyOf(map);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Map.Entry<ResourceLocation, String> e : entries.entrySet()) {
            buf.writeResourceLocation(e.getKey());
            buf.writeUtf(e.getValue());
        }
    }

    public Map<ResourceLocation, String> entries() {
        return entries;
    }

    private static final Map<ResourceLocation, String> RAW = new LinkedHashMap<>();

    public static void storeRaw(Map<ResourceLocation, String> raw) {
        RAW.clear();
        RAW.putAll(raw);
    }

    public static MoleculeSync snapshot() {
        return new MoleculeSync(Map.copyOf(RAW));
    }
}
