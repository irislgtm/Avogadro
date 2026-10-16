package com.trifectumsoftware.avogadro;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_TOOLTIPS = BUILDER.define("enableTooltips", true);

    public static final ModConfigSpec.IntValue TOOLTIP_SIZE = BUILDER.defineInRange("tooltipSize", 64, 16, 128);

    public static final ModConfigSpec.DoubleValue SPIN_SECONDS = BUILDER.defineInRange("spinSeconds", 6.0, 0.5, 60.0);

    public static final ModConfigSpec.BooleanValue JEI_CATEGORY = BUILDER.define("jeiCategory", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
