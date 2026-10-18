package com.trifectumsoftware.avogadro.compat.nei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.client.render.MoleculeRenderer;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

/**
 * NEI category listing every configured molecule. Each "recipe" draws the spinning model plus the formula, and uses
 * one of the molecule's mapped items as the selectable result so that NEI can associate the entry with the item
 * panel and the R/U keybinds.
 */
public class MoleculeRecipeHandler extends TemplateRecipeHandler {

    public static final String ID = "avogadro:molecules";

    private static final int MODEL_X = 4;
    private static final int MODEL_Y = 3;
    private static final int MODEL_SIZE = 58;
    private static final int RESULT_X = 150;
    private static final int RESULT_Y = 46;

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
            mc.fontRenderer.drawStringWithShadow(formula, MODEL_X + MODEL_SIZE + 8, MODEL_Y + 3, 0xFFFFFF);
        }
        mc.fontRenderer.drawStringWithShadow(def.id(), MODEL_X + MODEL_SIZE + 8, MODEL_Y + 15, 0xA0A0A0);

        List<PositionedStack> ingredients = arecipes.get(recipe)
            .getIngredients();
        int x = MODEL_X + MODEL_SIZE + 8;
        for (int i = 0; i < ingredients.size() && i < 4; i++) {
            PositionedStack stack = ingredients.get(i);
            mc.fontRenderer.drawStringWithShadow(shortName(stack), x, MODEL_Y + 30 + i * 10, 0x808080);
        }
    }

    private static String shortName(PositionedStack stack) {
        if (stack == null || stack.item == null) return "";
        Item item = stack.item.getItem();
        String name = String.valueOf(Item.itemRegistry.getNameForObject(item));
        return name;
    }

    public class CachedMolecule extends CachedRecipe {

        final MoleculeDef def;
        private final PositionedStack result;
        private final List<PositionedStack> ingredients;

        CachedMolecule(MoleculeDef def, ItemStack result, List<PositionedStack> ingredients) {
            this.def = def;
            this.result = result == null ? null : new PositionedStack(result, RESULT_X, RESULT_Y);
            this.ingredients = ingredients;
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return ingredients;
        }
    }

    private void add(MoleculeDef def) {
        List<ItemStack> stacks = mapped(def);
        if (stacks.isEmpty()) return;

        List<PositionedStack> ingredients = new java.util.ArrayList<PositionedStack>();
        int x = MODEL_X + MODEL_SIZE + 8;
        for (int i = 1; i < stacks.size() && i <= 4; i++) {
            ingredients.add(new PositionedStack(stacks.get(i), x, MODEL_Y + 26 + (i - 1) * 10));
        }
        arecipes.add(new CachedMolecule(def, stacks.get(0), ingredients));
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
