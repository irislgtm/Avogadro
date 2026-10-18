package com.trifectumsoftware.avogadro.client.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

/**
 * Draws the pending molecule inside the vanilla tooltip box. The box geometry mirrors what
 * {@code GuiScreen.drawHoveringText} computes from the same line list, and the model is placed in the blank filler
 * lines that {@link MoleculeTooltips} reserved.
 */
public final class MoleculeTooltipOverlay {

    private MoleculeTooltipOverlay() {}

    public static void draw(List<?> lines, int mouseX, int mouseY) {
        String hit = MoleculeTooltips.takePending();
        if (hit == null || !Config.enableTooltips) return;

        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null || lines == null || lines.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;
        if (font == null) return;

        int width = 0;
        for (Object line : lines) {
            int lineWidth = font.getStringWidth(String.valueOf(line));
            if (lineWidth > width) width = lineWidth;
        }
        int count = lines.size();
        int height = 8 + (count > 1 ? 2 + (count - 1) * 10 : 0);

        int left = mouseX + 12 - 4;
        int top = mouseY - 12 - 5;
        int boxWidth = width + 8;
        int boxHeight = height + 4;

        int size = Math.min(Config.tooltipSize, Math.min(boxWidth - 4, boxHeight - 4));
        if (size <= 0) return;

        int modelX = left + (boxWidth - size) / 2;
        int modelY = top + boxHeight - size - 2;

        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);

        MoleculeRenderer.render(def.structure(), modelX, modelY, size, angle);
    }
}
