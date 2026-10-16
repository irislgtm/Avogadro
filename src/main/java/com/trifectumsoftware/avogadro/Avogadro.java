package com.trifectumsoftware.avogadro;

import com.trifectumsoftware.avogadro.molecule.MoleculeNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Avogadro.MODID)
public class Avogadro {
    public static final String MODID = "avogadro";

    public Avogadro(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MoleculeNetwork.register();
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
