package com.trifectumsoftware.avogadro.api;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import com.trifectumsoftware.avogadro.molecule.MoleculeStructure;

public final class AvogadroAPI {

    private AvogadroAPI() {}

    public static MoleculeDef register(MoleculeDef definition) {
        return MoleculeStore.registerApi(definition);
    }

    public static MoleculeDef register(String id, MoleculeStructure structure) {
        return builder(id).structure(structure)
            .register();
    }

    public static MoleculeDef register(String id, MoleculeStructure structure, String formula) {
        return builder(id).structure(structure)
            .formula(formula)
            .register();
    }

    public static MoleculeDef unregister(String id) {
        return MoleculeStore.unregisterApi(id);
    }

    public static MoleculeDef get(String id) {
        return MoleculeStore.get(id);
    }

    public static Collection<MoleculeDef> molecules() {
        return MoleculeStore.all();
    }

    public static MoleculeDef forItem(ItemStack stack) {
        String hit = MoleculeStore.lookup(stack);
        return hit == null ? null : MoleculeStore.get(hit);
    }

    public static MoleculeDef forFluid(FluidStack stack) {
        String hit = MoleculeStore.lookup(stack);
        return hit == null ? null : MoleculeStore.get(hit);
    }

    public static MoleculeBuilder builder(String id) {
        return new MoleculeBuilder(id);
    }
}
