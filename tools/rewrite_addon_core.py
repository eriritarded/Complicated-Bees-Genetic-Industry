from pathlib import Path
root=Path(__file__).resolve().parents[1]
base=root/'genetic_industry/src/main/java/com/thesmartnoob880/genindustry_bees'
def write(path,text):
 (base/path).write_text(text,encoding='utf-8')
write('GenIndustryBees.java','''package com.thesmartnoob880.genindustry_bees;
import com.mojang.logging.LogUtils;
import com.thesmartnoob880.genindustry_bees.block.entity.modBlockEntities;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import com.thesmartnoob880.genindustry_bees.config.Config;
import com.thesmartnoob880.genindustry_bees.item.*;
import com.thesmartnoob880.genindustry_bees.recipe.modRecipes;
import com.thesmartnoob880.genindustry_bees.screen.*;
import com.thesmartnoob880.genindustry_bees.util.*;
import com.accbdd.complicated_bees.port.PortCapabilities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

@Mod(GenIndustryBees.MODID)
public class GenIndustryBees {
    public static final String MODID="genindustry_bees";
    public static final Logger LOGGER=LogUtils.getLogger();
    public GenIndustryBees(ModContainer container,IEventBus bus){
        modCreativeTab.register(bus);modItems.register(bus);modBlocks.register(bus);modRecipes.register(bus);modBlockEntities.register(bus);modMenuTypes.register(bus);
        bus.addListener(this::commonSetup);bus.addListener(this::capabilities);
        container.registerConfig(ModConfig.Type.COMMON,Config.SPEC);
    }
    private void commonSetup(FMLCommonSetupEvent event){event.enqueueWork(labwareHelper::registerdefault);}
    private void capabilities(RegisterCapabilitiesEvent event){
        PortCapabilities.registerBlockEntity(event,PortCapabilities.ItemHandler.BLOCK,modBlockEntities.GENOME_EXTRACTOR_BE.get(),(be,side)->be.getConjoinedItemHandler().orElse(null));
        PortCapabilities.registerBlockEntity(event,PortCapabilities.ItemHandler.BLOCK,modBlockEntities.GENETIC_IMPRINTER_BE.get(),(be,side)->be.getConjoinedItemHandler().orElse(null));
        PortCapabilities.registerBlockEntity(event,PortCapabilities.EnergyStorage.BLOCK,modBlockEntities.GENOME_EXTRACTOR_BE.get(),(be,side)->be.getEnergyHandler());
        PortCapabilities.registerBlockEntity(event,PortCapabilities.EnergyStorage.BLOCK,modBlockEntities.GENETIC_IMPRINTER_BE.get(),(be,side)->be.getEnergyHandler());
    }
    @EventBusSubscriber(modid=MODID,value=Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent public static void screens(RegisterMenuScreensEvent event){
            event.register(modMenuTypes.GENOME_EXTRACTOR_MENU.get(),genomeExtractorScreen::new);
            event.register(modMenuTypes.GENETIC_IMPRINTER_MENU.get(),geneticImprinterScreen::new);
        }
        @SubscribeEvent public static void colors(RegisterColorHandlersEvent.ItemTintSources event){event.register(Identifier.fromNamespaceAndPath(MODID,"gene"),com.thesmartnoob880.genindustry_bees.port.GeneTint.CODEC);}
        @SubscribeEvent public static void properties(RegisterRangeSelectItemModelPropertyEvent event){event.register(Identifier.fromNamespaceAndPath(MODID,"gene_count"),com.thesmartnoob880.genindustry_bees.port.GeneCount.CODEC);}
    }
}
''')
write('port/GeneTint.java','''package com.thesmartnoob880.genindustry_bees.port;
import com.accbdd.complicated_bees.ComplicatedBees;
import com.thesmartnoob880.genindustry_bees.item.geneSampleItem;
import com.thesmartnoob880.genindustry_bees.util.geneColors;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public record GeneTint() implements ItemTintSource {
    public static final MapCodec<GeneTint> CODEC=MapCodec.unit(GeneTint::new);
    public MapCodec<GeneTint> type(){return CODEC;}
    public int calculate(ItemStack stack,ClientLevel level,LivingEntity owner){
        var id=Identifier.tryParse(GeneticData.get(stack).getStringOr(geneSampleItem.KEY_GENE_NAME,""));
        return id==null?0xfff5f5f5:ARGB.opaque(geneColors.GENE_COLORS.getOrDefault(ComplicatedBees.GENE_REGISTRY.get().getValue(id),0xf5f5f5));
    }
}
''')
write('port/GeneCount.java','''package com.thesmartnoob880.genindustry_bees.port;
import com.accbdd.complicated_bees.ComplicatedBees;
import com.thesmartnoob880.genindustry_bees.item.geneticTemplateItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
public record GeneCount() implements RangeSelectItemModelProperty {
    public static final MapCodec<GeneCount> CODEC=MapCodec.unit(GeneCount::new);
    public MapCodec<GeneCount> type(){return CODEC;}
    public float get(ItemStack stack,ClientLevel level,ItemOwner owner,int seed){return geneticTemplateItem.getGeneCount(stack)/(float)Math.max(1,ComplicatedBees.GENE_REGISTRY.get().size());}
}
''')
write('recipe/RecipeGeneticTemplate.java','''package com.thesmartnoob880.genindustry_bees.recipe;
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
''')
write('recipe/GeneWipingFurnaceRecipe.java','''package com.thesmartnoob880.genindustry_bees.recipe;
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
''')
write('recipe/modRecipes.java','''package com.thesmartnoob880.genindustry_bees.recipe;
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
''')
