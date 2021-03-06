package net.geforcemods.securitycraft.gui;

import org.lwjgl.opengl.GL11;

import net.geforcemods.securitycraft.containers.BriefcaseInventory;
import net.geforcemods.securitycraft.containers.ContainerBriefcase;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiBriefcaseInventory extends GuiContainer {

	private static final ResourceLocation field_110410_t = new ResourceLocation("securitycraft:textures/gui/container/briefcase_inventory.png");

	public GuiBriefcaseInventory(EntityPlayer player, InventoryPlayer inventory) {
		super(new ContainerBriefcase(player, inventory, new BriefcaseInventory(player.inventory.getCurrentItem())));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		this.fontRenderer.drawString(ClientUtils.localize("item.briefcase.name"), this.xSize / 2 - this.fontRenderer.getStringWidth(ClientUtils.localize("item.briefcase.name")) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(field_110410_t);
		int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}

}
