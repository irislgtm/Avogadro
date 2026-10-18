package com.trifectumsoftware.avogadro.mixin;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.trifectumsoftware.avogadro.client.tooltip.MoleculeTooltipOverlay;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {

    // func_146283_a == GuiScreen.drawHoveringText(List, int, int). MCP ships no name for this member, so the SRG name
    // is the one present in both the dev workspace and the reobfuscated jar.
    @Inject(method = "func_146283_a(Ljava/util/List;II)V", at = @At("TAIL"))
    private void avogadro$drawMoleculeModel(List<?> lines, int mouseX, int mouseY, CallbackInfo ci) {
        MoleculeTooltipOverlay.draw(lines, mouseX, mouseY);
    }
}
