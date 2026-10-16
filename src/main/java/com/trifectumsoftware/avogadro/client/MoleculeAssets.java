package com.trifectumsoftware.avogadro.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MoleculeAssets implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> scan(manager), backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(found -> MoleculeClientStore.applyLocal(found.assets(), found.configs()), gameExecutor);
    }

    private record Found(Map<ResourceLocation, MoleculeDef> assets, Map<ResourceLocation, MoleculeDef> configs) {
    }

    private static Found scan(ResourceManager manager) {
        Map<ResourceLocation, JsonElement> jsons = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Resource> e : manager.listResources("molecules", p -> p.getPath().endsWith(".json")).entrySet()) {
            try (BufferedReader r = e.getValue().openAsReader()) {
                ResourceLocation key = e.getKey();
                String path = key.getPath();
                String sub = path.startsWith("molecules/") ? path.substring(10) : path;
                sub = sub.substring(0, sub.length() - 5);
                jsons.put(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), sub), JsonParser.parseReader(r));
            } catch (Exception skipped) {
            }
        }
        return new Found(MoleculeStore.parseAll(jsons), scanConfig());
    }

    private static Map<ResourceLocation, MoleculeDef> scanConfig() {
        Map<ResourceLocation, MoleculeDef> out = new LinkedHashMap<>();
        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve("avogadro").resolve("molecules");
            if (!Files.isDirectory(dir)) return out;
            List<Path> files;
            try (var stream = Files.list(dir)) {
                files = stream.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            }
            Map<ResourceLocation, JsonElement> jsons = new LinkedHashMap<>();
            for (Path p : files) {
                try (BufferedReader r = Files.newBufferedReader(p)) {
                    String name = p.getFileName().toString();
                    jsons.put(ResourceLocation.fromNamespaceAndPath(Avogadro.MODID, name.substring(0, name.length() - 5)), JsonParser.parseReader(r));
                } catch (Exception skipped) {
                }
            }
            out.putAll(MoleculeStore.parseAll(jsons));
        } catch (Exception skipped) {
        }
        return out;
    }
}
