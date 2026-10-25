package com.thesmartnoob880.genindustry_bees;
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
