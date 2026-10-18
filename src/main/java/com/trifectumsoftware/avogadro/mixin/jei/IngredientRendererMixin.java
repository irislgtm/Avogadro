package com.trifectumsoftware.avogadro.mixin.jei;

import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltip;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = IIngredientRenderer.class, remap = false)
public abstract class IngredientRendererMixin {
    @Inject(method = "getTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Ljava/lang/Object;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"), remap = false)
    private void avogadro$appendMolecule(ITooltipBuilder builder, Object ingredient, TooltipFlag flag, CallbackInfo ci) {
        if (!(ingredient instanceof FluidStack fluid) || fluid.isEmpty()) return;
        ResourceLocation hit = MoleculeStore.lookup(fluid);
        if (hit != null && MoleculeStore.get(hit) != null) builder.add(new MoleculeTooltip(hit));
    }
}
