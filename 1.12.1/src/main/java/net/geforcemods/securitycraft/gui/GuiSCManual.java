package net.geforcemods.securitycraft.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.api.TileEntitySCTE;
import net.geforcemods.securitycraft.gui.components.CustomHoverChecker;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.GuiUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiSCManual extends GuiScreen {

	private ResourceLocation infoBookTexture = new ResourceLocation("securitycraft:textures/gui/info_book_texture.png");
	private ResourceLocation infoBookTextureSpecial = new ResourceLocation("securitycraft:textures/gui/info_book_texture_special.png"); //for items without a recipe
	private ResourceLocation infoBookTitlePage = new ResourceLocation("securitycraft:textures/gui/info_book_title_page.png");
	private ResourceLocation infoBookIcons = new ResourceLocation("securitycraft:textures/gui/info_book_icons.png");
	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	
    private List<CustomHoverChecker> hoverCheckers = new ArrayList<CustomHoverChecker>();
    private int currentPage = -1;
    private NonNullList<Ingredient> recipe;
    int k = -1;
    boolean update = false;
    
	public GuiSCManual() {
		super();
	}
	
	@Override
	public void initGui(){		
		byte b0 = 2;
		
		if((this.width - 256) / 2 != k && k != -1)
			update = true;

		k = (this.width - 256) / 2;
		Keyboard.enableRepeatEvents(true);
        GuiSCManual.NextPageButton nextButton = new GuiSCManual.NextPageButton(1, k + 210, b0 + 158, true);
        GuiSCManual.NextPageButton prevButton = new GuiSCManual.NextPageButton(2, k + 16, b0 + 158, false);

        this.buttonList.add(nextButton);
        this.buttonList.add(prevButton);
    }
	
	@Override
	public void drawScreen(int par1, int par2, float par3){		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if(update)
		{
			updateRecipeAndIcons();
			update = false;
		}
		
		if(this.currentPage == -1){
	    	this.mc.getTextureManager().bindTexture(infoBookTitlePage);
		}else{
			if(this.recipe != null)
				this.mc.getTextureManager().bindTexture(infoBookTexture);
			else
				this.mc.getTextureManager().bindTexture(infoBookTextureSpecial);
		}
		
	    this.drawTexturedModalRect(k, 5, 0, 0, 256, 250);
	    
	    if(this.currentPage > -1){
	    	this.fontRenderer.drawString(ClientUtils.localize(mod_SecurityCraft.instance.manualPages.get(currentPage).getItem().getUnlocalizedName() + ".name"), k + 39, 27, 0, false);	
	    		this.fontRenderer.drawSplitString(mod_SecurityCraft.instance.manualPages.get(currentPage).getHelpInfo(), k + 18, 45, 225, 0);	
	    }else{
	    	this.fontRenderer.drawString(ClientUtils.localize("gui.scManual.intro.1"), k + 39, 27, 0, false);	
	    	this.fontRenderer.drawString(ClientUtils.localize("gui.scManual.intro.2"), k + 60, 159, 0, false);	
	    
	    	if(I18n.hasKey("gui.scManual.author")){
		    	this.fontRenderer.drawString(ClientUtils.localize("gui.scManual.author"), k + 65, 170, 0, false);
	    	}
	    }
	    
	    for(int i = 0; i < this.buttonList.size(); i++){
            this.buttonList.get(i).drawButton(this.mc, par1, par2, 0);
        }
	    
	    if(this.currentPage > -1){
	    	Item item = mod_SecurityCraft.instance.manualPages.get(currentPage).getItem();
	    	GuiUtils.drawItemStackToGui(mc, item, k + 19, 22, !(mod_SecurityCraft.instance.manualPages.get(currentPage).getItem() instanceof ItemBlock));

	    	this.mc.getTextureManager().bindTexture(infoBookIcons);

	    	TileEntity te = ((item instanceof ItemBlock && ((ItemBlock) item).getBlock() instanceof ITileEntityProvider) ? ((ITileEntityProvider) ((ItemBlock) item).getBlock()).createNewTileEntity(Minecraft.getMinecraft().world, 0) : null);
	    	Block itemBlock = ((item instanceof ItemBlock) ? ((ItemBlock) item).getBlock() : null);
	    	
	    	if(itemBlock != null){    		
	    		if(itemBlock instanceof IExplosive){
	    	    	this.drawTexturedModalRect(k + 107, 117, 54, 1, 18, 18);
		    	}	
	    		
	    		if(te != null){
			    	if(te instanceof IOwnable){
		    	    	this.drawTexturedModalRect(k + 29, 118, 1, 1, 16, 16);
			    	}	
			    	
			    	if(te instanceof IPasswordProtected){
		    	    	this.drawTexturedModalRect(k + 55, 118, 18, 1, 17, 16);
			    	}	
			    	
			    	if(te instanceof TileEntitySCTE && ((TileEntitySCTE) te).isActivatedByView()){
		    	    	this.drawTexturedModalRect(k + 81, 118, 36, 1, 17, 16);
			    	}	
			    			    		 
			    	if(te instanceof CustomizableSCTE){
		    	    	this.drawTexturedModalRect(k + 213, 118, 72, 1, 16, 16);
			    	}
	    		}
	    	}
	    	
	    	if(recipe != null){
		    	for(int i = 0; i < 3; i++){
		    		for(int j = 0; j < 3; j++){
		    			if(((i * 3) + j) >= recipe.size()){ break; }
		    			if(recipe.get((i * 3) + j).getMatchingStacks().length == 0 || this.recipe.get((i * 3) + j).getMatchingStacks()[0].isEmpty()){ continue; }
		    			
		    			if(this.recipe.get((i * 3) + j).getMatchingStacks()[0].getItem() instanceof ItemBlock){
			    	    	GuiUtils.drawItemStackToGui(mc, Block.getBlockFromItem(this.recipe.get((i * 3) + j).getMatchingStacks()[0].getItem()), (k + 100) + (j * 20), 144 + (i * 20), !(this.recipe.get((i * 3) + j).getMatchingStacks()[0].getItem() instanceof ItemBlock));
		    			}else{
			    	    	GuiUtils.drawItemStackToGui(mc, this.recipe.get((i * 3) + j).getMatchingStacks()[0].getItem(), 0, (k + 100) + (j * 20), 144 + (i * 20), !(this.recipe.get((i * 3) + j).getMatchingStacks()[0].getItem() instanceof ItemBlock));
		    			}		    			   
		    		}
		    	}
	    	}	
	    	
	    	for(CustomHoverChecker chc : hoverCheckers){
	    		if(chc != null && chc.checkHover(par1, par2)){
	    			if(chc.getName() != null)
	    				drawHoveringText(mc.fontRenderer.listFormattedStringToWidth(chc.getName(), 250), par1, par2, mc.fontRenderer);
	    		}
	    	}
	    }
	}
	
	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	protected void keyTyped(char par1, int par2) throws IOException{
		super.keyTyped(par1, par2);
		
		if(par2 == Keyboard.KEY_LEFT){
    		this.currentPage--;
    		
    		if(currentPage < -1)
    			currentPage = mod_SecurityCraft.instance.manualPages.size() - 1;
    		
			Minecraft.getMinecraft().player.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("random.click")), 0.15F, 1.0F);
			this.updateRecipeAndIcons();
		}else if(par2 == Keyboard.KEY_RIGHT){
			this.currentPage++;
    		
    		if(currentPage > mod_SecurityCraft.instance.manualPages.size() - 1)
    			currentPage = -1;
    		
			Minecraft.getMinecraft().player.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("random.click")), 0.15F, 1.0F);
			this.updateRecipeAndIcons();
		}
	}
	
    @Override
	protected void actionPerformed(GuiButton par1GuiButton){
    	if(par1GuiButton.id == 1){
    		this.currentPage++;
    		
    		if(currentPage > mod_SecurityCraft.instance.manualPages.size() - 1)
    			currentPage = -1;
    		
    		this.updateRecipeAndIcons();
    	}else if(par1GuiButton.id == 2){
    		this.currentPage--;
    		
    		if(currentPage < -1)
    			currentPage = mod_SecurityCraft.instance.manualPages.size() - 1;
    		
    		this.updateRecipeAndIcons();
    	}
    }
    
    private void updateRecipeAndIcons(){
    	if(this.currentPage < 0){ 
    		recipe = null; 
    		this.hoverCheckers.clear();
    		return;
    	}
    	
		this.hoverCheckers.clear();

		if(mod_SecurityCraft.instance.manualPages.get(currentPage).hasCustomRecipe()) {
			this.recipe = mod_SecurityCraft.instance.manualPages.get(currentPage).getRecipe();
		}
		else {
	    	for(int o = 0; o < CraftingManager.REGISTRY.getKeys().size(); o++)
	    	{
	    		IRecipe object = CraftingManager.REGISTRY.getObjectById(o);

	    		if(object instanceof ShapedRecipes){
					ShapedRecipes recipe = (ShapedRecipes) object;
					
					if(!recipe.getRecipeOutput().isEmpty() && recipe.getRecipeOutput().getItem() == mod_SecurityCraft.instance.manualPages.get(currentPage).getItem()){
						NonNullList<Ingredient> recipeItems = NonNullList.<Ingredient>withSize(recipe.recipeItems.size(), Ingredient.EMPTY);
						
						for(int i = 0; i < recipeItems.size(); i++)
						{
							recipeItems.set(i, recipe.recipeItems.get(i));
						}
						
						this.recipe = recipeItems;
						break;
					}
				}else if(object instanceof ShapelessRecipes){
					ShapelessRecipes recipe = (ShapelessRecipes) object;
	
					if(!recipe.getRecipeOutput().isEmpty() && recipe.getRecipeOutput().getItem() == mod_SecurityCraft.instance.manualPages.get(currentPage).getItem()){
						NonNullList<Ingredient> recipeItems = NonNullList.<Ingredient>withSize(recipe.recipeItems.size(), Ingredient.EMPTY);
						
						for(int i = 0; i < recipeItems.size(); i++)
						{
							recipeItems.set(i, recipe.recipeItems.get(i));
						}
						
						this.recipe = recipeItems;
						break;
					}
				}
				
				this.recipe = null;
			}
		}
    	
		if(recipe != null)
		{
			outer:
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					if((i * 3) + j == recipe.size())
						break outer;
					
					if(recipe.get((i * 3) + j).getMatchingStacks().length > 0 && !recipe.get((i * 3) + j).getMatchingStacks()[0].isEmpty())
						hoverCheckers.add(new CustomHoverChecker(144 + (i * 20), 144 + (i * 20) + 16, (k + 100) + (j * 20), (k + 100) + (j * 20) + 16, 20, recipe.get((i * 3) + j).getMatchingStacks()[0].getDisplayName()));	
				}
			}
		}
		else
		{
			String name = mod_SecurityCraft.instance.manualPages.get(currentPage).getItemName();
			
			name = name.substring(0, 1).toLowerCase() + name.substring(1, name.length()).replace(" ", ""); //make first character lower case and remove spaces
			hoverCheckers.add(new CustomHoverChecker(144, 144 + (2 * 20) + 16, k + 100, (k + 100) + (2 * 20) + 16, 20, ClientUtils.localize("gui.scManual.recipe." + name)));
		}
		
    	Item item = mod_SecurityCraft.instance.manualPages.get(currentPage).getItem();
    	TileEntity te = ((item instanceof ItemBlock && ((ItemBlock) item).getBlock() instanceof ITileEntityProvider) ? ((ITileEntityProvider) ((ItemBlock) item).getBlock()).createNewTileEntity(Minecraft.getMinecraft().world, 0) : null);
    	Block itemBlock = ((item instanceof ItemBlock) ? ((ItemBlock) item).getBlock() : null);

    	if(te != null){
	    	if(te instanceof IOwnable){
	    		this.hoverCheckers.add(new CustomHoverChecker(118, 118 + 16, k + 29, (k + 29) + 16, 20, ClientUtils.localize("gui.scManual.ownableBlock"))); 
	    	}	
	    	
	    	if(te instanceof IPasswordProtected){
	    		this.hoverCheckers.add(new CustomHoverChecker(118, 118 + 16, k + 55, (k + 55) + 16, 20, ClientUtils.localize("gui.scManual.passwordProtectedBlock"))); 
	    	}
	    	
	    	if(te instanceof TileEntitySCTE && ((TileEntitySCTE) te).isActivatedByView()){
	    		this.hoverCheckers.add(new CustomHoverChecker(118, 118 + 16, k + 81, (k + 81) + 16, 20, ClientUtils.localize("gui.scManual.viewActivatedBlock"))); 
	    	}
	    	
	    	if(itemBlock instanceof IExplosive){
	    		this.hoverCheckers.add(new CustomHoverChecker(118, 118 + 16, k + 107, (k + 107) + 16, 20, ClientUtils.localize("gui.scManual.explosiveBlock"))); 
	    	}
	    	
	    	if(te instanceof CustomizableSCTE){
	    		this.hoverCheckers.add(new CustomHoverChecker(118, 118 + 16, k + 213, (k + 213) + 16, 20, ClientUtils.localize("gui.scManual.customizableBlock"))); 
	    	}
    	}
    }
    
	@SideOnly(Side.CLIENT)
    static class NextPageButton extends GuiButton {
		private final boolean field_146151_o;

		public NextPageButton(int par1, int par2, int par3, boolean par4){
			super(par1, par2, par3, 23, 13, "");
			this.field_146151_o = par4;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_, float f){
			if(this.visible){
				boolean flag = p_146112_2_ >= this.x && p_146112_3_ >= this.y && p_146112_2_ < this.x + this.width && p_146112_3_ < this.y + this.height;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				p_146112_1_.getTextureManager().bindTexture(bookGuiTextures);
				int k = 0;
				int l = 192;

				if(flag){
					k += 23;
				}

				if(!this.field_146151_o){
					l += 13;
				}

				this.drawTexturedModalRect(this.x, this.y, k, l, 23, 13);
			}
		}
	}

}
