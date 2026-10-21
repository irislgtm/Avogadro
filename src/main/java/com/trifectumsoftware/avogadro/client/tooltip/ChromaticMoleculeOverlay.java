package com.trifectumsoftware.avogadro.client.tooltip;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fluids.FluidStack;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class ChromaticMoleculeOverlay {

    private static Method getLastTooltipContext;
    private static Method getItem;
    private static Method getFluid;
    private static boolean unavailable;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!Config.enableTooltips || unavailable) return;
        if (event.gui == null) return;
        MoleculeDef def = molecule();
        if (def == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRenderer == null) return;
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int size = Math.max(16, Math.min(Config.tooltipSize, 64));
        int padding = 4;
        int box = size + padding * 2;
        int x = Math.max(2, (resolution.getScaledWidth() - box) / 2);
        int y = Math.max(2, resolution.getScaledHeight() - box - 30);

        Gui.drawRect(x, y, x + box, y + box, 0xC0101010);
        Gui.drawRect(x, y, x + box, y + 1, 0xFF808080);
        Gui.drawRect(x, y + box - 1, x + box, y + box, 0xFF404040);
        Gui.drawRect(x, y, x + 1, y + box, 0xFF808080);
        Gui.drawRect(x + box - 1, y, x + box, y + box, 0xFF404040);

        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);
        MoleculeRenderer.render(def.structure(), x + padding, y + padding, size, angle);
    }

    private static MoleculeDef molecule() {
        if (unavailable) return null;
        if (getLastTooltipContext == null) {
            try {
                Class<?> handler = Class.forName("com.slprime.chromatictooltips.TooltipHandler");
                Class<?> context = Class.forName("com.slprime.chromatictooltips.api.TooltipContext");
                getLastTooltipContext = handler.getMethod("getLastTooltipContext");
                getItem = context.getMethod("getItem");
                getFluid = context.getMethod("getFluid");
            } catch (Throwable ignored) {
                unavailable = true;
                return null;
            }
        }
        try {
            Object context = getLastTooltipContext.invoke(null);
            if (context == null) return null;
            Object item = getItem.invoke(context);
            if (item instanceof ItemStack) {
                String id = MoleculeStore.lookup((ItemStack) item);
                if (id != null) return MoleculeStore.get(id);
            }
            Object fluid = getFluid.invoke(context);
            if (fluid instanceof FluidStack) {
                String id = MoleculeStore.lookup((FluidStack) fluid);
                if (id != null) return MoleculeStore.get(id);
            }
        } catch (Throwable ignored) {
            return null;
        }
        return null;
    }
}
