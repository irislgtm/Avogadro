package com.trifectumsoftware.avogadro.compat.nei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class MoleculeRecipeHandler extends TemplateRecipeHandler {

    public static final String ID = "avogadro:molecules";

    private static final int MODEL_X = 8;
    private static final int MODEL_Y = 8;
    private static final int MODEL_SIZE = 58;
    private static final int TEXT_X = MODEL_X + MODEL_SIZE + 8;
    private static final int TEXT_Y = MODEL_Y;
    private static final int SLOT_X = TEXT_X;
    private static final int SLOT_Y = TEXT_Y + 32;
    private static final int CYCLE_TICKS = 40;
    private static final int TEXT_WIDTH = 166 - TEXT_X - 4;

    @Override
    public String getHandlerId() {
        return ID;
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("avogadro.nei.molecules");
    }

    @Override
    public String getGuiTexture() {
        return "/gui/crafting.png";
    }

    @Override
    public void drawBackground(int recipe) {}

    @Override
    public void drawForeground(int recipe) {
        MoleculeDef def = ((CachedMolecule) arecipes.get(recipe)).def;

        long period = (long) (Config.spinSeconds * 1000.0f);
        if (period < 1L) period = 1L;
        float angle = (float) ((System.currentTimeMillis() % period) / (double) period * 360.0);
        MoleculeRenderer.render(def.structure(), MODEL_X, MODEL_Y, MODEL_SIZE, angle);

        Minecraft mc = Minecraft.getMinecraft();
        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {
            mc.fontRenderer.drawString(trim(mc.fontRenderer, formula, TEXT_WIDTH), TEXT_X, TEXT_Y, 0x000000);
        }
        mc.fontRenderer
            .drawString(trim(mc.fontRenderer, displayId(def.id()), TEXT_WIDTH), TEXT_X, TEXT_Y + 12, 0x000000);
    }

    private static String displayId(String id) {
        if (id == null) return "";
        int colon = id.indexOf(':');
        return colon >= 0 && colon + 1 < id.length() ? id.substring(colon + 1) : id;
    }

    private static String trim(FontRenderer font, String text, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (font.getStringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int available = maxWidth - font.getStringWidth(ellipsis);
        if (available <= 0) return font.trimStringToWidth(text, maxWidth);
        return font.trimStringToWidth(text, available) + ellipsis;
    }

    public class CachedMolecule extends CachedRecipe {

        final MoleculeDef def;
        private final List<PositionedStack> slots;

        CachedMolecule(MoleculeDef def, List<PositionedStack> slots) {
            this.def = def;
            this.slots = slots;
        }

        @Override
        public PositionedStack getResult() {
            if (slots.isEmpty()) return null;
            return slots.get((cycleticks / CYCLE_TICKS) % slots.size());
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return java.util.Collections.emptyList();
        }
    }

    private void add(MoleculeDef def) {
        List<ItemStack> stacks = mapped(def);
        List<PositionedStack> slots = new java.util.ArrayList<PositionedStack>(stacks.size());
        for (ItemStack stack : stacks) {
            slots.add(new PositionedStack(stack, SLOT_X, SLOT_Y));
        }
        arecipes.add(new CachedMolecule(def, slots));
    }

    private void addAll() {
        for (MoleculeDef def : MoleculeStore.all()) {
            add(def);
        }
    }

    private void addFor(ItemStack stack) {
        if (stack == null) return;
        String hit = MoleculeStore.lookup(stack);
        if (hit == null) return;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def != null) add(def);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (ID.equals(outputId) || "item".equals(outputId)) {
            addAll();
            return;
        }
        super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        addFor(result);
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (ID.equals(inputId) || "item".equals(inputId)) {
            addAll();
            return;
        }
        super.loadUsageRecipes(inputId, ingredients);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        addFor(ingredient);
    }

    private static List<ItemStack> mapped(MoleculeDef def) {
        List<ItemStack> out = new java.util.ArrayList<ItemStack>();
        for (String name : def.items()) {
            ItemStack stack = resolve(name);
            if (stack != null) out.add(stack);
        }
        return out;
    }

    private static ItemStack resolve(String name) {
        try {
            Object object = Item.itemRegistry.getObject(name);
            if (!(object instanceof Item)) return null;
            return new ItemStack((Item) object, 1, 0);
        } catch (Exception bad) {
            return null;
        }
    }
}
