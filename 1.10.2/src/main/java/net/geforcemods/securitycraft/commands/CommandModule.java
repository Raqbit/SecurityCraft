package net.geforcemods.securitycraft.commands;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class CommandModule extends CommandBase implements ICommand {

	private List<String> nicknames;

	public CommandModule() {
		this.nicknames = new ArrayList<String>();
		this.nicknames.add("module");
	}
	
    @Override
	public int getRequiredPermissionLevel()
    {
        return 0;
    }

	@Override
	public String getCommandName() {
		return "module";
	}
	
	@Override
	public List<String> getCommandAliases() {
		return this.nicknames;
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return ClientUtils.localize("messages.command.module.usage");
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException{
		if(args.length == 1){
			if(args[0].matches("copy")){
				EntityPlayer player = PlayerUtils.getPlayerFromName(sender.getName());

				if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemModule && ((ItemModule) player.inventory.getCurrentItem().getItem()).canNBTBeModified()){		
					mod_SecurityCraft.instance.setSavedModule(player.inventory.getCurrentItem().getTagCompound());
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.saved"), TextFormatting.GREEN);
				}else{
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.notHoldingForData"), TextFormatting.RED);
				}
				
				return;
			}else if(args[0].matches("paste")){
				EntityPlayer player = PlayerUtils.getPlayerFromName(sender.getName());

				if(mod_SecurityCraft.instance.getSavedModule() == null){
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.nothingSaved"), TextFormatting.RED);
					return;
				}
				
				if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemModule && ((ItemModule) player.inventory.getCurrentItem().getItem()).canNBTBeModified()){		
					player.inventory.getCurrentItem().setTagCompound(mod_SecurityCraft.instance.getSavedModule());
					mod_SecurityCraft.instance.setSavedModule(null);
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.saved"), TextFormatting.GREEN);
				}
				
				return;
			}
		}else if(args.length == 2){
			if(args[0].matches("add")){
				EntityPlayer player = PlayerUtils.getPlayerFromName(sender.getName());
				
				if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemModule && ((ItemModule) player.inventory.getCurrentItem().getItem()).canNBTBeModified()){			
					if(player.inventory.getCurrentItem().getTagCompound() == null){
						player.inventory.getCurrentItem().setTagCompound(new NBTTagCompound());				
					}
					
					for(int i = 1; i <= 10; i++){
						if(player.inventory.getCurrentItem().getTagCompound().hasKey("Player" + i) && player.inventory.getCurrentItem().getTagCompound().getString("Player" + i).matches(args[1])){
							PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.alreadyContained").replace("#", args[1]), TextFormatting.RED);
							return;
						}
					}
                    
					player.inventory.getCurrentItem().getTagCompound().setString("Player" + getNextSlot(player.inventory.getCurrentItem().getTagCompound()), args[1]);
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.added").replace("#", args[1]), TextFormatting.GREEN);
					return;
				}else{
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.notHoldingForModify"), TextFormatting.RED);
					return;
				}
			}else if(args[0].matches("remove")){
				EntityPlayer player = PlayerUtils.getPlayerFromName(sender.getName());
				
				if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemModule && ((ItemModule) player.inventory.getCurrentItem().getItem()).canNBTBeModified()){			
					if(player.inventory.getCurrentItem().getTagCompound() == null){
						player.inventory.getCurrentItem().setTagCompound(new NBTTagCompound());				
					}
					
					for(int i = 1; i <= 10; i++){
						if(player.inventory.getCurrentItem().getTagCompound().hasKey("Player" + i) && player.inventory.getCurrentItem().getTagCompound().getString("Player" + i).matches(args[1])){
							player.inventory.getCurrentItem().getTagCompound().removeTag("Player" + i);
						}
					}
					
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.removed").replace("#", args[1]), TextFormatting.GREEN);
					return;
				}else{
					PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("messages.module.manager"), ClientUtils.localize("messages.module.notHoldingForModify"), TextFormatting.RED);
					return;
				}
			}
		}
		
		throw new WrongUsageException(ClientUtils.localize("messages.command.module.usage"));
	}

	private int getNextSlot(NBTTagCompound stackTagCompound) {
		for(int i = 1; i <= 10; i++){
			if(stackTagCompound.getString("Player" + i) != null && !stackTagCompound.getString("Player" + i).isEmpty()){
				continue;
			}else{
				return i;
			}
		}
		
		return 0;
	}
}
