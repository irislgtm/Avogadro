package com.trifectumsoftware.avogadro.client.tooltip;

import com.mojang.datafixers.util.Either;
import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = Avogadro.MODID, value = Dist.CLIENT)
public final class MoleculeTooltipEvents {
    private MoleculeTooltipEvents() {
    }

    @SubscribeEvent
    public static void gather(RenderTooltipEvent.GatherComponents event) {
        if (!Config.ENABLE_TOOLTIPS.get()) return;
        ResourceLocation hit = MoleculeStore.lookup(event.getItemStack());
        if (hit != null && MoleculeStore.get(hit) != null) {
            event.getTooltipElements().add(Either.right(new MoleculeTooltip(hit)));
        }
    }
}
