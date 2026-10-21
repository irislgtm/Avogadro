package com.trifectumsoftware.avogadro.compat.ntm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.api.AvogadroAPI;
import com.trifectumsoftware.avogadro.molecule.AtomElement;
import com.trifectumsoftware.avogadro.molecule.MoleculeGenerators;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import com.trifectumsoftware.avogadro.molecule.MoleculeStructure;
import com.trifectumsoftware.avogadro.molecule.SmilesParser;

import cpw.mods.fml.common.Loader;

public final class NtmCompat {

    private static final String MODID = "hbm";
    private static final Set<String> IDS = new HashSet<String>();
    private static final Map<Integer, String> MATERIAL_IDS = new HashMap<Integer, String>();
    private static final Map<Integer, String> FLUID_IDS = new HashMap<Integer, String>();
    private static final Map<String, String> ITEM_IDS = new HashMap<String, String>();
    private static final Map<String, String> ITEM_ALIASES = new HashMap<String, String>();
    private static boolean registered;

    static {
        ITEM_ALIASES.put("niter", "saltpeter");
        ITEM_ALIASES.put("cinnebar", "cinnabar");
    }

    private NtmCompat() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    public static void register() {
        if (registered) return;
        registered = true;
        for (NTMMaterial material : Mats.orderedList) {
            registerMaterial(material);
            if (!MATERIAL_IDS.containsKey(Integer.valueOf(material.id)) && material.names != null
                && material.names.length > 0) {
                Avogadro.LOG.info("No NTM molecule for material {}", material.names[0]);
            }
        }
        for (FluidType fluid : Fluids.getAll()) {
            registerFluid(fluid);
        }
        Avogadro.LOG.info(
            "Registered {} NTM material molecules and {} NTM fluid molecules",
            Integer.valueOf(MATERIAL_IDS.size()),
            Integer.valueOf(FLUID_IDS.size()));
    }

    public static String lookup(ItemStack stack) {
        if (!isLoaded() || !Config.ntmCompat) return null;
        if (stack == null || stack.getItem() == null) return null;
        register();
        if (stack.getItem() == ModItems.fluid_icon) {
            String id = fluidId(Fluids.fromID(stack.getItemDamage()));
            if (id != null) return id;
        }
        String unlocalized = stack.getItem()
            .getUnlocalizedName();
        if (unlocalized != null && unlocalized.startsWith("item.crystal_")) {
            String id = itemId(unlocalized.substring(13));
            if (id != null) return id;
        }
        Object name = Item.itemRegistry.getNameForObject(stack.getItem());
        if (name != null && name.toString()
            .startsWith(MODID + ":")) {
            String path = name.toString()
                .substring(MODID.length() + 1);
            if (path.startsWith("crystal_")) {
                String id = itemId(path.substring(8));
                if (id != null) return id;
            }
            String id = fluidId(FluidContainerRegistry.getFluidType(stack));
            if (id != null) return id;
        }
        String ore = lookupOre(stack);
        if (ore != null) return ore;
        if (name == null || !name.toString()
            .startsWith(MODID + ":")) return null;
        List<MaterialStack> materials = Mats.getMaterialsFromItem(stack);
        for (MaterialStack entry : materials) {
            if (entry == null || entry.material == null) continue;
            registerMaterial(entry.material);
            String id = MATERIAL_IDS.get(Integer.valueOf(entry.material.id));
            if (id != null) return id;
        }
        return null;
    }

