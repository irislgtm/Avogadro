package com.trifectumsoftware.avogadro.compat.ntm;

import java.util.HashMap;
import java.util.Map;

final class NtmData {

    static final class Spec {

        final String formula;
        final String smiles;
        final String lattice;
        final String[] symbols;
        final int[] counts;

        Spec(String formula, String smiles, String lattice, String[] symbols, int[] counts) {
            this.formula = formula;
            this.smiles = smiles;
            this.lattice = lattice;
            this.symbols = symbols;
            this.counts = counts;
        }
    }

    private NtmData() {}

    static Spec element(String symbol, String lattice) {
        return new Spec(symbol, null, lattice, new String[] { symbol }, new int[] { 1 });
    }

    static Spec cluster(String formula, Object... parts) {
        String[] symbols = new String[parts.length / 2];
        int[] counts = new int[parts.length / 2];
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = (String) parts[i * 2];
            counts[i] = ((Integer) parts[i * 2 + 1]).intValue();
        }
        return new Spec(formula, null, null, symbols, counts);
    }

    static Spec molecule(String formula, String smiles) {
        return new Spec(formula, smiles, null, null, null);
    }

    static Spec fullerene(String formula) {
        return new Spec(formula, null, "fullerene", null, null);
    }

    private static void put(Map<String, Spec> map, String name, Spec spec) {
        map.put(name, spec);
    }

    static final Map<String, Spec> MATERIALS = new HashMap<String, Spec>();
    static final Map<String, Spec> FLUIDS = new HashMap<String, Spec>();
    static final Map<String, String> ELEMENTS = new HashMap<String, String>();
    static final Map<String, String> LATTICES = new HashMap<String, String>();
    static final Map<String, Integer> SYMBOL_COLORS = new HashMap<String, Integer>();

    static {
        put(MATERIALS, "carbon", element("C", "graphite"));
        put(MATERIALS, "coal", cluster("C", "C", 4));
        put(MATERIALS, "lignite", cluster("C", "C", 4));
        put(MATERIALS, "coalcoke", cluster("C", "C", 4));
        put(MATERIALS, "petcoke", cluster("C", "C", 4));
        put(MATERIALS, "ligcoke", cluster("C", "C", 4));
        put(MATERIALS, "lignitecoke", cluster("C", "C", 4));
        put(MATERIALS, "graphite", element("C", "graphite"));
        put(MATERIALS, "diamond", element("C", "diamond"));
        put(MATERIALS, "redstone", cluster("Fe2O3", "Fe", 2, "O", 3));
        put(MATERIALS, "obsidian", cluster("SiO2", "Si", 1, "O", 2));
        put(MATERIALS, "stone", cluster("SiO2", "Si", 1, "O", 2));
        put(MATERIALS, "hematite", cluster("Fe2O3", "Fe", 2, "O", 3));
        put(MATERIALS, "uraninite", cluster("UO2", "U", 1, "O", 2));
        put(MATERIALS, "lapis", cluster("Na8Al6Si6O24S2", "Na", 8, "Al", 6, "Si", 6, "O", 24, "S", 2));
        put(MATERIALS, "meteoriciron", cluster("Fe8Ni2", "Fe", 8, "Ni", 2));
        put(MATERIALS, "malachite", cluster("Cu2CO5H2", "Cu", 2, "C", 1, "O", 5, "H", 2));
        put(MATERIALS, "bauxite", cluster("Al2O5H4", "Al", 2, "O", 5, "H", 4));
        put(MATERIALS, "cryolite", cluster("Na3AlF6", "Na", 3, "Al", 1, "F", 6));
        put(MATERIALS, "emerald", cluster("Be3Al2Si6O18", "Be", 3, "Al", 2, "Si", 6, "O", 18));
        put(MATERIALS, "borax", cluster("Na2B4O17H20", "Na", 2, "B", 4, "O", 17, "H", 20));
        put(MATERIALS, "sodalite", cluster("Na8Al6Si6O24Cl2", "Na", 8, "Al", 6, "Si", 6, "O", 24, "Cl", 2));
        put(MATERIALS, "sulfur", cluster("S8", "S", 8));
        put(MATERIALS, "kno", cluster("KNO3", "K", 1, "N", 1, "O", 3));
        put(MATERIALS, "fluorite", cluster("CaF2", "Ca", 1, "F", 2));
        put(MATERIALS, "phosphorus", cluster("P4", "P", 4));
        put(MATERIALS, "chlorocalcite", cluster("KCaCl3", "K", 1, "Ca", 1, "Cl", 3));
        put(MATERIALS, "molysite", cluster("FeCl3", "Fe", 1, "Cl", 3));
        put(MATERIALS, "cinnabar", cluster("HgS", "Hg", 1, "S", 1));
        put(MATERIALS, "asbestos", cluster("Mg3Si2O9H4", "Mg", 3, "Si", 2, "O", 9, "H", 4));
        put(MATERIALS, "osmiridium", cluster("OsIr", "Os", 1, "Ir", 1));
        put(MATERIALS, "wood", cluster("C6H10O5", "C", 6, "H", 10, "O", 5));
        put(MATERIALS, "ivory", cluster("Ca5P3O13H", "Ca", 5, "P", 3, "O", 13, "H", 1));
        put(MATERIALS, "bone", cluster("Ca5P3O13H", "Ca", 5, "P", 3, "O", 13, "H", 1));
        put(MATERIALS, "saltpeter", cluster("KNO3", "K", 1, "N", 1, "O", 3));
        put(MATERIALS, "redphosphorus", cluster("P4", "P", 4));
        put(MATERIALS, "wroughtiron", element("Fe", "bcc"));
        put(MATERIALS, "pigiron", element("Fe", "bcc"));
        put(MATERIALS, "advancedalloy", cluster("Fe3Cu2CO", "Fe", 3, "Cu", 2, "C", 1, "O", 1));
        put(MATERIALS, "durasteel", cluster("Fe15C5W3Co", "Fe", 15, "C", 5, "W", 3, "Co", 1));
        put(
            MATERIALS,
            "workersalloy",
            cluster(
                "B2La2CeCoLiNdNbHg2C",
                "B",
                2,
                "La",
                2,
                "Ce",
                1,
                "Co",
                1,
                "Li",
                1,
                "Nd",
                1,
                "Nb",
                1,
                "Hg",
                2,
                "C",
                1));
        put(
            MATERIALS,
            "starmetal",
            cluster("Fe45C15W9Co6At2Hg5", "Fe", 45, "C", 15, "W", 9, "Co", 6, "At", 2, "Hg", 5));
        put(MATERIALS, "ferrouranium", cluster("Fe6C2U", "Fe", 6, "C", 2, "U", 1));
        put(MATERIALS, "bismuthbronze", cluster("Cu8Bi", "Cu", 8, "Bi", 1));
        put(MATERIALS, "arsenicbronze", cluster("Cu8As", "Cu", 8, "As", 1));
        put(MATERIALS, "magnetizedtungsten", cluster("WSa", "W", 1, "Sa", 1));
        put(MATERIALS, "cmbsteel", cluster("W6Sa6", "W", 6, "Sa", 6));
        put(MATERIALS, "dineutronium", cluster("Dn", "Dn", 8));
        put(MATERIALS, "saturnite", cluster("Fe15C5W3CoCu", "Fe", 15, "C", 5, "W", 3, "Co", 1, "Cu", 1));
        put(MATERIALS, "polycarbonate", molecule("(C16H14O3)n", "CC(C)(c1ccc(O)cc1)c1ccc(O)cc1"));
        put(MATERIALS, "watzmud", cluster("Mud", "Mud", 8));
        put(MATERIALS, "ghiorsium336", cluster("Gh", "Gh", 8));
        put(MATERIALS, "flux", cluster("CaF2", "Ca", 1, "F", 2));
        put(MATERIALS, "slag", cluster("CaSiO3", "Ca", 1, "Si", 1, "O", 3));
        put(MATERIALS, "steel", cluster("Fe3C", "Fe", 3, "C", 1));
        put(MATERIALS, "mingrade", cluster("Cu2O", "Cu", 2, "O", 1));
        put(MATERIALS, "alloy", cluster("Fe3Cu2CO", "Fe", 3, "Cu", 2, "C", 1, "O", 1));
        put(MATERIALS, "dura", cluster("Fe15C5W3Co", "Fe", 15, "C", 5, "W", 3, "Co", 1));
        put(
            MATERIALS,
            "desh",
            cluster(
                "B2La2CeCoLiNdNbHg2C",
                "B",
                2,
                "La",
                2,
                "Ce",
                1,
                "Co",
                1,
                "Li",
                1,
                "Nd",
                1,
                "Nb",
                1,
                "Hg",
                2,
                "C",
                1));
        put(MATERIALS, "star", cluster("FeNiCo", "Fe", 1, "Ni", 1, "Co", 1));
        put(MATERIALS, "ferro", cluster("Fe6C2U", "Fe", 6, "C", 2, "U", 1));
        put(MATERIALS, "tcalloy", cluster("Fe24C8Tc", "Fe", 24, "C", 8, "Tc", 1));
        put(MATERIALS, "cdalloy", cluster("Fe24C8Cd", "Fe", 24, "C", 8, "Cd", 1));
        put(MATERIALS, "bbronze", cluster("Cu8Bi", "Cu", 8, "Bi", 1));
        put(MATERIALS, "abronze", cluster("Cu8As", "Cu", 8, "As", 1));
        put(MATERIALS, "bscco", cluster("Bi2Sr2Ca2Cu3O10", "Bi", 2, "Sr", 2, "Ca", 2, "Cu", 3, "O", 10));
        put(MATERIALS, "magtung", cluster("WSa", "W", 1, "Sa", 1));
        put(MATERIALS, "cmb", cluster("W6Sa6", "W", 6, "Sa", 6));
        put(MATERIALS, "gunmetal", cluster("Cu3Al", "Cu", 3, "Al", 1));
        put(MATERIALS, "weaponsteel", cluster("Fe3C", "Fe", 3, "C", 1));
        put(MATERIALS, "saturn", cluster("Fe15C5W3CoCu", "Fe", 15, "C", 5, "W", 3, "Co", 1, "Cu", 1));
        put(MATERIALS, "rareearth", cluster("LaCeNd", "La", 1, "Ce", 1, "Nd", 1));
        put(
            MATERIALS,
            "rare",
            cluster("Zr6B8La6Ce3Co3Li3Nd3Nb3", "Zr", 6, "B", 8, "La", 6, "Ce", 3, "Co", 3, "Li", 3, "Nd", 3, "Nb", 3));
        put(MATERIALS, "trixite", cluster("Pu3Co4Nb4", "Pu", 3, "Co", 4, "Nb", 4));
        put(MATERIALS, "schrabidium", cluster("Sa", "Sa", 8));
        put(MATERIALS, "solinium", cluster("Sa", "Sa", 8));
        put(MATERIALS, "schrabidate", cluster("SaO2", "Sa", 1, "O", 2));
        put(MATERIALS, "schraranium", cluster("SaU", "Sa", 1, "U", 1));
        put(MATERIALS, "ghiorsium", cluster("Gh", "Gh", 8));
        put(MATERIALS, "dnt", cluster("Dn", "Dn", 8));
        put(MATERIALS, "mud", cluster("Mud", "Mud", 8));
        put(MATERIALS, "polymer", molecule("(C2H4)n", "CCCCCCCCCCCC"));
        put(MATERIALS, "bakelite", molecule("(C7H6O)n", "Oc1ccccc1.C=O"));
        put(MATERIALS, "rubber", molecule("(C5H8)n", "CC(=C)C=CCC(=C)C=C"));
        put(MATERIALS, "hardplastic", molecule("(C16H14O3)n", "CC(C)(c1ccc(O)cc1)c1ccc(O)cc1"));
        put(MATERIALS, "pvc", molecule("(C2H3Cl)n", "CC(Cl)CC(Cl)"));

        el("iron", "Fe");
        el("gold", "Au");
        el("copper", "Cu");
        el("tungsten", "W");
        el("aluminium", "Al");
        el("aluminum", "Al");
        el("lead", "Pb");
        el("bismuth", "Bi");
        el("arsenic", "As");
        el("tantalium", "Ta");
        el("tantalum", "Ta");
        el("neodymium", "Nd");
        el("niobium", "Nb");
        el("beryllium", "Be");
        el("cobalt", "Co");
        el("boron", "B");
        el("lanthanium", "La");
        el("lanthanum", "La");
        el("zirconium", "Zr");
        el("sodium", "Na");
        el("strontium", "Sr");
        el("calcium", "Ca");
        el("lithium", "Li");
        el("cadmium", "Cd");
        el("silicon", "Si");
        el("titanium", "Ti");
        el("uranium", "U");
        el("thorium", "Th");
        el("plutonium", "Pu");
        el("americium", "Am");
        el("neptunium", "Np");
        el("polonium", "Po");
        el("technetium", "Tc");
        el("radium", "Ra");
        el("actinium", "Ac");
        el("osmium", "Os");
        el("iridium", "Ir");
        el("tin", "Sn");
        el("silver", "Ag");
        el("nickel", "Ni");
        el("platinum", "Pt");
        el("palladium", "Pd");
        el("chromium", "Cr");
        el("manganese", "Mn");
        el("magnesium", "Mg");
        el("zinc", "Zn");
        el("mercury", "Hg");
        el("potassium", "K");
        el("rubidium", "Rb");
        el("caesium", "Cs");
        el("cesium", "Cs");
        el("barium", "Ba");
        el("cerium", "Ce");
        el("scandium", "Sc");
        el("yttrium", "Y");
        el("hafnium", "Hf");
        el("rhenium", "Re");
        el("ruthenium", "Ru");
        el("rhodium", "Rh");
        el("europium", "Eu");
        el("gadolinium", "Gd");
        el("terbium", "Tb");
        el("dysprosium", "Dy");
        el("holmium", "Ho");
        el("erbium", "Er");
        el("thulium", "Tm");
        el("ytterbium", "Yb");
        el("lutetium", "Lu");
        el("scandium", "Sc");
        el("praseodymium", "Pr");
        el("samarium", "Sm");
        el("promethium", "Pm");

        lat("Fe", "bcc");
        lat("W", "bcc");
        lat("Nb", "bcc");
        lat("Ta", "bcc");
        lat("Mo", "bcc");
        lat("V", "bcc");
        lat("Cr", "bcc");
        lat("Li", "bcc");
        lat("Na", "bcc");
        lat("K", "bcc");
        lat("Rb", "bcc");
        lat("Cs", "bcc");
        lat("Ba", "bcc");
        lat("Cu", "fcc");
        lat("Al", "fcc");
        lat("Au", "fcc");
        lat("Ag", "fcc");
        lat("Ni", "fcc");
        lat("Pt", "fcc");
        lat("Pd", "fcc");
        lat("Pb", "fcc");
        lat("Ir", "fcc");
        lat("Rh", "fcc");
        lat("Ca", "fcc");
        lat("Sr", "fcc");
        lat("Ce", "fcc");
        lat("Th", "fcc");
        lat("Yb", "fcc");
        lat("Ac", "fcc");
        lat("La", "fcc");
        lat("Nd", "fcc");
        lat("Zr", "fcc");
        lat("Ti", "fcc");
        lat("Co", "fcc");
        lat("Cd", "fcc");
        lat("Be", "fcc");
        lat("Hf", "fcc");
        lat("Sc", "fcc");
        lat("Y", "fcc");
        lat("C", "diamond");
        lat("Si", "diamond");
        lat("Ge", "diamond");
        lat("Sn", "diamond");
        lat("U", "simple");
        lat("Pu", "simple");
        lat("Np", "simple");
        lat("Am", "simple");
        lat("Po", "simple");
        lat("Ra", "simple");
        lat("Bi", "simple");
        lat("As", "simple");
        lat("B", "simple");
        lat("S", "simple");
        lat("P", "simple");
        lat("Se", "simple");
        lat("Te", "simple");

        color("Sa", 0x32FFFF);
        color("So", 0xA2E6E0);
        color("Sd", 0x77C0D7);
        color("Gh", 0xF4EFE1);
        color("Dn", 0x7582B9);
        color("Mud", 0xBCB5A9);
        color("St", 0xCCCCEA);

        put(FLUIDS, "WATER", molecule("H2O", "O"));
        put(FLUIDS, "STEAM", molecule("H2O", "O"));
        put(FLUIDS, "HOTSTEAM", molecule("H2O", "O"));
        put(FLUIDS, "SUPERHOTSTEAM", molecule("H2O", "O"));
        put(FLUIDS, "ULTRAHOTSTEAM", molecule("H2O", "O"));
        put(FLUIDS, "SPENTSTEAM", molecule("H2O", "O"));
        put(FLUIDS, "HEAVYWATER", molecule("D2O", "O"));
        put(FLUIDS, "HEAVYWATER_HOT", molecule("D2O", "O"));
        put(FLUIDS, "HYDROGEN", molecule("H2", "[H][H]"));
        put(FLUIDS, "DEUTERIUM", molecule("D2", "[H][H]"));
        put(FLUIDS, "TRITIUM", molecule("T2", "[H][H]"));
        put(FLUIDS, "OXYGEN", molecule("O2", "[O][O]"));
        put(FLUIDS, "AIR", molecule("N2+O2+Ar", "[N][N].[O][O].[Ar]"));
        put(FLUIDS, "CARBONDIOXIDE", molecule("CO2", "O=C=O"));
        put(FLUIDS, "SULFURIC_ACID", molecule("H2SO4", "OS(=O)(=O)O"));
        put(FLUIDS, "VITRIOL", molecule("H2SO4", "OS(=O)(=O)O"));
        put(FLUIDS, "NITRIC_ACID", molecule("HNO3", "O[N+](=O)[O-]"));
        put(FLUIDS, "PEROXIDE", molecule("H2O2", "OO"));
        put(FLUIDS, "CHLORINE", molecule("Cl2", "[Cl][Cl]"));
        put(FLUIDS, "FLUORINE", molecule("F2", "[F][F]"));
        put(FLUIDS, "XENON", molecule("Xe", "[Xe]"));
        put(FLUIDS, "HELIUM3", molecule("He", "[He]"));
        put(FLUIDS, "HELIUM4", molecule("He", "[He]"));
        put(FLUIDS, "MERCURY", molecule("Hg", "[Hg]"));
        put(FLUIDS, "SODIUM", molecule("Na", "[Na]"));
        put(FLUIDS, "LEAD", molecule("Pb", "[Pb]"));
        put(FLUIDS, "ETHANOL", molecule("C2H6O", "CCO"));
        put(FLUIDS, "AMMONIA", molecule("NH3", "N"));
        put(FLUIDS, "PHOSGENE", molecule("COCl2", "O=C(Cl)Cl"));
        put(FLUIDS, "MUSTARDGAS", molecule("C4H8Cl2S", "ClCCSCCCl"));
        put(FLUIDS, "NITROGLYCERIN", molecule("C3H5N3O9", "O=[N+]([O-])OCC(CO[N+](=O)[O-])O[N+](=O)[O-]"));
        put(FLUIDS, "UF6", cluster("UF6", "U", 1, "F", 6));
        put(FLUIDS, "PUF6", cluster("PuF6", "Pu", 1, "F", 6));
        put(FLUIDS, "SYNGAS", molecule("CO+H2", "[C-]#[O+].[H][H]"));
        put(FLUIDS, "OXYHYDROGEN", molecule("H2+O2", "[H][H].[O][O]"));
        put(FLUIDS, "REFORMGAS", molecule("H2+CO+CH4", "[H][H].[C-]#[O+].C"));
        put(FLUIDS, "BIOGAS", molecule("CH4+CO2", "C.O=C=O"));
        put(FLUIDS, "BIOFUEL", molecule("C2H6O+C19H36O2", "CCO.CCCCCCCCC=CCCCCCCCC(=O)OC"));
        put(FLUIDS, "LPG", molecule("C3H8+C4H10", "CCC.CCCC"));
        put(FLUIDS, "GAS", molecule("CH4+C2H6+C3H8", "C.CC.CCC"));
        put(FLUIDS, "SOURGAS", molecule("H2S+CO2", "S.O=C=O"));
        put(FLUIDS, "NAPHTHA", molecule("C5H12-C12H26", "CCCCC.CCCCCC.CCCCCCC.CCCCCCCC"));
        put(FLUIDS, "NAPHTHA_CRACK", molecule("C5H12-C12H26", "CCCCC.CCCCCC.CCCCCCC.CCCCCCCC"));
        put(FLUIDS, "NAPHTHA_DS", molecule("C5H12-C12H26", "CCCCC.CCCCCC.CCCCCCC.CCCCCCCC"));
        put(FLUIDS, "NAPHTHA_COKER", molecule("C5H12-C12H26", "CCCCC.CCCCCC.CCCCCCC.CCCCCCCC"));
        put(FLUIDS, "GASOLINE", molecule("C4-C12 hydrocarbons", "CC(C)(C)CC(C)(C)C.CCCCCCC.c1ccccc1.Cc1ccccc1"));
        put(FLUIDS, "GASOLINE_LEADED", molecule("C4-C12 hydrocarbons", "CC(C)(C)CC(C)(C)C.CCCCCCC.c1ccccc1.Cc1ccccc1"));
        put(FLUIDS, "COALGAS", molecule("CH4+CO+H2", "C.[H][H].[C-]#[O+]"));
        put(FLUIDS, "COALGAS_LEADED", molecule("CH4+CO+H2", "C.[H][H].[C-]#[O+]"));
        put(FLUIDS, "DIESEL", molecule("C10H22-C16H34", "CCCCCCCCCCCCCCCC.CCCCCCC"));
        put(FLUIDS, "DIESEL_CRACK", molecule("C10H22-C16H34", "CCCCCCCCCCCCCCCC.CCCCCCC"));
        put(FLUIDS, "DIESEL_REFORM", molecule("C10H22-C16H34", "CCCCCCCCCCCCCCCC.CCCCCCC"));
        put(FLUIDS, "DIESEL_CRACK_REFORM", molecule("C10H22-C16H34", "CCCCCCCCCCCCCCCC.CCCCCCC"));
        put(FLUIDS, "KEROSENE", molecule("C10H22-C16H34", "CCCCCCCCCCCC.c1ccc2ccccc2c1"));
        put(FLUIDS, "KEROSENE_REFORM", molecule("C10H22-C16H34", "CCCCCCCCCCCC.c1ccc2ccccc2c1"));
        put(FLUIDS, "LUBRICANT", molecule("C18H38-C30H62", "CCCCCCCCCCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "HEATINGOIL", molecule("C12H26-C20H42", "CCCCCCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "HEATINGOIL_VACUUM", molecule("C12H26-C20H42", "CCCCCCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "BITUMEN", molecule("C20+ polyaromatics", "c1ccc2ccccc2c1.c1ccc2ccccc2c1"));
        put(FLUIDS, "OIL", molecule("alkanes+aromatics", "CCCCCC.CCCCCCCCC.C1CCCCC1.c1ccccc1"));
        put(FLUIDS, "PETROLEUM", molecule("alkanes+aromatics", "CCCCCC.CCCCCCCCC.C1CCCCC1.c1ccccc1"));
        put(FLUIDS, "CRACKOIL", molecule("alkanes+aromatics", "CCCCCC.CCCCCCCCC.C1CCCCC1.c1ccccc1"));
        put(FLUIDS, "COALOIL", molecule("phenols+aromatics", "Oc1ccccc1.Cc1ccccc1O.c1ccc2ccccc2c1"));
        put(FLUIDS, "COALCREOSOTE", molecule("phenols+aromatics", "Oc1ccccc1.Cc1ccccc1O.c1ccc2ccccc2c1"));
        put(FLUIDS, "AROMATICS", molecule("C6H6+C7H8+C8H10", "c1ccccc1.Cc1ccccc1.Cc1ccccc1C"));
        put(FLUIDS, "XYLENE", molecule("C8H10", "Cc1ccccc1C"));
        put(FLUIDS, "UNSATURATEDS", molecule("C2H4+C3H6+C4H6", "C=C.CC=C.C=CC=C"));
        put(FLUIDS, "SOLVENT", molecule("naphtha+aromatics", "CCCCC.CCCCCC.c1ccccc1.Cc1ccccc1"));
        put(FLUIDS, "REFORMATE", molecule("C6H6+C7H8+C8H10", "c1ccccc1.Cc1ccccc1.Cc1ccccc1C"));
        put(FLUIDS, "SMEAR", molecule("C20+ hydrocarbons", "CCCCCCCCCCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "RECLAIMED", molecule("C12+ hydrocarbons", "CCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "PETROIL", molecule("C12+ hydrocarbons", "CCCCCCCCCCCC.CCCCCCCCCCCCCCCCCCCC"));
        put(FLUIDS, "FRACKSOL", molecule("H2SO4+H2O+hydrocarbons", "OS(=O)(=O)O.O.CCCCCC"));
        put(FLUIDS, "COOLANT", molecule("H2O+KNO3", "O.[K][N](=O)=O"));
        put(FLUIDS, "CRYOGEL", molecule("H2O", "O"));
        put(FLUIDS, "NITAN", molecule("C12H26+Hg", "CCCCCCCCCCCC.[Hg]"));
        put(FLUIDS, "PERFLUOROMETHYL", molecule("C2F6", "FC(F)(F)C(F)(F)F"));
        put(FLUIDS, "RADIOSOLVENT", molecule("CH2Cl2", "ClCCl"));
        put(FLUIDS, "THORIUM_SALT", cluster("ThCl4", "Th", 1, "Cl", 4));
        put(FLUIDS, "SODIUM_ALUMINATE", cluster("NaAlO2", "Na", 1, "Al", 1, "O", 2));
        put(FLUIDS, "ALUMINA", cluster("Al2O3", "Al", 2, "O", 3));
        put(FLUIDS, "LYE", cluster("NaOH", "Na", 1, "O", 1, "H", 1));
        put(FLUIDS, "POTASSIUM_CHLORIDE", cluster("KCl", "K", 1, "Cl", 1));
        put(FLUIDS, "CALCIUM_CHLORIDE", cluster("CaCl2", "Ca", 1, "Cl", 2));
        put(FLUIDS, "CALCIUM_SOLUTION", cluster("CaO2H2", "Ca", 1, "O", 2, "H", 2));
        put(FLUIDS, "CONCRETE", cluster("CaSiO3", "Ca", 1, "Si", 1, "O", 3));
        put(FLUIDS, "WOODOIL", molecule("terpenes+phenols", "CC1=CCCC(C1)C(C)=C.Oc1ccccc1"));
        put(FLUIDS, "FULLERENE", fullerene("C60"));
        put(FLUIDS, "SCHRABIDIC", cluster("HSaO4", "H", 1, "Sa", 1, "O", 4));
        put(FLUIDS, "WATZ", cluster("Mud", "Mud", 8));
    }

    private static void el(String name, String symbol) {
        ELEMENTS.put(name, symbol);
    }

    private static void lat(String symbol, String lattice) {
        LATTICES.put(symbol, lattice);
    }

    private static void color(String symbol, int color) {
        SYMBOL_COLORS.put(symbol, Integer.valueOf(color));
    }

    static Spec material(String name) {
        return name == null ? null : MATERIALS.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    static Spec fluid(String name) {
        return name == null ? null : FLUIDS.get(name);
    }

    static String element(String name) {
        if (name == null) return null;
        String key = name.toLowerCase(java.util.Locale.ROOT);
        String direct = ELEMENTS.get(key);
        if (direct != null) return direct;
        int end = key.length();
        while (end > 0 && Character.isDigit(key.charAt(end - 1))) end--;
        if (end > 0 && end < key.length()) {
            direct = ELEMENTS.get(key.substring(0, end));
            if (direct != null) return direct;
        }
        if (key.endsWith("rg")) {
            direct = ELEMENTS.get(key.substring(0, key.length() - 2));
            if (direct != null) return direct;
        }
        return null;
    }

    static String lattice(String symbol) {
        String lattice = LATTICES.get(symbol);
        return lattice == null ? "fcc" : lattice;
    }

    static int symbolColor(String symbol) {
        Integer color = SYMBOL_COLORS.get(symbol);
        return color == null ? 0 : color.intValue();
    }
}
