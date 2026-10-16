package com.trifectumsoftware.avogadro.molecule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trifectumsoftware.avogadro.compat.gt.GTCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MoleculeStore {
    private static final Map<ResourceLocation, MoleculeDef> DEFS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> ITEM = new HashMap<>();
    private static final List<ItemTagRow> ITEM_TAGS = new ArrayList<>();
    private static final Map<ResourceLocation, ResourceLocation> FLUID = new HashMap<>();
    private static final List<FluidTagRow> FLUID_TAGS = new ArrayList<>();
    private static final Map<ResourceLocation, ResourceLocation> MATERIAL = new HashMap<>();
    private static final Map<String, ResourceLocation> BY_PATH = new HashMap<>();

    private MoleculeStore() {
    }

    private record ItemTagRow(TagKey<Item> tag, ResourceLocation mol) {
    }

    private record FluidTagRow(TagKey<Fluid> tag, ResourceLocation mol) {
    }

    public static MoleculeDef get(ResourceLocation id) {
        return DEFS.get(id);
    }

    public static Collection<MoleculeDef> all() {
        return List.copyOf(DEFS.values());
    }

    public static void setAll(Map<ResourceLocation, MoleculeDef> defs) {
        DEFS.clear();
        ITEM.clear();
        ITEM_TAGS.clear();
        FLUID.clear();
        FLUID_TAGS.clear();
        MATERIAL.clear();
        BY_PATH.clear();
        DEFS.putAll(defs);
        for (Map.Entry<ResourceLocation, MoleculeDef> e : DEFS.entrySet()) {
            MoleculeDef d = e.getValue();
            for (String s : d.items()) {
                ResourceLocation k = ResourceLocation.tryParse(s);
                if (k != null) ITEM.put(k, e.getKey());
            }
            for (String s : d.itemTags()) {
                ResourceLocation k = ResourceLocation.tryParse(s);
                if (k != null) ITEM_TAGS.add(new ItemTagRow(ItemTags.create(k), e.getKey()));
            }
            for (String s : d.fluids()) {
                ResourceLocation k = ResourceLocation.tryParse(s);
                if (k != null) FLUID.put(k, e.getKey());
            }
            for (String s : d.fluidTags()) {
                ResourceLocation k = ResourceLocation.tryParse(s);
                if (k != null) FLUID_TAGS.add(new FluidTagRow(FluidTags.create(k), e.getKey()));
            }
            for (String s : d.materials()) {
                ResourceLocation k = ResourceLocation.tryParse(s);
                if (k != null) MATERIAL.put(k, e.getKey());
            }
            BY_PATH.put(e.getKey().getPath(), e.getKey());
        }
    }

    public static ResourceLocation lookup(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ResourceLocation direct = ITEM.get(key);
        if (direct != null) return direct;
        for (ItemTagRow row : ITEM_TAGS) {
            if (stack.is(row.tag())) return row.mol();
        }
        IFluidHandler fluids = Capabilities.FluidHandler.ITEM.getCapability(stack, null);
        if (fluids != null) {
            for (int i = 0; i < fluids.getTanks(); i++) {
                FluidStack inTank = fluids.getFluidInTank(i);
                if (inTank.isEmpty()) continue;
                ResourceLocation hit = lookup(inTank);
                if (hit == null) continue;
                MoleculeDef def = DEFS.get(hit);
                if (def != null && def.matchContainers()) return hit;
            }
        }
        if (GTCompat.isLoaded()) {
            ResourceLocation material = GTCompat.materialId(stack);
            if (material != null) {
                ResourceLocation hit = MATERIAL.get(material);
                if (hit != null) return hit;
                ResourceLocation pathHit = BY_PATH.get(material.getPath());
                if (pathHit != null) return pathHit;
            }
        }
        return BY_PATH.get(key.getPath());
    }

    public static ResourceLocation lookup(FluidStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        ResourceLocation direct = FLUID.get(key);
        if (direct != null) return direct;
        for (FluidTagRow row : FLUID_TAGS) {
            if (stack.is(row.tag())) return row.mol();
        }
        if (GTCompat.isLoaded()) {
            ResourceLocation material = GTCompat.materialId(stack.getFluid());
            if (material != null) {
                ResourceLocation hit = MATERIAL.get(material);
                if (hit != null) return hit;
                ResourceLocation pathHit = BY_PATH.get(material.getPath());
                if (pathHit != null) return pathHit;
            }
        }
        return BY_PATH.get(key.getPath());
    }

    public static Map<ResourceLocation, MoleculeDef> parseAll(Map<ResourceLocation, JsonElement> jsons) {
        Map<ResourceLocation, MoleculeDef> defs = new LinkedHashMap<>();
        List<ResourceLocation> pending = new ArrayList<>(jsons.keySet());
        for (int pass = 0; pass < 10 && !pending.isEmpty(); pass++) {
            Iterator<ResourceLocation> it = pending.iterator();
            while (it.hasNext()) {
                ResourceLocation id = it.next();
                MoleculeDef def = parse(id, jsons.get(id), defs);
                if (def != null) {
                    defs.put(id, def);
                    it.remove();
                }
            }
        }
        return defs;
    }

    public static MoleculeDef parse(ResourceLocation id, JsonElement root, Map<ResourceLocation, MoleculeDef> known) {
        try {
            if (root == null || !root.isJsonObject()) return null;
            JsonObject o = root.getAsJsonObject();
            String formula = null;
            if (o.has("formula") && o.get("formula").isJsonPrimitive()) formula = o.get("formula").getAsString();
            List<String> items = strings(o, "items");
            List<String> itemTags = tags(o, "item_tags");
            List<String> fluids = strings(o, "fluids");
            List<String> fluidTags = tags(o, "fluid_tags");
            List<String> materials = strings(o, "materials");
            boolean hasContainers = o.has("match_containers") && o.get("match_containers").isJsonPrimitive();
            boolean matchContainers = !hasContainers || o.get("match_containers").getAsBoolean();
            MoleculeStructure structure = null;
            if (o.has("copy")) {
                ResourceLocation baseId = ResourceLocation.tryParse(o.get("copy").getAsString());
                MoleculeDef base = baseId == null ? null : known.get(baseId);
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
                if (!o.get("atoms").isJsonArray() || o.getAsJsonArray("atoms").isEmpty()) return null;
                JsonArray bonds = o.has("bonds") && o.get("bonds").isJsonArray() ? o.getAsJsonArray("bonds") : null;
                structure = atoms(id, o.getAsJsonArray("atoms"), bonds);
                if (structure == null) return null;
                if (formula == null) formula = hill(structure);
            } else if (o.has("smiles")) {
                structure = SmilesParser.parse(id.toString(), o.get("smiles").getAsString());
                if (formula == null) formula = hill(structure);
            } else {
                return null;
            }
            return new MoleculeDef(id, structure, formula,
                    List.copyOf(items), List.copyOf(itemTags), List.copyOf(fluids), List.copyOf(fluidTags), List.copyOf(materials), matchContainers);
        } catch (Exception bad) {
            return null;
        }
    }

    private static MoleculeStructure atoms(ResourceLocation id, JsonArray arr, JsonArray bonds) {
        try {
            MoleculeStructure.Builder b = MoleculeStructure.builder(id.toString(), "");
            for (JsonElement e : arr) {
                JsonObject a = e.getAsJsonObject();
                b.addAtom(AtomElement.bySymbol(a.get("e").getAsString()),
                        a.get("x").getAsFloat(), a.get("y").getAsFloat(), a.get("z").getAsFloat());
            }
            if (bonds != null) {
                for (JsonElement e : bonds) {
                    JsonArray t = e.getAsJsonArray();
                    b.bond(t.get(0).getAsInt(), t.get(1).getAsInt(),
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
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            int v = e.getAsInt();
            if (v == 2) return BondType.DOUBLE;
            if (v == 3) return BondType.TRIPLE;
            if (v >= 4) return BondType.QUADRUPLE;
            return BondType.SINGLE;
        }
        String s = e.getAsString().trim().toUpperCase(java.util.Locale.ROOT);
        if (s.equals("DOUBLE") || s.equals("=") || s.equals("2")) return BondType.DOUBLE;
        if (s.equals("TRIPLE") || s.equals("#") || s.equals("3")) return BondType.TRIPLE;
        if (s.equals("QUADRUPLE") || s.equals("4")) return BondType.QUADRUPLE;
        return BondType.SINGLE;
    }

    private static String hill(MoleculeStructure mol) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MoleculeStructure.Atom a : mol.atoms()) counts.merge(a.element().symbol(), 1, Integer::sum);
        StringBuilder sb = new StringBuilder();
        take(counts, sb, "C");
        take(counts, sb, "H");
        List<String> rest = new ArrayList<>(counts.keySet());
        Collections.sort(rest);
        for (String k : rest) take(counts, sb, k);
        return sb.toString();
    }

    private static void take(Map<String, Integer> counts, StringBuilder sb, String key) {
        Integer n = counts.remove(key);
        if (n == null) return;
        sb.append(key);
        if (n > 1) {
            for (char c : String.valueOf(n).toCharArray()) sb.append((char) (0x2080 + (c - '0')));
        }
    }

    private static List<String> strings(JsonObject o, String key) {
        List<String> out = new ArrayList<>();
        if (!o.has(key) || !o.get(key).isJsonArray()) return out;
        for (JsonElement e : o.getAsJsonArray(key)) {
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) out.add(e.getAsString());
        }
        return out;
    }

    private static List<String> tags(JsonObject o, String key) {
        List<String> out = strings(o, key);
        List<String> norm = new ArrayList<>(out.size());
        for (String s : out) norm.add(s.startsWith("#") ? s.substring(1) : s);
        return norm;
    }

    private static List<String> union(List<String> a, List<String> b) {
        List<String> out = new ArrayList<>(a);
        for (String s : b) {
            if (!out.contains(s)) out.add(s);
        }
        return out;
    }
}
