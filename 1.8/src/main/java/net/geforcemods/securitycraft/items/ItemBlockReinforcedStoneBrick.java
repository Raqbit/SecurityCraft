package net.geforcemods.securitycraft.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockReinforcedStoneBrick extends ItemBlockTinted
{
	public ItemBlockReinforcedStoneBrick(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	public int getMetadata(int meta){
		return meta;
	}
	
	public String getUnlocalizedName(ItemStack stack)
	{
		if(stack.getItemDamage() == 0)
			return this.getUnlocalizedName() + "_default";
		else if(stack.getItemDamage() == 1)
			return this.getUnlocalizedName() + "_mossy";
		else if(stack.getItemDamage() == 2)
			return this.getUnlocalizedName() + "_cracked";
		else if(stack.getItemDamage() == 3)
			return this.getUnlocalizedName() + "_chiseled";
		else
			return this.getUnlocalizedName();
	}
}
