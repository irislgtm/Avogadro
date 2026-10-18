package com.trifectumsoftware.avogadro.client.tooltip;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record ClientMoleculeTooltip(MoleculeTooltip data) implements ClientTooltipComponent {

    @Override
    public int getHeight() {
        return Config.TOOLTIP_SIZE.get() + 12;
    }

    @Override
    public int getWidth(Font font) {
        return Config.TOOLTIP_SIZE.get() + 24;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        MoleculeDef def = MoleculeStore.get(data.molecule());
        if (def == null) return;
        int size = Config.TOOLTIP_SIZE.get();
        gfx.enableScissor(x, y, x + size + 24, y + size + 12);
        double period = Config.SPIN_SECONDS.get() * 1000.0;
        float angle = (float) ((System.currentTimeMillis() % (long) period) / period * 360.0);
        MoleculeRenderer.render(def.structure(), gfx, x + 12, y + 6, size, angle);
        gfx.disableScissor();
    }
}
