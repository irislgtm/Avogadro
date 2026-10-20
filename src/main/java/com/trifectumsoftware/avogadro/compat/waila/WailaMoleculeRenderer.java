package com.trifectumsoftware.avogadro.compat.waila;

import java.awt.Dimension;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;

public final class WailaMoleculeRenderer implements IWailaTooltipRenderer {

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        int size = size();
        return new Dimension(size, size);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        if (params == null || params.length == 0) return;
        MoleculeDef def = MoleculeStore.get(params[0]);
        if (def == null) return;

        int size = size();
        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);
        MoleculeRenderer.render(def.structure(), 0, 0, size, angle);
    }

    private static int size() {
        int size = Config.wailaSize;
        if (size < 16) size = 16;
        if (size > 64) size = 64;
        return size;
    }
}
