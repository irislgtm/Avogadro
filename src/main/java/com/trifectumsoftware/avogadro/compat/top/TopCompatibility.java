package com.trifectumsoftware.avogadro.compat.top;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import io.netty.buffer.ByteBuf;

public final class TopCompatibility {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String PROBE = "mcjty.theoneprobe.api.ITheOneProbe";
    private static final String PROVIDER = "mcjty.theoneprobe.api.IProbeInfoProvider";
    private static final String INFO = "mcjty.theoneprobe.api.IProbeInfo";
    private static final String HIT_DATA = "mcjty.theoneprobe.api.IProbeHitData";
    private static final String ELEMENT = "mcjty.theoneprobe.api.IElement";
    private static final String FACTORY = "mcjty.theoneprobe.api.IElementFactory";
    private static final String BLOCK_STATE = "net.minecraft.block.state.IBlockState";

    private static boolean registered;
    private static Object probe;
    private static int elementId = -1;

    private TopCompatibility() {}

    public static void register() {
        if (registered) return;
        registered = true;
        if (!Loader.isModLoaded("theoneprobe") && !Loader.isModLoaded("theonesmeagle")) return;

        String function = modernApi() ? "com.trifectumsoftware.avogadro.compat.top.TopCompatibility$GetTheOneProbeJava"
            : "com.trifectumsoftware.avogadro.compat.top.TopCompatibility$GetTheOneProbeGuava";
        if (Loader.isModLoaded("theoneprobe")) {
            sendFunctionMessage("theoneprobe", function);
        }
        if (Loader.isModLoaded("theonesmeagle")) {
            sendFunctionMessage("theonesmeagle", function);
        }
    }

    private static void sendFunctionMessage(String modid, String function) {

        try {
            FMLInterModComms.class.getMethod("sendFunctionMessage", String.class, String.class, String.class)
                .invoke(null, modid, "getTheOneProbe", function);
        } catch (Throwable ignored) {}
    }

    private static boolean modernApi() {
        try {
            Class.forName("net.minecraft.util.math.BlockPos");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void attach(Object theOneProbe) {
        probe = theOneProbe;
        try {
            registerElementFactory();
            Class<?> providerClass = Class.forName(PROVIDER);
            Object provider = Proxy.newProxyInstance(
                TopCompatibility.class.getClassLoader(),
                new Class<?>[] { providerClass },
                new ProviderHandler());
            Class.forName(PROBE)
                .getMethod("registerProvider", providerClass)
                .invoke(theOneProbe, provider);
            Avogadro.LOG.info("Registered The One Probe molecule provider");
        } catch (Throwable bad) {
            Avogadro.LOG.warn("Could not register The One Probe molecule provider", bad);
        }
    }

    private static void registerElementFactory() throws Exception {
        Class<?> factoryClass = Class.forName(FACTORY);
        Object factory = Proxy.newProxyInstance(
            TopCompatibility.class.getClassLoader(),
            new Class<?>[] { factoryClass },
            new FactoryHandler());
        elementId = ((Integer) Class.forName(PROBE)
            .getMethod("registerElementFactory", factoryClass)
            .invoke(probe, factory)).intValue();
    }

    private static final class ProviderHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("getID".equals(name)) return "avogadro:molecules";
            if ("addProbeInfo".equals(name)) {
                addProbeInfo(args);
                return null;
            }
            return null;
        }
    }

    private static void addProbeInfo(Object[] args) {
        Object info = find(args, INFO);
        if (info == null) return;

        ItemStack stack = pick(args);
        if (stack == null) return;
        String hit = MoleculeStore.lookup(stack);
        if (hit == null) return;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null) return;

        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {
            try {
                Class.forName(INFO)
                    .getMethod("text", String.class)
                    .invoke(info, formula);
            } catch (Throwable ignored) {}
        }
        if (elementId < 0) return;
        try {
            Object element = newElement(hit);
            Class.forName(INFO)
                .getMethod("element", Class.forName(ELEMENT))
                .invoke(info, element);
        } catch (Throwable ignored) {}
    }

    private static ItemStack pick(Object[] args) {
        Object hit = find(args, HIT_DATA);
        if (hit != null) {
            try {
                Object stack = Class.forName(HIT_DATA)
                    .getMethod("getPickBlock")
                    .invoke(hit);
                if (stack instanceof ItemStack) return (ItemStack) stack;
            } catch (Throwable ignored) {}
        }
        Object state = find(args, BLOCK_STATE);
        if (state == null) return null;
        try {
            Class<?> stateClass = Class.forName(BLOCK_STATE);
            Block block = (Block) stateClass.getMethod("getBlock")
                .invoke(state);
            Item item = Item.getItemFromBlock(block);
            if (item == null) return null;
            int meta = ((Integer) Block.class.getMethod("getMetaFromState", stateClass)
                .invoke(block, state)).intValue();
            return new ItemStack(item, 1, meta);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object find(Object[] args, String className) {
        if (args == null) return null;
        try {
            Class<?> wanted = Class.forName(className);
            for (Object arg : args) {
                if (wanted.isInstance(arg)) return arg;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Object newElement(String molecule) {
        return Proxy.newProxyInstance(
            TopCompatibility.class.getClassLoader(),
            new Class<?>[] { classOrNull(ELEMENT) },
            new ElementHandler(molecule));
    }

    private static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable bad) {
            throw new IllegalStateException(name, bad);
        }
    }

    private static final class FactoryHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("createElement".equals(method.getName()) && args != null && args.length == 1) {
                ByteBuf buf = (ByteBuf) args[0];
                int length = buf.readInt();
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                return newElement(new String(bytes, UTF8));
            }
            return null;
        }
    }

    private static final class ElementHandler implements InvocationHandler {

        private final String molecule;

        ElementHandler(String molecule) {
            this.molecule = molecule;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getID".equals(name)) return Integer.valueOf(elementId);
            if ("getWidth".equals(name) || "getHeight".equals(name)) return Integer.valueOf(size());
            if ("toBytes".equals(name) && args != null && args.length == 1) {
                ByteBuf buf = (ByteBuf) args[0];
                byte[] bytes = molecule.getBytes(UTF8);
                buf.writeInt(bytes.length);
                buf.writeBytes(bytes);
                return null;
            }
            if ("render".equals(name) && args != null && args.length == 2) {
                renderMolecule(((Integer) args[0]).intValue(), ((Integer) args[1]).intValue());
                return null;
            }
            return null;
        }

        private void renderMolecule(int x, int y) {
            MoleculeDef def = MoleculeStore.get(molecule);
            if (def == null) return;
            try {
                Class<?> renderer = Class.forName("com.trifectumsoftware.avogadro.client.render.MoleculeRenderer");
                renderer
                    .getMethod(
                        "render",
                        com.trifectumsoftware.avogadro.molecule.MoleculeStructure.class,
                        float.class,
                        float.class,
                        float.class,
                        float.class)
                    .invoke(null, def.structure(), Float.valueOf(x), Float.valueOf(y), Float.valueOf(size()), angle());
            } catch (Throwable ignored) {}
        }
    }

    private static int size() {
        int size = Config.tooltipSize;
        if (size < 16) size = 16;
        if (size > 128) size = 128;
        return size;
    }

    private static float angle() {
        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        return (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);
    }

    public static final class GetTheOneProbeGuava implements com.google.common.base.Function<Object, Void> {

        @Override
        public Void apply(Object input) {
            attach(input);
            return null;
        }
    }

    public static final class GetTheOneProbeJava implements java.util.function.Function<Object, Void> {

        @Override
        public Void apply(Object input) {
            attach(input);
            return null;
        }
    }
}
