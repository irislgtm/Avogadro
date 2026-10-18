package com.trifectumsoftware.avogadro;

import net.minecraftforge.common.MinecraftForge;

import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltips;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new MoleculeTooltips());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
