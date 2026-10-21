package com.trifectumsoftware.avogadro.client.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class MoleculeTooltips {

    static final class Pending {

        final String molecule;
        final int fillerStart;
        final int fillerRows;

        Pending(String molecule, int fillerStart, int fillerRows) {
            this.molecule = molecule;
            this.fillerStart = fillerStart;
            this.fillerRows = fillerRows;
        }
    }

    private static final boolean CHROMATIC = Loader.isModLoaded("chromatictooltips");
    private static volatile Pending pending;

    static Pending takePending() {
        Pending value = pending;
        pending = null;
        return value;
    }

    public static void clearPending() {
        pending = null;
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {

        pending = null;
        if (!Config.enableTooltips) return;

        String hit = MoleculeStore.lookup(event.itemStack);
        if (hit == null) return;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null) return;

        List<String> lines = event.toolTip;
        if (lines == null || lines.isEmpty()) return;

        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {
            lines.add(formula);
        }

        if (CHROMATIC) {
            pending = null;
            return;
        }

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int size = Config.tooltipSize;
        if (size <= 0 || font == null) return;

        int rows = (int) Math.ceil(size / 10.0);
        String spacer = spacer(font, size + 2);
        int start = lines.size();
        for (int i = 0; i < rows; i++) {
            lines.add(spacer);
        }
        pending = new Pending(hit, start, rows);
    }

    private static String spacer(FontRenderer font, int minWidth) {
        StringBuilder builder = new StringBuilder();
        while (font.getStringWidth(builder.toString()) < minWidth) {
            builder.append(' ');
        }
        return builder.toString();
    }
}
