package com.thesmartnoob880.genindustry_bees.block;

import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

import java.util.function.Supplier;

public class modBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            new com.accbdd.complicated_bees.port.RegistrationContext.Blocks(GenIndustryBees.MODID);

    public static final Supplier<Block> GENOME_EXTRACTOR = registerBlock("genome_extractor",
            ()-> new genomeExtractorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(com.accbdd.complicated_bees.port.RegistrationContext.blockKey()).noOcclusion()));
    public static final Supplier<Block> GENETIC_IMPRINTER = registerBlock("genetic_imprinter",
            ()-> new geneticImprinterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(com.accbdd.complicated_bees.port.RegistrationContext.blockKey())));

    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, Supplier<T> block) {
        modItems.ITEMS.register(name, () -> new BlockItem(block.get(), com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
