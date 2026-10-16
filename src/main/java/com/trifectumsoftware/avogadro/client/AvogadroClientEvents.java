package com.trifectumsoftware.avogadro.client;

import com.trifectumsoftware.avogadro.Avogadro;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Avogadro.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AvogadroClientEvents {
    private AvogadroClientEvents() {
    }

    @SubscribeEvent
    public static void factories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MoleculeTooltip.class, ClientMoleculeTooltip::new);
    }

    @SubscribeEvent
    public static void reloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new MoleculeAssets());
    }
}
