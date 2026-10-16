package com.trifectumsoftware.avogadro.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MoleculeClientStore {
    private static Map<ResourceLocation, MoleculeDef> server = new LinkedHashMap<>();
    private static Map<ResourceLocation, MoleculeDef> assets = new LinkedHashMap<>();
    private static Map<ResourceLocation, MoleculeDef> overrides = new LinkedHashMap<>();

    private MoleculeClientStore() {
    }

    public static void applyServerJson(Map<ResourceLocation, String> raw) {
        Map<ResourceLocation, JsonElement> jsons = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, String> e : raw.entrySet()) {
            try {
                jsons.put(e.getKey(), JsonParser.parseString(e.getValue()));
            } catch (Exception skipped) {
            }
        }
        server = MoleculeStore.parseAll(jsons);
        rebuild();
    }

    public static void applyLocal(Map<ResourceLocation, MoleculeDef> a, Map<ResourceLocation, MoleculeDef> c) {
        assets = new LinkedHashMap<>(a);
        overrides = new LinkedHashMap<>(c);
        rebuild();
    }

    private static void rebuild() {
        Map<ResourceLocation, MoleculeDef> merged = new LinkedHashMap<>();
        merged.putAll(server);
        merged.putAll(assets);
        merged.putAll(overrides);
        MoleculeStore.setAll(merged);
    }
}
