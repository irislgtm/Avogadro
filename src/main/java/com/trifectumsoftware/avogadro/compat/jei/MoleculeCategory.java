package com.trifectumsoftware.avogadro.compat.jei;

import com.trifectumsoftware.avogadro.Avogadro;
import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoleculeCategory implements IRecipeCategory<MoleculeCategory.Entry> {
    public record Entry(ResourceLocation id, MoleculeDef def) {
    }

    public static final RecipeType<Entry> TYPE = RecipeType.create(Avogadro.MODID, "molecules", Entry.class);

    private final IDrawable background;
    private final IDrawable icon;

    public MoleculeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(180, 96);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.WATER_BUCKET));
    }

    @Override
    public RecipeType<Entry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.avogadro.molecules");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 96;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = new ArrayList<>();
        for (String s : recipe.def().items()) {
            ResourceLocation k = ResourceLocation.tryParse(s);
            if (k == null) continue;
            if (BuiltInRegistries.ITEM.containsKey(k)) {
                inputs.add(new ItemStack(BuiltInRegistries.ITEM.get(k)));
                if (inputs.size() >= 4) break;
            }
        }
        if (!inputs.isEmpty()) builder.addSlot(RecipeIngredientRole.INPUT, 4, 4).addItemStacks(inputs);
    }

    @Override
    public void draw(Entry recipe, IRecipeSlotsView view, GuiGraphics gfx, double mouseX, double mouseY) {
        float angle = (float) ((System.currentTimeMillis() % 6000L) / 6000.0 * 360.0);
        MoleculeRenderer.render(recipe.def().structure(), gfx, 100, 12, 72, angle);
        var font = Minecraft.getInstance().font;
        gfx.drawString(font, recipe.def().formula(), 8, 28, 0xFF202020, false);
        String id = recipe.id().toString();
        String shown = font.plainSubstrByWidth(id, 80);
        if (!shown.equals(id)) shown = shown + "...";
        gfx.drawString(font, shown, 8, 40, 0xFF404040, false);
    }

    public static List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (MoleculeDef def : MoleculeStore.all()) out.add(new Entry(def.id(), def));
        out.sort(Comparator.comparing(e -> e.id().toString()));
        return out;
    }
}
