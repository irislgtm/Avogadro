package com.trifectumsoftware.avogadro.molecule;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class MoleculeLoader extends SimpleJsonResourceReloadListener {
    public MoleculeLoader() {
        super(new Gson(), "molecules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, MoleculeDef> defs = MoleculeStore.parseAll(map);
        MoleculeStore.setAll(defs);
        Map<ResourceLocation, String> raw = new LinkedHashMap<>();
        for (ResourceLocation id : defs.keySet()) {
            JsonElement el = map.get(id);
            if (el != null) raw.put(id, el.toString());
        }
        MoleculeSync.storeRaw(raw);
    }
}
