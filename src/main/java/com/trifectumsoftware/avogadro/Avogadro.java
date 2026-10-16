package com.trifectumsoftware.avogadro;

import com.trifectumsoftware.avogadro.molecule.MoleculeSync;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(Avogadro.MODID)
public class Avogadro {
    public static final String MODID = "avogadro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Avogadro(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @SuppressWarnings("removal")
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void payloads(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(MoleculeSync.TYPE, MoleculeSync.CODEC, MoleculeSync::handle);
        }
    }
}
