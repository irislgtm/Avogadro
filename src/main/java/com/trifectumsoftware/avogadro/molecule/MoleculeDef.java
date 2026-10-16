package com.trifectumsoftware.avogadro.molecule;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MoleculeDef(
        ResourceLocation id,
        MoleculeStructure structure,
        String formula,
        List<String> items,
        List<String> itemTags,
        List<String> fluids,
        List<String> fluidTags,
        List<String> materials,
        boolean matchContainers) {
}
