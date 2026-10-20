package com.trifectumsoftware.avogadro.client.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

public final class MoleculeTooltipOverlay {

    private MoleculeTooltipOverlay() {}

    public static void draw(List<?> lines, int mouseX, int mouseY) {
        MoleculeTooltips.Pending pending = MoleculeTooltips.takePending();
        if (pending == null || !Config.enableTooltips) return;

        MoleculeDef def = MoleculeStore.get(pending.molecule);
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

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();

        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + width + 3 > screenWidth) x -= 28 + width;
        if (y + height + 6 > screenHeight) y = screenHeight - height - 6;

        int boxLeft = x - 3;
        int boxTop = y - 3;
        int boxWidth = width + 6;
        int boxHeight = height + 6;

        int size;
        int modelX;
        int modelY;
        int start = pending.fillerStart;
        int rows = pending.fillerRows;
        if (start >= 0 && rows > 0 && start + rows <= count) {

            int blockTop = y + (start == 0 ? 0 : 2 + start * 10);
            int blockHeight = rows * 10;
            size = Math.min(Config.tooltipSize, Math.min(boxWidth - 4, blockHeight - 2));
            if (size <= 0) return;
            modelX = boxLeft + (boxWidth - size) / 2;
            modelY = blockTop + (blockHeight - size) / 2;
        } else {
            size = Math.min(Config.tooltipSize, Math.min(boxWidth - 4, boxHeight - 4));
            if (size <= 0) return;
            modelX = boxLeft + (boxWidth - size) / 2;
            modelY = boxTop + boxHeight - size - 2;
        }

        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);

        MoleculeRenderer.render(def.structure(), modelX, modelY, size, angle);
    }
}
