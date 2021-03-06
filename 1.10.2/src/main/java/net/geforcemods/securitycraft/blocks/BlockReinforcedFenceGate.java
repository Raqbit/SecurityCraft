package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class BlockReinforcedFenceGate extends BlockFenceGate implements ITileEntityProvider, IIntersectable {

	public BlockReinforcedFenceGate(){
		super(BlockPlanks.EnumType.OAK);
		ObfuscationReflectionHelper.setPrivateValue(Block.class, this, Material.IRON, 18);
		setSoundType(SoundType.METAL);
	}

	/**
     * Called upon block activation (right click on the block.)
     */
	@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){
        return false;
    }
    
    @Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state){
        super.breakBlock(par1World, pos, state);
        par1World.removeTileEntity(pos);
    }
    
    @Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity) {
		if(BlockUtils.getBlockPropertyAsBoolean(world, pos, OPEN)){
			return;
		}
    	
    	if(entity instanceof EntityItem)
			return;
		else if(entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entity;

			if(((TileEntityOwnable)world.getTileEntity(pos)).getOwner().isOwner(player))
				return;
		}
		else if(entity instanceof EntityCreeper)
		{
			EntityCreeper creeper = (EntityCreeper)entity;
			EntityLightningBolt lightning = new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), true);

			creeper.onStruckByLightning(lightning);
			return;
		}

		entity.attackEntityFrom(CustomDamageSources.electricity, 6.0F);
	}
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        if(!worldIn.isRemote) {
            boolean flag = isSCBlock(blockIn) && worldIn.isBlockPowered(pos);

            if (flag || blockIn.getDefaultState().canProvidePower()) {
                if (flag && !((Boolean)state.getValue(OPEN)).booleanValue() && !((Boolean)state.getValue(POWERED)).booleanValue()) {
                    worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(true)).withProperty(POWERED, Boolean.valueOf(true)), 2);
                    worldIn.playEvent((EntityPlayer)null, 1008, pos, 0);
                }
                else if (!flag && ((Boolean)state.getValue(OPEN)).booleanValue() && ((Boolean)state.getValue(POWERED)).booleanValue()) {
                    worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(false)).withProperty(POWERED, Boolean.valueOf(false)), 2);
                    worldIn.playEvent((EntityPlayer)null, 1014, pos, 0);
                }
                else if (flag != ((Boolean)state.getValue(POWERED)).booleanValue()) {
                    worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(flag)), 2);
                }
            }
        }
    }
    
    private boolean isSCBlock(Block block) {
    	return (block instanceof BlockLaserBlock || block instanceof BlockRetinalScanner ||
    			block instanceof BlockKeypad || block instanceof BlockKeycardReader || block instanceof BlockInventoryScanner);
    }

    @Override
	public boolean eventReceived(IBlockState state, World par1World, BlockPos pos, int par5, int par6){
        super.eventReceived(state, par1World, pos, par5, par6);
        TileEntity tileentity = par1World.getTileEntity(pos);
        return tileentity != null ? tileentity.receiveClientEvent(par5, par6) : false;
    }
    
    @Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityOwnable().intersectsEntities();
    }

}
