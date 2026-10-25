package com.trifectumsoftware.avogadro.compat.nei;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltips;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import com.trifectumsoftware.avogadro.molecule.MoleculeStructure;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.IContainerTooltipHandler;

public final class NeiMoleculeTooltipHandler implements IContainerTooltipHandler {

    @Override
    public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemStack, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public Map<String, String> handleHotkeys(GuiContainer gui, int mousex, int mousey, Map<String, String> hotkeys) {
        return hotkeys;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemStack, int mousex, int mousey,
        List<String> currenttip) {

        MoleculeTooltips.clearPending();
        if (!Config.enableTooltips || itemStack == null || currenttip == null) return currenttip;

        String hit = MoleculeStore.lookup(itemStack);
        if (hit == null) return currenttip;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null) return currenttip;

        currenttip.removeIf(NeiMoleculeTooltipHandler::isFiller);

        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {

            currenttip.removeIf(line -> isFormula(line, formula));
            currenttip.add(formula);
        }
        int textWidth = 0;
        for (String line : currenttip) {
            textWidth = Math.max(textWidth, GuiDraw.fontRenderer.getStringWidth(line));
        }
        currenttip.add(GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(new MoleculeLine(def.structure(), textWidth)));
        return currenttip;
    }

    private static boolean isFiller(String line) {
        if (line == null) return false;
        String stripped = line.replaceAll("\u00A7.", "");
        return !stripped.isEmpty() && stripped.trim()
            .isEmpty();
    }

    private static boolean isFormula(String line, String formula) {
        if (line == null) return false;
        return line.replaceAll("\u00A7.", "")
            .trim()
            .equals(formula);
    }

    private static final class MoleculeLine implements GuiDraw.ITooltipLineHandler {

        private final MoleculeStructure structure;
        private final int size;
        private final int width;

        MoleculeLine(MoleculeStructure structure, int textWidth) {
            this.structure = structure;
            int configured = Config.tooltipSize;
            this.size = Math.max(16, Math.min(128, configured));
            this.width = Math.max(size, textWidth);
        }

        @Override
        public Dimension getSize() {
            return new Dimension(width, size);
        }

        @Override
        public void draw(int x, int y) {
            long period = (long) (Config.spinSeconds * 1000.0f);
            if (period < 1L) period = 1L;
            float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);
            int modelX = x + (width - size) / 2;
            MoleculeRenderer.render(structure, modelX, y, size, angle);
        }
    }
}
