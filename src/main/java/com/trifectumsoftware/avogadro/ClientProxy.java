package com.trifectumsoftware.avogadro;

import net.minecraftforge.common.MinecraftForge;

import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltips;
import com.trifectumsoftware.avogadro.compat.ntm.NtmCompat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    private static final String WAILA_PROVIDER = "com.trifectumsoftware.avogadro.compat.waila.WailaMoleculeProvider.load";

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new MoleculeTooltips());

        if (Config.wailaCompat) {
            FMLInterModComms.sendMessage("Waila", "register", WAILA_PROVIDER);
            FMLInterModComms.sendMessage("waila", "register", WAILA_PROVIDER);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        if (Config.ntmCompat && Loader.isModLoaded("hbm")) {
            NtmCompat.register();
        }
        MoleculeRenderer.precomputeAll();
    }
}
