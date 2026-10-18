package com.trifectumsoftware.avogadro.client.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Adds the molecule formula and enough blank filler lines to reserve room for the model, which is then drawn by
 * {@link MoleculeTooltipOverlay} from the {@code GuiScreen} mixin once the vanilla tooltip box has been laid out.
 */
public final class MoleculeTooltips {

    private static volatile String pending;

    public static String takePending() {
        String value = pending;
        pending = null;
        return value;
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!Config.enableTooltips) return;

        String hit = MoleculeStore.lookup(event.itemStack);
        if (hit == null) return;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null) return;

        List<String> lines = event.toolTip;
        if (lines == null || lines.isEmpty()) return;

        pending = hit;

        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {
            lines.add(formula);
        }

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int size = Config.tooltipSize;
        if (size <= 0 || font == null) return;

        int rows = (int) Math.ceil(size / 10.0);
        String spacer = spacer(font, size + 2);
        for (int i = 0; i < rows; i++) {
            lines.add(spacer);
        }
    }

    private static String spacer(FontRenderer font, int minWidth) {
        StringBuilder builder = new StringBuilder();
        while (font.getStringWidth(builder.toString()) < minWidth) {
            builder.append(' ');
        }
        return builder.toString();
    }
}
