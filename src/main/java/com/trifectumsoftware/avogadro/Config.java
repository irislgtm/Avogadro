package com.trifectumsoftware.avogadro;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_TOOLTIPS = BUILDER.define("enableTooltips", true);

    public static final ForgeConfigSpec.IntValue TOOLTIP_SIZE = BUILDER.defineInRange("tooltipSize", 64, 16, 128);

    public static final ForgeConfigSpec.DoubleValue SPIN_SECONDS = BUILDER.defineInRange("spinSeconds", 6.0, 0.5, 60.0);

    public static final ForgeConfigSpec.BooleanValue JEI_CATEGORY = BUILDER.define("jeiCategory", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();
}
