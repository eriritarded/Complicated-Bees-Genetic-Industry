package com.thesmartnoob880.genindustry_bees.compat.jei;

import com.accbdd.complicated_bees.bees.Species;
import com.accbdd.complicated_bees.registry.SpeciesRegistration;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import com.thesmartnoob880.genindustry_bees.item.modCreativeTab;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
public class GenIndustryBeesJEI implements IModPlugin {

    @Override
    public @NotNull Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(GenIndustryBees.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new genomeExtractorCategory(helper));
        registration.addRecipeCategories(new geneticImprinterCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<Species> species = Minecraft.getInstance().getConnection().registryAccess().lookup(SpeciesRegistration.SPECIES_REGISTRY_KEY).get().stream().toList();
        Stream<ItemStack> speciesTemplate = species.stream().map(JEIChromosomes::completeTemplate);
        ItemStack[] geneSamples = modCreativeTab.POSSIBLE_SAMPLES.get().getDisplayItems().toArray(new ItemStack[]{});
        registration.addRecipes(genomeExtractorCategory.TYPE, species);
        registration.addRecipes(geneticImprinterCategory.TYPE, species);
        List<ItemStack> templateAndSamples = new ArrayList<>(List.of(modItems.GENETIC_TEMPLATE.get().getDefaultInstance()));
        templateAndSamples.addAll(List.of(geneSamples));
        registration.addIngredientInfo(templateAndSamples, VanillaTypes.ITEM_STACK, Component.translatable("genindustry_bees.jei.info.add_gene_data"));
        registration.addIngredientInfo(List.of(modItems.GENE_SAMPLE.get().getDefaultInstance(), modItems.GENETIC_TEMPLATE.get().getDefaultInstance()), VanillaTypes.ITEM_STACK, Component.translatable("genindustry_bees.jei.info.recycle"));
        registration.addIngredientInfo(List.of(modBlocks.GENETIC_IMPRINTER.get().asItem().getDefaultInstance()), VanillaTypes.ITEM_STACK, Component.translatable("genindustry_bees.jei.info.genetic_imprinter.examples"));
        registration.addIngredientInfo(List.of(modBlocks.GENOME_EXTRACTOR.get().asItem().getDefaultInstance()), VanillaTypes.ITEM_STACK, Component.translatable("genindustry_bees.jei.info.genome_extractor.examples"));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(modItems.GENE_SAMPLE.get(),(stack,context)->com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack));
    }


    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(modBlocks.GENOME_EXTRACTOR.get().asItem(), genomeExtractorCategory.TYPE);
        registration.addRecipeCatalyst(modBlocks.GENETIC_IMPRINTER.get().asItem(), geneticImprinterCategory.TYPE);

    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);
    }

    public static IDrawable createDrawable(final Identifier location, final int uOffset, final int vOffset, final int width, final int height, final int textureWidth, final int textureHeight) {
        return new IDrawable() {
            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public void draw(@NotNull GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
                com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,location, xOffset, yOffset, (float)uOffset, (float)vOffset, this.getWidth(), this.getHeight(), textureWidth, textureHeight);
            }
        };
    }
}