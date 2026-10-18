package com.trifectumsoftware.avogadro.molecule;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.trifectumsoftware.avogadro.Avogadro;

/**
 * Loads every molecule definition from {@code config/avogadro/molecules/*.json}. This is the only data source on
 * 1.7.10 - there is no datapack or server sync layer. If the directory does not exist yet it is created and the
 * bundled defaults are written into it.
 */
public final class MoleculeConfigLoader {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String[] DEFAULTS = { "benzene.json", "carbon.json", "carbon_dioxide.json", "ethene.json",
        "methane.json", "toluene.json", "water.json" };

    private MoleculeConfigLoader() {}

    public static File moleculesDir(File configDir) {
        return new File(new File(configDir, Avogadro.MODID), "molecules");
    }

    public static void load(File configDir) {
        File dir = moleculesDir(configDir);
        if (!dir.isDirectory()) {
            if (dir.mkdirs()) {
                writeDefaults(dir);
            } else {
                Avogadro.LOG.warn("Could not create molecule config directory {}", dir);
            }
        }

        Map<String, JsonElement> jsons = new LinkedHashMap<String, JsonElement>();
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {

                @Override
                public int compare(File a, File b) {
                    return a.getName()
                        .compareTo(b.getName());
                }
            });
            for (File file : files) {
                if (!file.isFile() || !file.getName()
                    .endsWith(".json")) continue;
                Reader reader = null;
                try {
                    String name = file.getName();
                    reader = new InputStreamReader(new FileInputStream(file), UTF8);
                    jsons.put(
                        Avogadro.MODID + ":" + name.substring(0, name.length() - 5),
                        new JsonParser().parse(reader));
                } catch (Exception skipped) {
                    Avogadro.LOG.warn("Skipping unreadable molecule file {}", file);
                } finally {
                    close(reader);
                }
            }
        }

        Map<String, MoleculeDef> defs = MoleculeStore.parseAll(jsons);
        MoleculeStore.setAll(defs);
        Avogadro.LOG.info("Loaded {} molecule definition(s) from {}", Integer.valueOf(defs.size()), dir);
    }

    private static void writeDefaults(File dir) {
        for (String name : DEFAULTS) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = MoleculeConfigLoader.class.getResourceAsStream("/assets/avogadro/molecules/" + name);
                if (in == null) continue;
                out = new FileOutputStream(new File(dir, name));
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            } catch (Exception skipped) {
                Avogadro.LOG.warn("Could not write default molecule {}", name);
            } finally {
                close(in);
                close(out);
            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignored) {}
    }
}
