package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.blocks.BlockRetinalScanner;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;


public class TileEntityRetinalScanner extends CustomizableSCTE {
	
	private OptionBoolean activatedByEntities = new OptionBoolean("activatedByEntities", false);

	@Override
	public void entityViewed(EntityLivingBase entity){
		if(!world.isRemote && !BlockUtils.getBlockPropertyAsBoolean(world, pos, BlockRetinalScanner.POWERED)){
			if(!(entity instanceof EntityPlayer) && !activatedByEntities.asBoolean())
				return;
			
			if(entity instanceof EntityPlayer && PlayerUtils.isPlayerMountedOnCamera(entity))
				return;
			
			if(entity instanceof EntityPlayer && !getOwner().isOwner((EntityPlayer) entity)) {
                PlayerUtils.sendMessageToPlayer((EntityPlayer) entity, ClientUtils.localize("tile.retinalScanner.name"), ClientUtils.localize("messages.retinalScanner.notOwner").replace("#", getOwner().getName()), TextFormatting.RED);
				return;
			}
			
			BlockUtils.setBlockProperty(world, pos, BlockRetinalScanner.POWERED, true);
    		world.scheduleUpdate(new BlockPos(pos), mod_SecurityCraft.retinalScanner, 60);
    		
            if(entity instanceof EntityPlayer){
                PlayerUtils.sendMessageToPlayer((EntityPlayer) entity, ClientUtils.localize("tile.retinalScanner.name"), ClientUtils.localize("messages.retinalScanner.hello").replace("#", entity.getName()), TextFormatting.GREEN);
            }             
    	}
	}
	
	@Override
	public int getViewCooldown() {
    	return 30;
    }
	
	@Override
	public boolean activatedOnlyByPlayer() {
    	return !activatedByEntities.asBoolean();
    }

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[]{EnumCustomModules.WHITELIST};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[]{ activatedByEntities };
	}

}
