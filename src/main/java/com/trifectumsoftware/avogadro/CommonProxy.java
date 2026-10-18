package com.trifectumsoftware.avogadro;

import com.trifectumsoftware.avogadro.molecule.MoleculeConfigLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MoleculeConfigLoader.load(event.getModConfigurationDirectory());
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}
}
