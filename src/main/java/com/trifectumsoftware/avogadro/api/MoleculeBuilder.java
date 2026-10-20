package com.trifectumsoftware.avogadro.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeGenerators;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import com.trifectumsoftware.avogadro.molecule.MoleculeStructure;
import com.trifectumsoftware.avogadro.molecule.SmilesParser;

public final class MoleculeBuilder {

    private final String id;
    private String formula;
    private MoleculeStructure structure;
    private String copy;
    private final List<String> items = new ArrayList<String>();
    private final List<String> itemTags = new ArrayList<String>();
    private final List<String> fluids = new ArrayList<String>();
    private final List<String> fluidTags = new ArrayList<String>();
    private final List<String> materials = new ArrayList<String>();
    private boolean matchContainers = true;
    private boolean matchContainersSet;

    MoleculeBuilder(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("Molecule id is required");
        this.id = id;
    }

    public MoleculeBuilder structure(MoleculeStructure structure) {
        this.structure = structure;
        return this;
    }

    public MoleculeBuilder smiles(String smiles) {
        this.structure = SmilesParser.parse(id, smiles);
        return this;
    }

    public MoleculeBuilder fullerene(int carbons) {
        this.structure = MoleculeGenerators.fullerene(id, carbons);
        return this;
    }

    public MoleculeBuilder nanotube(int n, int m, int cells) {
        this.structure = MoleculeGenerators.nanotube(id, n, m, cells);
        return this;
    }

    public MoleculeBuilder crystal(String lattice, String... elements) {
        return crystal(lattice, 2, 2, elements);
    }

    public MoleculeBuilder crystal(String lattice, int cells, String... elements) {
        return crystal(lattice, cells, 2, elements);
    }

    public MoleculeBuilder crystal(String lattice, int cells, int layers, String... elements) {
        this.structure = MoleculeGenerators.crystal(id, lattice, Arrays.asList(elements), cells, layers);
        return this;
    }

    public MoleculeBuilder formula(String formula) {
        this.formula = formula;
        return this;
    }

    public MoleculeBuilder copy(String moleculeId) {
        this.copy = moleculeId;
        return this;
    }

    public MoleculeBuilder item(String registryName) {
        addUnique(items, registryName);
        return this;
    }

    public MoleculeBuilder items(String... registryNames) {
        for (String name : registryNames) item(name);
        return this;
    }

    public MoleculeBuilder itemTag(String oreDictionaryName) {
        addUnique(itemTags, stripHash(oreDictionaryName));
        return this;
    }

    public MoleculeBuilder itemTags(String... oreDictionaryNames) {
        for (String name : oreDictionaryNames) itemTag(name);
        return this;
    }

    public MoleculeBuilder fluid(String fluidName) {
        addUnique(fluids, fluidName);
        return this;
    }

    public MoleculeBuilder fluids(String... fluidNames) {
        for (String name : fluidNames) fluid(name);
        return this;
    }

    public MoleculeBuilder fluidTag(String tagName) {
        addUnique(fluidTags, stripHash(tagName));
        return this;
    }

    public MoleculeBuilder fluidTags(String... tagNames) {
        for (String name : tagNames) fluidTag(name);
        return this;
    }

    public MoleculeBuilder material(String materialName) {
        addUnique(materials, materialName);
        return this;
    }

    public MoleculeBuilder materials(String... materialNames) {
        for (String name : materialNames) material(name);
        return this;
    }

    public MoleculeBuilder matchContainers(boolean matchContainers) {
        this.matchContainers = matchContainers;
        this.matchContainersSet = true;
        return this;
    }

    public MoleculeDef build() {
        MoleculeStructure result = structure;
        String resultFormula = formula;
        List<String> resultItems = items;
        List<String> resultItemTags = itemTags;
        List<String> resultFluids = fluids;
        List<String> resultFluidTags = fluidTags;
        List<String> resultMaterials = materials;
        boolean resultContainers = matchContainers;

        if (copy != null) {
            MoleculeDef base = MoleculeStore.get(copy);
            if (base == null) throw new IllegalArgumentException("Unknown molecule to copy: " + copy);
            if (result == null) result = base.structure();
            if (resultFormula == null) resultFormula = base.formula();
            resultItems = union(base.items(), resultItems);
            resultItemTags = union(base.itemTags(), resultItemTags);
            resultFluids = union(base.fluids(), resultFluids);
            resultFluidTags = union(base.fluidTags(), resultFluidTags);
            resultMaterials = union(base.materials(), resultMaterials);
            if (!matchContainersSet) resultContainers = base.matchContainers();
        }

        if (result == null) throw new IllegalStateException("Molecule " + id + " has no geometry");
        if (resultFormula == null) resultFormula = MoleculeStore.hill(result);

        return new MoleculeDef(
            id,
            result,
            resultFormula,
            resultItems,
            resultItemTags,
            resultFluids,
            resultFluidTags,
            resultMaterials,
            resultContainers);
    }

    public MoleculeDef register() {
        return AvogadroAPI.register(build());
    }

    private static void addUnique(List<String> list, String value) {
        if (value == null || value.isEmpty() || list.contains(value)) return;
        list.add(value);
    }

    private static String stripHash(String value) {
        return value != null && value.startsWith("#") ? value.substring(1) : value;
    }

    private static List<String> union(List<String> base, List<String> own) {
        List<String> out = new ArrayList<String>(base);
        for (String value : own) addUnique(out, value);
        return out;
    }
}
