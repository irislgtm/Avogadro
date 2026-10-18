package com.trifectumsoftware.avogadro.molecule;

public final class AtomElement {

    public static final AtomElement HYDROGEN = new AtomElement("H", 0xFFFFFFFF, 0.25f);
    public static final AtomElement CARBON = new AtomElement("C", 0xFF3A3A3A, 0.40f);
    public static final AtomElement NITROGEN = new AtomElement("N", 0xFF3050F8, 0.35f);
    public static final AtomElement OXYGEN = new AtomElement("O", 0xFFE63333, 0.35f);
    public static final AtomElement FLUORINE = new AtomElement("F", 0xFF90E050, 0.30f);
    public static final AtomElement CHLORINE = new AtomElement("Cl", 0xFF1FF01F, 0.35f);
    public static final AtomElement BROMINE = new AtomElement("Br", 0xFFA62929, 0.40f);
    public static final AtomElement IODINE = new AtomElement("I", 0xFF940094, 0.45f);
    public static final AtomElement SULFUR = new AtomElement("S", 0xFFD9D920, 0.40f);
    public static final AtomElement SELENIUM = new AtomElement("Se", 0xFFFFA100, 0.40f);
    public static final AtomElement PHOSPHORUS = new AtomElement("P", 0xFFFF8000, 0.40f);
    public static final AtomElement BORON = new AtomElement("B", 0xFFFFB5B5, 0.40f);
    public static final AtomElement SILICON = new AtomElement("Si", 0xFFF0C8A0, 0.42f);
    public static final AtomElement SODIUM = new AtomElement("Na", 0xFFAB5CF2, 0.45f);
    public static final AtomElement POTASSIUM = new AtomElement("K", 0xFF8F40D4, 0.48f);
    public static final AtomElement LITHIUM = new AtomElement("Li", 0xFFCC80FF, 0.42f);
    public static final AtomElement CALCIUM = new AtomElement("Ca", 0xFF3DFF00, 0.46f);
    public static final AtomElement MAGNESIUM = new AtomElement("Mg", 0xFF8AFF00, 0.44f);
    public static final AtomElement ALUMINIUM = new AtomElement("Al", 0xFFBFA6A6, 0.44f);
    public static final AtomElement IRON = new AtomElement("Fe", 0xFFE06633, 0.42f);
    public static final AtomElement COPPER = new AtomElement("Cu", 0xFFC88033, 0.42f);
    public static final AtomElement ZINC = new AtomElement("Zn", 0xFF7D80B0, 0.42f);
    public static final AtomElement TIN = new AtomElement("Sn", 0xFF668080, 0.44f);
    public static final AtomElement LEAD = new AtomElement("Pb", 0xFF575961, 0.46f);
    public static final AtomElement SILVER = new AtomElement("Ag", 0xFFC0C0C0, 0.44f);
    public static final AtomElement GOLD = new AtomElement("Au", 0xFFFFD123, 0.44f);
    public static final AtomElement MANGANESE = new AtomElement("Mn", 0xFF9C7AC7, 0.42f);
    public static final AtomElement CHROMIUM = new AtomElement("Cr", 0xFF8A99C7, 0.42f);
    public static final AtomElement NICKEL = new AtomElement("Ni", 0xFF50D050, 0.42f);
    public static final AtomElement COBALT = new AtomElement("Co", 0xFFF090A0, 0.42f);
    public static final AtomElement TITANIUM = new AtomElement("Ti", 0xFFBFC2C7, 0.43f);
    public static final AtomElement TUNGSTEN = new AtomElement("W", 0xFF2194D6, 0.44f);
    public static final AtomElement OSMIUM = new AtomElement("Os", 0xFF26558F, 0.43f);
    public static final AtomElement URANIUM = new AtomElement("U", 0xFF008FFF, 0.46f);
    public static final AtomElement PLUTONIUM = new AtomElement("Pu", 0xFF006B6B, 0.46f);
    public static final AtomElement POLONIUM = new AtomElement("Po", 0xFFAB5C00, 0.45f);
    public static final AtomElement ARGON = new AtomElement("Ar", 0xFF80D1E3, 0.40f);
    public static final AtomElement KRYPTON = new AtomElement("Kr", 0xFF5CB8D1, 0.42f);
    public static final AtomElement HELIUM = new AtomElement("He", 0xFFD9FFFF, 0.28f);
    public static final AtomElement NEON = new AtomElement("Ne", 0xFFB3E3F5, 0.34f);
    public static final AtomElement XENON = new AtomElement("Xe", 0xFF429EB0, 0.44f);

    private final String symbol;
    private final int color;
    private final float radius;

    public AtomElement(String symbol, int color, float radius) {
        this.symbol = symbol;
        this.color = color;
        this.radius = radius;
    }

    public String symbol() {
        return symbol;
    }

    public int color() {
        return color;
    }

    public float radius() {
        return radius;
    }

    public static AtomElement bySymbol(String s) {
        if (s == null) return new AtomElement("?", 0xFFFF88FF, 0.35f);
        if ("H".equals(s)) return HYDROGEN;
        if ("C".equals(s)) return CARBON;
        if ("N".equals(s)) return NITROGEN;
        if ("O".equals(s)) return OXYGEN;
        if ("F".equals(s)) return FLUORINE;
        if ("Cl".equals(s)) return CHLORINE;
        if ("Br".equals(s)) return BROMINE;
        if ("I".equals(s)) return IODINE;
        if ("S".equals(s)) return SULFUR;
        if ("Se".equals(s)) return SELENIUM;
        if ("P".equals(s)) return PHOSPHORUS;
        if ("B".equals(s)) return BORON;
        if ("Si".equals(s)) return SILICON;
        if ("Na".equals(s)) return SODIUM;
        if ("K".equals(s)) return POTASSIUM;
        if ("Li".equals(s)) return LITHIUM;
        if ("Ca".equals(s)) return CALCIUM;
        if ("Mg".equals(s)) return MAGNESIUM;
        if ("Al".equals(s)) return ALUMINIUM;
        if ("Fe".equals(s)) return IRON;
        if ("Cu".equals(s)) return COPPER;
        if ("Zn".equals(s)) return ZINC;
        if ("Sn".equals(s)) return TIN;
        if ("Pb".equals(s)) return LEAD;
        if ("Ag".equals(s)) return SILVER;
        if ("Au".equals(s)) return GOLD;
        if ("Mn".equals(s)) return MANGANESE;
        if ("Cr".equals(s)) return CHROMIUM;
        if ("Ni".equals(s)) return NICKEL;
        if ("Co".equals(s)) return COBALT;
        if ("Ti".equals(s)) return TITANIUM;
        if ("W".equals(s)) return TUNGSTEN;
        if ("Os".equals(s)) return OSMIUM;
        if ("U".equals(s)) return URANIUM;
        if ("Pu".equals(s)) return PLUTONIUM;
        if ("Po".equals(s)) return POLONIUM;
        if ("Ar".equals(s)) return ARGON;
        if ("Kr".equals(s)) return KRYPTON;
        if ("He".equals(s)) return HELIUM;
        if ("Ne".equals(s)) return NEON;
        if ("Xe".equals(s)) return XENON;
        return new AtomElement(s, 0xFFFF88FF, 0.35f);
    }
}
