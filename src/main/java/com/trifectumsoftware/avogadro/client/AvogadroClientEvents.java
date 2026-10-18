package com.trifectumsoftware.avogadro.client;

import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.client.tooltip.ClientMoleculeTooltip;
import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Avogadro.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AvogadroClientEvents {
    private AvogadroClientEvents() {
    }

    @SubscribeEvent
    public static void factories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MoleculeTooltip.class, ClientMoleculeTooltip::new);
    }

    @SubscribeEvent
    public static void reloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ClientMoleculeLoader());
    }
}
