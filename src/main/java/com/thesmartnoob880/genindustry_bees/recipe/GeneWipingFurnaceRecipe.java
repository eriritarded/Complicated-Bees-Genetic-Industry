package com.thesmartnoob880.genindustry_bees.recipe;
import com.thesmartnoob880.genindustry_bees.item.IGeneticHolder;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public class GeneWipingFurnaceRecipe extends SmeltingRecipe {
    public static final MapCodec<GeneWipingFurnaceRecipe> CODEC=cookingMapCodec(GeneWipingFurnaceRecipe::new,200);
    public static final StreamCodec<RegistryFriendlyByteBuf,GeneWipingFurnaceRecipe> STREAM_CODEC=cookingStreamCodec(GeneWipingFurnaceRecipe::new);
    public GeneWipingFurnaceRecipe(Recipe.CommonInfo common,AbstractCookingRecipe.CookingBookInfo book,Ingredient ingredient,ItemStackTemplate result,float experience,int cookingTime){super(common,book,ingredient,result,experience,cookingTime);}
    @Override public boolean matches(SingleRecipeInput input,Level level){
        return super.matches(input,level)&&input.item().getItem() instanceof IGeneticHolder holder&&holder.hasGeneticData(input.item());
    }
    @Override @SuppressWarnings({"unchecked","rawtypes"}) public RecipeSerializer<SmeltingRecipe> getSerializer(){return (RecipeSerializer)modRecipes.FURNACE_RECYCLING.get();}
}
