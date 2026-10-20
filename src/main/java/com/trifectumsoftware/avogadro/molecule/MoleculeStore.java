package com.trifectumsoftware.avogadro.molecule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.oredict.OreDictionary;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class MoleculeStore {

    private static final Object LOCK = new Object();
    private static final Map<String, MoleculeDef> CONFIG_DEFS = new LinkedHashMap<String, MoleculeDef>();
    private static final Map<String, MoleculeDef> API_DEFS = new LinkedHashMap<String, MoleculeDef>();
    private static final Map<String, MoleculeDef> DEFS = new LinkedHashMap<String, MoleculeDef>();
    private static final Map<String, String> ITEM = new HashMap<String, String>();
    private static final Map<String, String> ITEM_ORE = new HashMap<String, String>();
    private static final Map<String, String> FLUID = new HashMap<String, String>();
    private static final Map<String, String> BY_PATH = new HashMap<String, String>();

    private MoleculeStore() {}

    public static MoleculeDef get(String id) {
        synchronized (LOCK) {
            return DEFS.get(id);
        }
    }

    public static Collection<MoleculeDef> all() {
        synchronized (LOCK) {
            return Collections.unmodifiableList(new ArrayList<MoleculeDef>(DEFS.values()));
        }
    }

    public static void setAll(Map<String, MoleculeDef> defs) {
        synchronized (LOCK) {
            CONFIG_DEFS.clear();
            CONFIG_DEFS.putAll(defs);
            rebuildDefs();
        }
    }

    private static void rebuildDefs() {
        DEFS.clear();
        DEFS.putAll(CONFIG_DEFS);
        DEFS.putAll(API_DEFS);
        rebuildIndexes();
    }

    public static MoleculeDef registerApi(MoleculeDef def) {
        if (def == null || def.id() == null
            || def.id()
                .isEmpty()
            || def.structure() == null) {
            throw new IllegalArgumentException("Molecule definitions need an id and a structure");
        }
        synchronized (LOCK) {
            API_DEFS.put(def.id(), def);
            rebuildDefs();
            return def;
        }
    }

    public static MoleculeDef unregisterApi(String id) {
        synchronized (LOCK) {
            MoleculeDef removed = API_DEFS.remove(id);
            if (removed == null) return null;
            rebuildDefs();
            return removed;
        }
    }

    private static void rebuildIndexes() {
        ITEM.clear();
        ITEM_ORE.clear();
        FLUID.clear();
        BY_PATH.clear();
        for (Map.Entry<String, MoleculeDef> e : DEFS.entrySet()) {
            MoleculeDef d = e.getValue();
            for (String s : d.items()) {
                if (s != null && !s.isEmpty()) ITEM.put(s, e.getKey());
            }
            for (String s : d.itemTags()) {
                if (s != null && !s.isEmpty()) ITEM_ORE.put(s, e.getKey());
            }
            for (String s : d.fluids()) {
                if (s != null && !s.isEmpty()) FLUID.put(s, e.getKey());
            }
            BY_PATH.put(path(e.getKey()), e.getKey());
        }
    }

    public static String lookup(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        Object nameObj = Item.itemRegistry.getNameForObject(stack.getItem());
        if (nameObj == null) return null;
        String key = nameObj.toString();
        synchronized (LOCK) {
            String direct = ITEM.get(key);
            if (direct != null) return direct;

            int[] oreIds = OreDictionary.getOreIDs(stack);
            for (int id : oreIds) {
                String hit = ITEM_ORE.get(OreDictionary.getOreName(id));
                if (hit != null) return hit;
            }

            Item item = stack.getItem();
            if (item instanceof IFluidContainerItem) {
                FluidStack inside = ((IFluidContainerItem) item).getFluid(stack);
                if (inside != null) {
                    String hit = lookup(inside);
                    if (hit != null) {
                        MoleculeDef def = DEFS.get(hit);
                        if (def != null && def.matchContainers()) return hit;
                    }
                }
            }
            return BY_PATH.get(path(key));
        }
    }

    public static String lookup(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return null;
        Fluid fluid = stack.getFluid();
        String name = fluid.getName();
        if (name == null) return null;
        synchronized (LOCK) {
            String direct = FLUID.get(name);
            if (direct != null) return direct;
            String p = path(name);
            direct = FLUID.get(p);
            if (direct != null) return direct;
            return BY_PATH.get(p);
        }
    }

    private static String path(String id) {
        int i = id.indexOf(':');
        return i < 0 ? id : id.substring(i + 1);
    }

    public static Map<String, MoleculeDef> parseAll(Map<String, JsonElement> jsons) {
        Map<String, MoleculeDef> defs = new LinkedHashMap<String, MoleculeDef>();
        List<String> pending = new ArrayList<String>(jsons.keySet());
        for (int pass = 0; pass < 10 && !pending.isEmpty(); pass++) {
            Iterator<String> it = pending.iterator();
            while (it.hasNext()) {
                String id = it.next();
                MoleculeDef def = parse(id, jsons.get(id), defs);
                if (def != null) {
                    defs.put(id, def);
                    it.remove();
                }
            }
        }
        return defs;
    }

    public static MoleculeDef parse(String id, JsonElement root, Map<String, MoleculeDef> known) {
        try {
            if (root == null || !root.isJsonObject()) return null;
            JsonObject o = root.getAsJsonObject();
            String formula = null;
            if (o.has("formula") && o.get("formula")
                .isJsonPrimitive())
                formula = o.get("formula")
                    .getAsString();
            List<String> items = strings(o, "items");
            List<String> itemTags = tags(o, "item_tags");
            List<String> fluids = strings(o, "fluids");
            List<String> fluidTags = tags(o, "fluid_tags");
            List<String> materials = strings(o, "materials");
            boolean hasContainers = o.has("match_containers") && o.get("match_containers")
                .isJsonPrimitive();
            boolean matchContainers = !hasContainers || o.get("match_containers")
                .getAsBoolean();
            MoleculeStructure structure = null;
            if (o.has("copy")) {
                String baseId = o.get("copy")
                    .getAsString();
                MoleculeDef base = known.get(baseId);
                if (base == null) base = get(baseId);
                if (base == null) return null;
                structure = base.structure();
                if (formula == null) formula = base.formula();
                items = union(base.items(), items);
                itemTags = union(base.itemTags(), itemTags);
                fluids = union(base.fluids(), fluids);
                fluidTags = union(base.fluidTags(), fluidTags);
                materials = union(base.materials(), materials);
                if (!hasContainers) matchContainers = base.matchContainers();
            } else if (o.has("atoms")) {
                if (!o.get("atoms")
                    .isJsonArray()
                    || o.getAsJsonArray("atoms")
                        .size() == 0)
                    return null;
                JsonArray bonds = o.has("bonds") && o.get("bonds")
                    .isJsonArray() ? o.getAsJsonArray("bonds") : null;
                structure = atoms(id, o.getAsJsonArray("atoms"), bonds);
                if (structure == null) return null;
                if (formula == null) formula = hill(structure);
            } else if (o.has("smiles")) {
                structure = SmilesParser.parse(
                    id,
                    o.get("smiles")
                        .getAsString());
                if (formula == null) formula = hill(structure);
            } else if (o.has("fullerene")) {
                JsonElement fullerene = o.get("fullerene");
                int carbons = fullerene.isJsonPrimitive() ? fullerene.getAsInt()
                    : fullerene.getAsJsonObject()
                        .get("carbons")
                        .getAsInt();
                structure = MoleculeGenerators.fullerene(id, carbons);
                if (formula == null) formula = hill(structure);
            } else if (o.has("nanotube")) {
                JsonObject nanotube = o.getAsJsonObject("nanotube");
                structure = MoleculeGenerators.nanotube(
                    id,
                    nanotube.get("n")
                        .getAsInt(),
                    nanotube.get("m")
                        .getAsInt(),
                    nanotube.has("cells") ? nanotube.get("cells")
                        .getAsInt() : 3);
                if (formula == null) formula = hill(structure);
            } else if (o.has("crystal")) {
                JsonObject crystal = o.getAsJsonObject("crystal");
                List<String> elements = strings(crystal, "elements");
                if (elements.isEmpty() && crystal.has("element")
                    && crystal.get("element")
                        .isJsonPrimitive()) {
                    elements.add(
                        crystal.get("element")
                            .getAsString());
                }
                structure = MoleculeGenerators.crystal(
                    id,
                    crystal.get("lattice")
                        .getAsString(),
                    elements,
                    crystal.has("cells") ? crystal.get("cells")
                        .getAsInt() : 2,
                    crystal.has("layers") ? crystal.get("layers")
                        .getAsInt() : 2);
                if (formula == null) formula = hill(structure);
            } else {
                return null;
            }
            return new MoleculeDef(
                id,
                structure,
                formula,
                copy(items),
                copy(itemTags),
                copy(fluids),
                copy(fluidTags),
                copy(materials),
                matchContainers);
        } catch (Exception bad) {
            return null;
        }
    }

    private static List<String> copy(List<String> in) {
        return Collections.unmodifiableList(new ArrayList<String>(in));
    }

    private static MoleculeStructure atoms(String id, JsonArray arr, JsonArray bonds) {
        try {
            MoleculeStructure.Builder b = MoleculeStructure.builder(id, "");
            for (JsonElement e : arr) {
                JsonObject a = e.getAsJsonObject();
                b.addAtom(
                    AtomElement.bySymbol(
                        a.get("e")
                            .getAsString()),
                    a.get("x")
                        .getAsFloat(),
                    a.get("y")
                        .getAsFloat(),
                    a.get("z")
                        .getAsFloat());
            }
            if (bonds != null) {
                for (JsonElement e : bonds) {
                    JsonArray t = e.getAsJsonArray();
                    b.bond(
                        t.get(0)
                            .getAsInt(),
                        t.get(1)
                            .getAsInt(),
                        t.size() > 2 ? bondType(t.get(2)) : BondType.SINGLE);
                }
            } else {
                b.autoBond();
            }
            return b.build();
        } catch (Exception bad) {
            return null;
        }
    }

    private static BondType bondType(JsonElement e) {
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive()
            .isNumber()) {
            int v = e.getAsInt();
            if (v == 2) return BondType.DOUBLE;
            if (v == 3) return BondType.TRIPLE;
            if (v >= 4) return BondType.QUADRUPLE;
            return BondType.SINGLE;
        }
        String s = e.getAsString()
            .trim()
            .toUpperCase(java.util.Locale.ROOT);
        if (s.equals("DOUBLE") || s.equals("=") || s.equals("2")) return BondType.DOUBLE;
        if (s.equals("TRIPLE") || s.equals("#") || s.equals("3")) return BondType.TRIPLE;
        if (s.equals("QUADRUPLE") || s.equals("4")) return BondType.QUADRUPLE;
        return BondType.SINGLE;
    }

    public static String hill(MoleculeStructure mol) {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (MoleculeStructure.Atom a : mol.atoms()) {
            Integer cur = counts.get(
                a.element()
                    .symbol());
            counts.put(
                a.element()
                    .symbol(),
                Integer.valueOf(cur == null ? 1 : cur.intValue() + 1));
        }
        StringBuilder sb = new StringBuilder();
        take(counts, sb, "C");
        take(counts, sb, "H");
        List<String> rest = new ArrayList<String>(counts.keySet());
        Collections.sort(rest);
        for (String k : rest) take(counts, sb, k);
        return sb.toString();
    }

    private static void take(Map<String, Integer> counts, StringBuilder sb, String key) {
        Integer n = counts.remove(key);
        if (n == null) return;
        sb.append(key);
        if (n.intValue() > 1) {
            for (char c : String.valueOf(n.intValue())
                .toCharArray()) {
                sb.append((char) (0x2080 + (c - '0')));
            }
        }
    }

    private static List<String> strings(JsonObject o, String key) {
        List<String> out = new ArrayList<String>();
        if (!o.has(key) || !o.get(key)
            .isJsonArray()) return out;
        for (JsonElement e : o.getAsJsonArray(key)) {
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive()
                .isString()) out.add(e.getAsString());
        }
        return out;
    }

    private static List<String> tags(JsonObject o, String key) {
        List<String> out = strings(o, key);
        List<String> norm = new ArrayList<String>(out.size());
        for (String s : out) norm.add(s.startsWith("#") ? s.substring(1) : s);
        return norm;
    }

    private static List<String> union(List<String> a, List<String> b) {
        List<String> out = new ArrayList<String>(a);
        for (String s : b) {
            if (!out.contains(s)) out.add(s);
        }
        return out;
    }
}
