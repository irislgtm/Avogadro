package com.trifectumsoftware.avogadro.molecule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.items = copy(items);
        this.itemTags = copy(itemTags);
        this.fluids = copy(fluids);
        this.fluidTags = copy(fluidTags);
        this.materials = copy(materials);
        this.matchContainers = matchContainers;
    }

    public MoleculeDef(String id, MoleculeStructure structure, String formula) {
        this(
            id,
            structure,
            formula,
            Collections.<String>emptyList(),
            Collections.<String>emptyList(),
            Collections.<String>emptyList(),
            Collections.<String>emptyList(),
            Collections.<String>emptyList(),
            true);
    }

    private static List<String> copy(List<String> in) {
        if (in == null || in.isEmpty()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<String>(in));
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

    @Override
    public String toString() {
        return "MoleculeDef{" + id + '}';
    }
}
