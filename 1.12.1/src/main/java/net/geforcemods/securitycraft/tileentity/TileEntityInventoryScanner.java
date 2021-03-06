package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class TileEntityInventoryScanner extends CustomizableSCTE implements IInventory{
	
	private NonNullList<ItemStack> inventoryContents = NonNullList.<ItemStack>withSize(37, ItemStack.EMPTY); 
	private String type = "check";
	private boolean isProvidingPower;
	private int cooldown;
	
	@Override
	public void update(){ 						
    	if(cooldown > 0){
    		cooldown--;
    	}else{
    		if(isProvidingPower){
    			isProvidingPower = false;
    			BlockUtils.updateAndNotify(getWorld(), pos, getWorld().getBlockState(pos).getBlock(), 1, true);
    		}
    	}
    }
    
    @Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound){ 	
    	super.readFromNBT(par1NBTTagCompound);
    	
        NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
        this.inventoryContents = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.inventoryContents.size())
            {
                this.inventoryContents.set(j, new ItemStack(nbttagcompound1));
            }
        }
        

    	if(par1NBTTagCompound.hasKey("cooldown")){
    	  	this.cooldown = par1NBTTagCompound.getInteger("cooldown");
      	}
    	
    	if(par1NBTTagCompound.hasKey("type")){
    	  	this.type = par1NBTTagCompound.getString("type");
      	}
    	
    }

    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound){   
    	super.writeToNBT(par1NBTTagCompound);
    	
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.inventoryContents.size(); ++i)
        {
            if (!this.inventoryContents.get(i).isEmpty())
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.inventoryContents.get(i).writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        par1NBTTagCompound.setTag("Items", nbttaglist);
        par1NBTTagCompound.setInteger("cooldown", cooldown);
        par1NBTTagCompound.setString("type", type);
        return par1NBTTagCompound;
    }
    
	@Override
	public int getSizeInventory() {
		return 37;
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
    {
        if (!this.inventoryContents.get(par1).isEmpty())
        {
            ItemStack itemstack;

            if (this.inventoryContents.get(par1).getCount() <= par2)
            {
                itemstack = this.inventoryContents.get(par1);
                this.inventoryContents.set(par1, ItemStack.EMPTY);
                this.markDirty();
                return itemstack;
            }
            else
            {
                itemstack = this.inventoryContents.get(par1).splitStack(par2);

                if (this.inventoryContents.get(par1).getCount() == 0)
                {
                    this.inventoryContents.set(par1, ItemStack.EMPTY);
                }

                this.markDirty();
                return itemstack;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

	/**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (!this.inventoryContents.get(par1).isEmpty())
        {
            ItemStack itemstack = this.inventoryContents.get(par1);
            this.inventoryContents.set(par1, ItemStack.EMPTY);
            return itemstack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

	@Override
	public ItemStack getStackInSlot(int var1) {
		return this.inventoryContents.get(var1);
	}
	
	/**
	 * Copy of getStackInSlot which doesn't get overrided by CustomizableSCTE.
	 */
	
	public ItemStack getStackInSlotCopy(int var1) {
		return this.inventoryContents.get(var1);
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		this.inventoryContents.set(par1, par2ItemStack);

        if (!par2ItemStack.isEmpty() && par2ItemStack.getCount() > this.getInventoryStackLimit())
        {
            par2ItemStack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
	}
	
	public void addItemToStorage(ItemStack par1ItemStack) {
		for(int i = 10; i < this.inventoryContents.size(); i++){
			if(this.inventoryContents.get(i).isEmpty()){
				this.inventoryContents.set(i, par1ItemStack);
				break;
			}else if(!this.inventoryContents.get(i).isEmpty() && this.inventoryContents.get(i).getItem() != null && par1ItemStack.getItem() != null && this.inventoryContents.get(i).getItem() == par1ItemStack.getItem()){
				if(this.inventoryContents.get(i).getCount() + par1ItemStack.getCount() <= this.getInventoryStackLimit()){
					this.inventoryContents.get(i).grow(par1ItemStack.getCount());
					break;
				}else{
					this.inventoryContents.get(i).setCount(this.getInventoryStackLimit());
				}
			}
		}

        this.markDirty();
	}
	
	public void clearStorage() {
		for(int i = 10; i < this.inventoryContents.size(); i++){
			if(!this.inventoryContents.get(i).isEmpty()){
				this.inventoryContents.set(i, ItemStack.EMPTY);
				break;
			}
		}

        this.markDirty();
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}
    
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public boolean shouldProvidePower() {
		return (this.type.matches("redstone") && this.isProvidingPower) ? true : false;
	}

	public void setShouldProvidePower(boolean isProvidingPower) {
		this.isProvidingPower = isProvidingPower;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	public NonNullList<ItemStack> getContents(){
		return inventoryContents;
	}
	
	public void setContents(NonNullList<ItemStack> contents){
		this.inventoryContents = contents;
	}
	
	@Override
	public void onModuleInserted(ItemStack stack, EnumCustomModules module){
		if(!this.getWorld().isRemote){
			if(this.getWorld().getTileEntity(pos.east(2)) != null && this.getWorld().getTileEntity(pos.east(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.west(2)) != null && this.getWorld().getTileEntity(pos.west(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.south(2)) != null && this.getWorld().getTileEntity(pos.south(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.north(2)) != null && this.getWorld().getTileEntity(pos.north(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).insertModule(stack);
				}
			}
		}
	}
	
	@Override
	public void onModuleRemoved(ItemStack stack, EnumCustomModules module){
		if(!this.getWorld().isRemote){
			if(this.getWorld().getTileEntity(pos.east(2)) != null && this.getWorld().getTileEntity(pos.east(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.west(2)) != null && this.getWorld().getTileEntity(pos.west(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.south(2)) != null && this.getWorld().getTileEntity(pos.south(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.north(2)) != null && this.getWorld().getTileEntity(pos.north(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).removeModule(module);
				}
			}
		}
	}

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[]{EnumCustomModules.WHITELIST, EnumCustomModules.SMART, EnumCustomModules.STORAGE};
	}

	@Override
	public Option<?>[] customOptions() {
		return null;
	}
	
}
