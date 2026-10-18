package com.trifectumsoftware.avogadro;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean enableTooltips = true;
    public static int tooltipSize = 64;
    public static float spinSeconds = 6.0f;
    public static boolean neiCategory = true;
    public static boolean cubicAtoms = false;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        enableTooltips = configuration.getBoolean(
            "enableTooltips",
            Configuration.CATEGORY_GENERAL,
            enableTooltips,
            "Render a molecule model tooltip for items that map to a molecule.");

        tooltipSize = configuration.getInt(
            "tooltipSize",
            Configuration.CATEGORY_GENERAL,
            tooltipSize,
            16,
            128,
            "Size in pixels of the rendered molecule model.");

        spinSeconds = configuration.getFloat(
            "spinSeconds",
            Configuration.CATEGORY_GENERAL,
            spinSeconds,
            0.5f,
            60.0f,
            "Seconds per full rotation of the molecule model.");

        neiCategory = configuration
            .getBoolean("neiCategory", Configuration.CATEGORY_GENERAL, neiCategory, "Add a Molecules category to NEI.");

        cubicAtoms = configuration.getBoolean(
            "cubicAtoms",
            Configuration.CATEGORY_GENERAL,
            cubicAtoms,
            "Draw atoms as cubes instead of spheres.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
