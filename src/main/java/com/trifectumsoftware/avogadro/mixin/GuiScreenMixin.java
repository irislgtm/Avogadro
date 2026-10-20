package com.trifectumsoftware.avogadro.mixin;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltipOverlay;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {

    @Inject(
        method = "drawHoveringText(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V",
        at = @At("TAIL"),
        remap = false)
    private void avogadro$drawMoleculeModel(List<?> lines, int mouseX, int mouseY, FontRenderer font, CallbackInfo ci) {
        MoleculeTooltipOverlay.draw(lines, mouseX, mouseY);
    }
}
