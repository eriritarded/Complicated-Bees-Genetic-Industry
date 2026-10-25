package com.thesmartnoob880.genindustry_bees.compat.jei;

import com.accbdd.complicated_bees.bees.Species;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class genomeExtractorCategory implements IRecipeCategory<Species> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(GenIndustryBees.MODID, "jei/genome_extractor");
    public static final RecipeType<Species> TYPE;
    private static final Component TITLE;
    private final IDrawable ICON;
    private final IDrawable BACKGROUND = GenIndustryBeesJEI.createDrawable(Identifier.fromNamespaceAndPath("genindustry_bees", "textures/gui/jei/genome_extractor_jei.png"), 0, 0, 176, 112, 176, 112);

    public genomeExtractorCategory(IGuiHelper helper){
        this.ICON = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(modBlocks.GENOME_EXTRACTOR.get().asItem()));
    }

    @Override
    public @NotNull RecipeType<Species> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return TITLE;
    }

    public void draw(Species recipe,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,net.minecraft.client.gui.GuiGraphicsExtractor graphics,double mouseX,double mouseY){BACKGROUND.draw(graphics);}
    public int getWidth(){return BACKGROUND.getWidth();}
    public int getHeight(){return BACKGROUND.getHeight();}
    public IDrawable getBackground() {
        return this.BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return this.ICON;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull Species species, @NotNull IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 27).setSlotName("labware").addIngredients(VanillaTypes.ITEM_STACK, JEIChromosomes.validLabware());
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 53).setSlotName("bees").addIngredients(VanillaTypes.ITEM_STACK, JEIChromosomes.validBees(species));
        builder.addSlot(RecipeIngredientRole.INPUT,42,53).setSlotName("blank_gene").addIngredient(VanillaTypes.ITEM_STACK, JEIChromosomes.blankGene());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 20).setSlotName("output_gene").addIngredients(VanillaTypes.ITEM_STACK,JEIChromosomes.possibleGenes(species));
    }

    static {
        TYPE = new RecipeType<>(ID, Species.class);
        TITLE = Component.translatable("gui.genindustry_bees.jei.genome_extractor");
    }
}
