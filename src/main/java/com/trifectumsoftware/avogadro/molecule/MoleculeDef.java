package com.trifectumsoftware.avogadro.molecule;

import java.util.List;

/**
 * A single molecule definition. On 1.7.10 molecule ids are plain strings ("avogadro:benzene") since
 * {@code ResourceLocation} there has a very different API.
 */
public final class MoleculeDef {

    private final String id;
    private final MoleculeStructure structure;
    private final String formula;
    private final List<String> items;
    private final List<String> itemTags;
    private final List<String> fluids;
    private final List<String> fluidTags;
    private final List<String> materials;
    private final boolean matchContainers;

    public MoleculeDef(String id, MoleculeStructure structure, String formula, List<String> items,
        List<String> itemTags, List<String> fluids, List<String> fluidTags, List<String> materials,
        boolean matchContainers) {
        this.id = id;
        this.structure = structure;
        this.formula = formula;
        this.items = items;
        this.itemTags = itemTags;
        this.fluids = fluids;
        this.fluidTags = fluidTags;
        this.materials = materials;
        this.matchContainers = matchContainers;
    }

    public String id() {
        return id;
    }

    public MoleculeStructure structure() {
        return structure;
    }

    public String formula() {
        return formula;
    }

    public List<String> items() {
        return items;
    }

    public List<String> itemTags() {
        return itemTags;
    }

    public List<String> fluids() {
        return fluids;
    }

    public List<String> fluidTags() {
        return fluidTags;
    }

    public List<String> materials() {
        return materials;
    }

    public boolean matchContainers() {
        return matchContainers;
    }
}
