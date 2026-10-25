package com.thesmartnoob880.genindustry_bees.block.entity;

import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

public class modBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, GenIndustryBees.MODID);

    public static final Supplier<BlockEntityType<genomeExtractorBlockEntity>> GENOME_EXTRACTOR_BE =
            BLOCK_ENTITIES.register("genome_extractor_be", ()->
                    new BlockEntityType<>(genomeExtractorBlockEntity::new,
                            modBlocks.GENOME_EXTRACTOR.get()));
    public static final Supplier<BlockEntityType<geneticImprinterBlockEntity>> GENETIC_IMPRINTER_BE =
            BLOCK_ENTITIES.register("genetic_imprinter_be", ()->
                    new BlockEntityType<>(geneticImprinterBlockEntity::new,
                            modBlocks.GENETIC_IMPRINTER.get()));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
