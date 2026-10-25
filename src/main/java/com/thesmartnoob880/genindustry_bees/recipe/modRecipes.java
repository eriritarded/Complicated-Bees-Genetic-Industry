package com.thesmartnoob880.genindustry_bees.recipe;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public class modRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> DEF_REG=DeferredRegister.create(Registries.RECIPE_SERIALIZER,GenIndustryBees.MODID);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RecipeGeneticTemplate>> GENETIC_TEMPLATE=DEF_REG.register("genetic_template_with_data",()->{
        RecipeGeneticTemplate recipe=new RecipeGeneticTemplate();
        return new RecipeSerializer<>(MapCodec.unit(recipe),StreamCodec.unit(recipe));
    });
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GeneWipingFurnaceRecipe>> FURNACE_RECYCLING=DEF_REG.register("furnace_recycling",()->new RecipeSerializer<>(GeneWipingFurnaceRecipe.CODEC,GeneWipingFurnaceRecipe.STREAM_CODEC));
    public static void register(IEventBus bus){DEF_REG.register(bus);}
}
