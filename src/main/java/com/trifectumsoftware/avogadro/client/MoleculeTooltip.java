package com.trifectumsoftware.avogadro.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record MoleculeTooltip(ResourceLocation molecule) implements TooltipComponent {
}
