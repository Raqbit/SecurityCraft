package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockReinforcedWoodSlabs extends ItemBlockTinted {
	
	private BlockSlab singleSlab = (BlockSlab) mod_SecurityCraft.reinforcedWoodSlabs;
	private Block doubleSlab = mod_SecurityCraft.reinforcedDoubleWoodSlabs;

	public ItemBlockReinforcedWoodSlabs(Block block) {
		super(block);
		this.setHasSubtypes(true);
	}
	
	public int getMetadata(int meta){
		return meta;
	}
	
	public String getUnlocalizedName(ItemStack stack){
		if(stack.getItemDamage() == 0){
			return this.getUnlocalizedName() + "_oak";
		}else if(stack.getItemDamage() == 1){
			return this.getUnlocalizedName() + "_spruce";
		}else if(stack.getItemDamage() == 2){
			return this.getUnlocalizedName() + "_birch";
		}else if(stack.getItemDamage() == 3){
			return this.getUnlocalizedName() + "_jungle";
		}else if(stack.getItemDamage() == 4){
			return this.getUnlocalizedName() + "_acacia";
		}else if(stack.getItemDamage() == 5){
			return this.getUnlocalizedName() + "_darkoak";
		}else{
			return this.getUnlocalizedName();
		}
	}
	
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ){
        if(stack.stackSize == 0){
            return false;
        }else if (!playerIn.canPlayerEdit(pos.offset(side), side, stack)){
            return false;
        }else{
            Object object = this.singleSlab.getVariant(stack);
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if(iblockstate.getBlock() == this.singleSlab){
                IProperty iproperty = this.singleSlab.getVariantProperty();
                Comparable<?> comparable = iblockstate.getValue(iproperty);
                BlockSlab.EnumBlockHalf enumblockhalf = (BlockSlab.EnumBlockHalf)iblockstate.getValue(BlockSlab.HALF);
                
                Owner owner = null;

                if(worldIn.getTileEntity(pos) instanceof IOwnable){
                	owner = ((IOwnable) worldIn.getTileEntity(pos)).getOwner();
                	
                	if(!((IOwnable) worldIn.getTileEntity(pos)).getOwner().isOwner(playerIn)){
                		if(!worldIn.isRemote){
                			PlayerUtils.sendMessageToPlayer(playerIn, StatCollector.translateToLocal("messages.reinforcedSlab"), StatCollector.translateToLocal("messages.reinforcedSlab.cannotDoubleSlab"), EnumChatFormatting.RED);
                		}
                		
                		return false;
                	}             
                }
                
                if((side == EnumFacing.UP && enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM || side == EnumFacing.DOWN && enumblockhalf == BlockSlab.EnumBlockHalf.TOP) && comparable == object){
                    IBlockState iblockstate1 = this.doubleSlab.getDefaultState().withProperty(iproperty, comparable);

                    if(worldIn.checkNoEntityCollision(this.doubleSlab.getCollisionBoundingBox(worldIn, pos, iblockstate1)) && worldIn.setBlockState(pos, iblockstate1, 3)){
                        worldIn.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.doubleSlab.stepSound.getPlaceSound(), (this.doubleSlab.stepSound.getVolume() + 1.0F) / 2.0F, this.doubleSlab.stepSound.getFrequency() * 0.8F);
                        --stack.stackSize;
                        
                        if(owner != null){
                        	((IOwnable) worldIn.getTileEntity(pos)).getOwner().set(owner.getUUID(), owner.getName());
                        }
                    }

                    return true;
                }
            }

            return this.tryPlace(stack, worldIn, pos.offset(side), object) ? true : super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack){
        BlockPos blockpos1 = pos;
        IProperty iproperty = this.singleSlab.getVariantProperty();
        Object object = this.singleSlab.getVariant(stack);
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if(iblockstate.getBlock() == this.singleSlab){
            boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

            if((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) && object == iblockstate.getValue(iproperty)){
                return true;
            }
        }

        pos = pos.offset(side);
        IBlockState iblockstate1 = worldIn.getBlockState(pos);
        return iblockstate1.getBlock() == this.singleSlab && object == iblockstate1.getValue(iproperty) ? true : super.canPlaceBlockOnSide(worldIn, blockpos1, side, player, stack);
    }

    private boolean tryPlace(ItemStack stack, World worldIn, BlockPos pos, Object variantInStack){
        IBlockState iblockstate = worldIn.getBlockState(pos);
        
        Owner owner = null;

        if(worldIn.getTileEntity(pos) instanceof IOwnable){
        	owner = ((IOwnable) worldIn.getTileEntity(pos)).getOwner();
        }

        if(iblockstate.getBlock() == this.singleSlab){
            Comparable<?> comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());

            if(comparable == variantInStack){
                IBlockState iblockstate1 = this.doubleSlab.getDefaultState().withProperty(this.singleSlab.getVariantProperty(), comparable);

                if (worldIn.checkNoEntityCollision(this.doubleSlab.getCollisionBoundingBox(worldIn, pos, iblockstate1)) && worldIn.setBlockState(pos, iblockstate1, 3)){
                    worldIn.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.doubleSlab.stepSound.getPlaceSound(), (this.doubleSlab.stepSound.getVolume() + 1.0F) / 2.0F, this.doubleSlab.stepSound.getFrequency() * 0.8F);
                    --stack.stackSize;
                    
                    if(owner != null){
                    	((IOwnable) worldIn.getTileEntity(pos)).getOwner().set(owner.getUUID(), owner.getName());
                    }
                }

                return true;
            }
        }

        return false;
    }

}
