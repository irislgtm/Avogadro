package com.trifectumsoftware.avogadro.compat.waila;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.trifectumsoftware.avogadro.Config;
import com.trifectumsoftware.avogadro.molecule.MoleculeDef;
import com.trifectumsoftware.avogadro.molecule.MoleculeStore;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;

public final class WailaMoleculeProvider implements IWailaDataProvider {

    private static final String RENDERER_ID = "avogadro:molecule";
    private static boolean registered;

    public static void load(IWailaRegistrar registrar) {
        if (registered) return;
        registered = true;
        registrar.registerBodyProvider(new WailaMoleculeProvider(), Block.class);
        registrar.registerTooltipRenderer(RENDERER_ID, new WailaMoleculeRenderer());
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        if (!Config.enableTooltips || !Config.wailaCompat) return currenttip;

        String hit = MoleculeStore.lookup(stack(accessor, itemStack));
        if (hit == null) return currenttip;
        MoleculeDef def = MoleculeStore.get(hit);
        if (def == null) return currenttip;

        String formula = def.formula();
        if (formula != null && !formula.isEmpty()) {
            currenttip.add(SpecialChars.WHITE + formula);
        }
        currenttip.add(SpecialChars.getRenderString(RENDERER_ID, hit));
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, net.minecraft.tileentity.TileEntity te, NBTTagCompound tag,
        World world, int x, int y, int z) {
        return tag;
    }

    private static ItemStack stack(IWailaDataAccessor accessor, ItemStack fallback) {
        if (fallback != null) return fallback;
        Block block = accessor.getBlock();
        if (block == null) return null;
        Item item = Item.getItemFromBlock(block);
        if (item == null) return null;
        return new ItemStack(item, 1, accessor.getMetadata());
    }
}