    private static String lookupOre(ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int oreId : ids) {
            String name = OreDictionary.getOreName(oreId);
            String suffix = null;
            int best = 0;
            for (String prefix : Mats.prefixByName.keySet()) {
                if (prefix.length() <= best || !name.startsWith(prefix)) continue;
                best = prefix.length();
                suffix = name.substring(prefix.length());
            }
            if (suffix == null || suffix.isEmpty()) continue;
            NTMMaterial material = Mats.matByName.get(suffix);
            if (material == null) continue;
            registerMaterial(material);
            String id = MATERIAL_IDS.get(Integer.valueOf(material.id));
            if (id != null) return id;
        }
        return null;
    }

    private static void registerMaterial(NTMMaterial material) {
        if (material == null || material.names == null || material.names.length == 0) return;
        if (MATERIAL_IDS.containsKey(Integer.valueOf(material.id))) return;
        String name = material.names[0];
        if (name == null || name.isEmpty()) return;
        NtmData.Spec spec = NtmData.material(name);
        if (spec == null) {
            String symbol = NtmData.element(name);
            if (symbol != null) spec = NtmData.element(symbol, NtmData.lattice(symbol));
        }
        if (spec == null) return;
        String id = unique("avogadro:ntm_" + slug(name));
        MATERIAL_IDS.put(Integer.valueOf(material.id), id);
        if (MoleculeStore.get(id) != null) return;
        AvogadroAPI.builder(id)
            .structure(structure(id, spec))
            .formula(pretty(spec.formula))
            .register();
    }

    private static void registerFluid(FluidType fluid) {
        if (fluid == null || fluid.getID() <= 0) return;
        if (FLUID_IDS.containsKey(Integer.valueOf(fluid.getID()))) return;
        NtmData.Spec spec = NtmData.fluid(fluid.getName());
        if (spec == null) return;
        String id = unique("avogadro:ntm_fluid_" + slug(fluid.getName()));
        FLUID_IDS.put(Integer.valueOf(fluid.getID()), id);
        if (MoleculeStore.get(id) != null) return;
        AvogadroAPI.builder(id)
            .structure(structure(id, spec))
            .formula(pretty(spec.formula))
            .register();
    }

    private static String fluidId(FluidType fluid) {
        if (fluid == null || fluid.getID() <= 0) return null;
        return FLUID_IDS.get(Integer.valueOf(fluid.getID()));
    }

    private static String itemId(String suffix) {
        String key = suffix.toLowerCase(Locale.ROOT);
        String cached = ITEM_IDS.get(key);
        if (cached != null) return cached;
        String alias = ITEM_ALIASES.get(key);
        if (alias != null) key = alias;
        NtmData.Spec spec = NtmData.material(key);
        if (spec == null) {
            String symbol = NtmData.element(key);
            if (symbol != null) spec = NtmData.element(symbol, NtmData.lattice(symbol));
        }
        if (spec == null) return null;
        String id = unique("avogadro:ntm_item_" + slug(key));
        ITEM_IDS.put(key, id);
        if (MoleculeStore.get(id) != null) return id;
        AvogadroAPI.builder(id)
            .structure(structure(id, spec))
            .formula(pretty(spec.formula))
            .register();
        return id;
    }

    private static MoleculeStructure structure(String id, NtmData.Spec spec) {
        if (spec.smiles != null) {
            MoleculeStructure structure = SmilesParser.parse(id, spec.smiles);
            if (structure != null && !structure.atoms()
                .isEmpty()) return structure;
        }
        if ("fullerene".equals(spec.lattice)) {
            MoleculeStructure structure = MoleculeGenerators.fullerene(id, 60);
            if (structure != null && !structure.atoms()
                .isEmpty()) return structure;
        }
        if (spec.lattice != null && spec.symbols != null
            && spec.symbols.length == 1
            && spec.counts != null
            && spec.counts.length == 1
            && spec.counts[0] == 1) {
            MoleculeStructure structure = MoleculeGenerators
                .crystal(id, spec.lattice, Collections.singletonList(spec.symbols[0]), 2, 2);
            if (structure != null && !structure.atoms()
                .isEmpty()) return structure;
        }
        return cluster(id, spec);
    }

    private static MoleculeStructure cluster(String id, NtmData.Spec spec) {
        int total = 0;
        for (int count : spec.counts) total += count;
        int size = Math.max(1, (int) Math.ceil(Math.cbrt(total)));
        List<float[]> sites = new ArrayList<float[]>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    sites.add(new float[] { x, y, z });
                }
            }
        }
        float center = (size - 1) / 2f;
        MoleculeStructure.Builder builder = MoleculeStructure.builder(id);
        int index = 0;
        for (int i = 0; i < spec.symbols.length; i++) {
            AtomElement element = atom(spec.symbols[i]);
            for (int count = 0; count < spec.counts[i]; count++) {
                float[] site = sites.get(index++);
                builder.addAtom(element, site[0] - center, site[1] - center, site[2] - center);
            }
        }
        for (int i = 0; i < total; i++) {
            float[] a = sites.get(i);
            for (int j = i + 1; j < total; j++) {
                float[] b = sites.get(j);
                float dx = a[0] - b[0];
                float dy = a[1] - b[1];
                float dz = a[2] - b[2];
                if (dx * dx + dy * dy + dz * dz <= 1.05f) {
                    builder.bond(i, j);
                }
            }
        }
        return builder.build();
    }

    private static AtomElement atom(String symbol) {
        int color = NtmData.symbolColor(symbol);
        if (color != 0) return new AtomElement(symbol, 0xFF000000 | color, 0.4f);
        return AtomElement.bySymbol(symbol);
    }

    private static String unique(String base) {
        String id = base;
        int suffix = 2;
        while (IDS.contains(id)) {
            id = base + "_" + suffix++;
        }
        IDS.add(id);
        return id;
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9_]", "_");
    }

    private static String pretty(String formula) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c >= '0' && c <= '9') {
                builder.append((char) (0x2080 + (c - '0')));
            } else if (c == 'n') {
                builder.append('\u2099');
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
