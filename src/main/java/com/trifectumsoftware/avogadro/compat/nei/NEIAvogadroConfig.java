package com.trifectumsoftware.avogadro.compat.nei;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.Tags;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

/**
 * NEI entry point. NEI discovers implementations of {@link IConfigureNEI} whose class name starts with "NEI" and ends
 * with "Config", so this class is deliberately named to match.
 */
public class NEIAvogadroConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
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
