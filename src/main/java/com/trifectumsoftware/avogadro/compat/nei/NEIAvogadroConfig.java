package com.trifectumsoftware.avogadro.compat.nei;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.Tags;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

public class NEIAvogadroConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        GuiContainerManager.addTooltipHandler(new NeiMoleculeTooltipHandler());
        if (!Config.neiCategory) return;
        MoleculeRecipeHandler handler = new MoleculeRecipeHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }

    @Override
    public String getName() {
        return "Avogadro";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
