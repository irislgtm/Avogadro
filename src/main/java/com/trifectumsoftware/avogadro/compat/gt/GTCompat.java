package com.trifectumsoftware.avogadro.compat.gt;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.ModList;

public final class GTCompat {
    public static final String MODID = "gtceu";

    private GTCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    public static ResourceLocation materialId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        MaterialStack ms = ChemicalHelper.getMaterialStack(stack);
        if (ms.isEmpty() || ms.material().isNull()) return null;
        return ms.material().getResourceLocation();
    }

    public static ResourceLocation materialId(Fluid fluid) {
        Material material = ChemicalHelper.getMaterial(fluid);
        if (material == null || material.isNull()) return null;
        return material.getResourceLocation();
    }
}
