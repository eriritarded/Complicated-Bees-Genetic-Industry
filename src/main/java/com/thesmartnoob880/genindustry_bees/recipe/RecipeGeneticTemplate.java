package com.thesmartnoob880.genindustry_bees.recipe;
import com.thesmartnoob880.genindustry_bees.item.*;
import com.thesmartnoob880.genindustry_bees.port.GeneticData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public class RecipeGeneticTemplate extends CustomRecipe {
    @Override public boolean matches(CraftingInput input,Level level){
        int samples=0,templates=0;
        for(ItemStack stack:input.items()){
            if(stack.isEmpty())continue;
            if(stack.is(modItems.GENE_SAMPLE.get())&&geneSampleItem.hasGeneData(stack))samples++;
            else if(stack.is(modItems.GENETIC_TEMPLATE.get()))templates++;
            else return false;
        }
        return samples==1&&templates==1;
    }
    @Override public ItemStack assemble(CraftingInput input){
        ItemStack sample=ItemStack.EMPTY,template=ItemStack.EMPTY;
        for(ItemStack stack:input.items()){
            if(stack.is(modItems.GENE_SAMPLE.get()))sample=stack;
            else if(stack.is(modItems.GENETIC_TEMPLATE.get()))template=stack;
        }
        if(template.isEmpty()||!geneSampleItem.hasGeneData(sample))return ItemStack.EMPTY;
        ItemStack result=template.copyWithCount(1);
        var gene=GeneticData.get(sample);var genes=GeneticData.get(template);
        genes.put(gene.getStringOr(geneSampleItem.KEY_GENE_NAME,""),gene.getCompoundOrEmpty(geneSampleItem.KEY_GENE_VALUE));
        GeneticData.set(result,genes);return result;
    }
    @Override public RecipeSerializer<RecipeGeneticTemplate> getSerializer(){return modRecipes.GENETIC_TEMPLATE.get();}
}
