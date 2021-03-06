package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blocks.BlockKeypadFurnace;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityKeypadFurnace extends TileEntityOwnable implements ISidedInventory, IPasswordProtected {
	
	private static final int[] slotsTop = new int[] {0};
    private static final int[] slotsBottom = new int[] {2, 1};
    private static final int[] slotsSides = new int[] {1};
    public NonNullList<ItemStack> furnaceItemStacks = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    public int cookTime;
    public int totalCookTime;
    private String furnaceCustomName;
    private String passcode;
    
    @Override
	public int getSizeInventory()
    {
        return this.furnaceItemStacks.size();
    }

    @Override
	public ItemStack getStackInSlot(int index)
    {
        return this.furnaceItemStacks.get(index);
    }

    @Override
	public ItemStack decrStackSize(int index, int count)
    {
        if (!this.furnaceItemStacks.get(index).isEmpty())
        {
            ItemStack itemstack;

            if (this.furnaceItemStacks.get(index).getCount() <= count)
            {
                itemstack = this.furnaceItemStacks.get(index);
                this.furnaceItemStacks.set(index, ItemStack.EMPTY);
                return itemstack;
            }
            else
            {
                itemstack = this.furnaceItemStacks.get(index).splitStack(count);

                if (this.furnaceItemStacks.get(index).getCount() == 0)
                {
                    this.furnaceItemStacks.set(index, ItemStack.EMPTY);
                }

                return itemstack;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
	public ItemStack removeStackFromSlot(int index)
    {
        if (!this.furnaceItemStacks.get(index).isEmpty())
        {
            ItemStack itemstack = this.furnaceItemStacks.get(index);
            this.furnaceItemStacks.set(index, ItemStack.EMPTY);
            return itemstack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
	public void setInventorySlotContents(int index, ItemStack stack)
    {
        boolean flag = !stack.isEmpty() && stack.isItemEqual(this.furnaceItemStacks.get(index)) && ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks.get(index));
        this.furnaceItemStacks.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        if (index == 0 && !flag)
        {
            this.totalCookTime = this.func_174904_a(stack);
            this.cookTime = 0;
            this.markDirty();
        }
    }

    @Override
	public String getName()
    {
        return this.hasCustomName() ? this.furnaceCustomName : "container.furnace";
    }

    @Override
	public boolean hasCustomName()
    {
        return this.furnaceCustomName != null && this.furnaceCustomName.length() > 0;
    }

    public void setCustomInventoryName(String p_145951_1_)
    {
        this.furnaceCustomName = p_145951_1_;
    }

    @Override
	public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.furnaceItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < this.furnaceItemStacks.size())
            {
                this.furnaceItemStacks.set(b0, new ItemStack(nbttagcompound1));
            }
        }

        this.furnaceBurnTime = compound.getShort("BurnTime");
        this.cookTime = compound.getShort("CookTime");
        this.totalCookTime = compound.getShort("CookTimeTotal");
        this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(1));
        this.passcode = compound.getString("passcode");

        if (compound.hasKey("CustomName", 8))
        {
            this.furnaceCustomName = compound.getString("CustomName");
        }
    }

    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setShort("BurnTime", (short)this.furnaceBurnTime);
        compound.setShort("CookTime", (short)this.cookTime);
        compound.setShort("CookTimeTotal", (short)this.totalCookTime);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.furnaceItemStacks.size(); ++i)
        {
            if (!this.furnaceItemStacks.get(i).isEmpty())
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.furnaceItemStacks.get(i).writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
        
        if(this.passcode != null && !this.passcode.isEmpty()){
        	compound.setString("passcode", passcode);
        }

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.furnaceCustomName);
        }
        
        return compound;
    }

    @Override
	public int getInventoryStackLimit()
    {
        return 64;
    }
    
    /**
     * Returns an integer between 0 and the passed value representing how close the current item is to being completely
     * cooked
     */
    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int p_145953_1_)
    {
        return this.cookTime * p_145953_1_ / 200;
    }

    /**
     * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
     * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
     */
    @SideOnly(Side.CLIENT)
    public int getBurnTimeRemainingScaled(int p_145955_1_)
    {
        if (this.currentItemBurnTime == 0)
        {
            this.currentItemBurnTime = 200;
        }

        return this.furnaceBurnTime * p_145955_1_ / this.currentItemBurnTime;
    }

    public boolean isBurning()
    {
        return this.furnaceBurnTime > 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isBurning(IInventory p_174903_0_)
    {
        return p_174903_0_.getField(0) > 0;
    }

    @Override
	public void update()
    {
        boolean flag = this.isBurning();
        boolean flag1 = false;
        
//        if(this.isBurning() && !((Boolean) Utils.getBlockProperty(getWorld(), getPos(), BlockKeypadFurnace.COOKING)).booleanValue()){
//        	Utils.setBlockProperty(getWorld(), getPos(), BlockKeypadFurnace.COOKING, true);
//        }else if(!this.isBurning() && ((Boolean) Utils.getBlockProperty(getWorld(), getPos(), BlockKeypadFurnace.COOKING)).booleanValue()){
//        	Utils.setBlockProperty(getWorld(), getPos(), BlockKeypadFurnace.COOKING, false);
//        }

        if (this.isBurning())
        {
            --this.furnaceBurnTime;
        }

        if (!this.world.isRemote)
        {
            if (!this.isBurning() && (this.furnaceItemStacks.get(1).isEmpty() || this.furnaceItemStacks.get(0).isEmpty()))
            {
                if (!this.isBurning() && this.cookTime > 0)
                {
                    this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
                }
            }
            else
            {
                if (!this.isBurning() && this.canSmelt())
                {
                    this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks.get(1));

                    if (this.isBurning())
                    {
                        flag1 = true;

                        if (!this.furnaceItemStacks.get(1).isEmpty())
                        {
                            this.furnaceItemStacks.get(1).shrink(1);

                            if (this.furnaceItemStacks.get(1).getCount() == 0)
                            {
                                this.furnaceItemStacks.set(1, furnaceItemStacks.get(1).getItem().getContainerItem(furnaceItemStacks.get(1)));
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canSmelt())
                {
                    ++this.cookTime;

                    if (this.cookTime == this.totalCookTime)
                    {
                        this.cookTime = 0;
                        this.totalCookTime = this.func_174904_a(this.furnaceItemStacks.get(0));
                        this.smeltItem();
                        flag1 = true;
                    }
                }
                else
                {
                    this.cookTime = 0;
                }
            }

            if (flag != this.isBurning())
            {
                flag1 = true;            
            }
        }

        if (flag1)
        {
            this.markDirty();
        }
    }

    public int func_174904_a(ItemStack p_174904_1_)
    {
        return 200;
    }

    private boolean canSmelt()
    {
        if (this.furnaceItemStacks.get(0).isEmpty())
        {
            return false;
        }
        else
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks.get(0));
            if (itemstack.isEmpty()) return false;
            if (this.furnaceItemStacks.get(2).isEmpty()) return true;
            if (!this.furnaceItemStacks.get(2).isItemEqual(itemstack)) return false;
            int result = furnaceItemStacks.get(2).getCount() + itemstack.getCount();
            return result <= getInventoryStackLimit() && result <= this.furnaceItemStacks.get(2).getMaxStackSize(); //Forge BugFix: Make it respect stack sizes properly.
        }
    }

    public void smeltItem()
    {
        if (this.canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks.get(0));

            if (this.furnaceItemStacks.get(2).isEmpty())
            {
                this.furnaceItemStacks.set(2, itemstack.copy());
            }
            else if (this.furnaceItemStacks.get(2).getItem() == itemstack.getItem())
            {
                this.furnaceItemStacks.get(2).grow(itemstack.getCount()); // Forge BugFix: Results may have multiple items
            }

            if (this.furnaceItemStacks.get(0).getItem() == Item.getItemFromBlock(Blocks.SPONGE) && this.furnaceItemStacks.get(0).getMetadata() == 1 && !this.furnaceItemStacks.get(1).isEmpty() && this.furnaceItemStacks.get(1).getItem() == Items.BUCKET)
            {
                this.furnaceItemStacks.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            this.furnaceItemStacks.get(0).shrink(1);

            if (this.furnaceItemStacks.get(0).getCount() <= 0)
            {
                this.furnaceItemStacks.set(0, ItemStack.EMPTY);
            }
        }
    }

    public static int getItemBurnTime(ItemStack p_145952_0_)
    {
        if (p_145952_0_.isEmpty())
        {
            return 0;
        }
        else
        {
            Item item = p_145952_0_.getItem();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR)
            {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.WOODEN_SLAB)
                {
                    return 150;
                }

                if (block.getMaterial(block.getDefaultState()) == Material.WOOD)
                {
                    return 300;
                }

                if (block == Blocks.COAL_BLOCK)
                {
                    return 16000;
                }
            }

            if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemHoe && ((ItemHoe)item).getMaterialName().equals("WOOD")) return 200;
            if (item == Items.STICK) return 100;
            if (item == Items.COAL) return 1600;
            if (item == Items.LAVA_BUCKET) return 20000;
            if (item == Item.getItemFromBlock(Blocks.SAPLING)) return 100;
            if (item == Items.BLAZE_ROD) return 2400;
            return net.minecraftforge.fml.common.registry.GameRegistry.getFuelValue(p_145952_0_);
        }
    }

    public static boolean isItemFuel(ItemStack p_145954_0_)
    {
        return getItemBurnTime(p_145954_0_) > 0;
    }

    @Override
	public boolean isUsableByPlayer(EntityPlayer player)
    {
        return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
	public void openInventory(EntityPlayer player) {}

    @Override
	public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isEmpty()
    {
    	for(ItemStack stack : furnaceItemStacks)
    	{
    		if(!stack.isEmpty())
    			return false;
    	}
    	
    	return true;
    }
    
    @Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index == 2 ? false : (index != 1 ? true : isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack));
    }

    @Override
	public int[] getSlotsForFace(EnumFacing side)
    {
        return side == EnumFacing.DOWN ? slotsBottom : (side == EnumFacing.UP ? slotsTop : slotsSides);
    }

    @Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        if (direction == EnumFacing.DOWN && index == 1)
        {
            Item item = stack.getItem();

            if (item != Items.WATER_BUCKET && item != Items.BUCKET)
            {
                return false;
            }
        }

        return true;
    }

    public String getGuiID()
    {
        return "minecraft:furnace";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerFurnace(playerInventory, this);
    }

    @Override
	public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return this.furnaceBurnTime;
            case 1:
                return this.currentItemBurnTime;
            case 2:
                return this.cookTime;
            case 3:
                return this.totalCookTime;
            default:
                return 0;
        }
    }

    @Override
	public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                this.furnaceBurnTime = value;
                break;
            case 1:
                this.currentItemBurnTime = value;
                break;
            case 2:
                this.cookTime = value;
                break;
            case 3:
                this.totalCookTime = value;
        }
    }

    @Override
	public int getFieldCount()
    {
        return 4;
    }

    @Override
	public void clear()
    {
        for (int i = 0; i < this.furnaceItemStacks.size(); ++i)
        {
            this.furnaceItemStacks.set(i, ItemStack.EMPTY);
        }
    }

	@Override
	public ITextComponent getDisplayName() {
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]);
	}
	
	@Override
	public void activate(EntityPlayer player) {
		if(!world.isRemote && BlockUtils.getBlock(getWorld(), getPos()) instanceof BlockKeypadFurnace){
    		BlockKeypadFurnace.activate(world, pos, player);
    	}
	}
	
	@Override
	public void openPasswordGUI(EntityPlayer player) {
		if(getPassword() != null) {
			player.openGui(mod_SecurityCraft.instance, GuiHandler.INSERT_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
		}
		else {
			player.openGui(mod_SecurityCraft.instance, GuiHandler.SETUP_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
		}		
	}
	
	@Override
	public boolean onCodebreakerUsed(IBlockState blockState, EntityPlayer player, boolean isCodebreakerDisabled) {
		if(isCodebreakerDisabled) {
			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("tile.keypadFurnace.name"), ClientUtils.localize("messages.codebreakerDisabled"), TextFormatting.RED);
		}
		else {	
			activate(player);
			return true;
		}
		
		return false;
	}

	@Override
	public String getPassword() {
		return (this.passcode != null && !this.passcode.isEmpty()) ? this.passcode : null;
	}
	
	@Override
	public void setPassword(String password) {
		passcode = password;
	}
}
