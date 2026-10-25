package com.thesmartnoob880.genindustry_bees.item;

import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.bees.gene.IGene;
import com.accbdd.complicated_bees.registry.SpeciesRegistration;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class modCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GenIndustryBees.MODID);

    public static final Supplier<CreativeModeTab> MOD_ITEMS = CREATIVE_MODE_TABS.register("mod_items", () -> CreativeModeTab.builder().icon(() -> new ItemStack(modBlocks.GENOME_EXTRACTOR.get()))
            .title(Component.translatable("creativeTab."+ GenIndustryBees.MODID+".modItems"))
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(modItems.GENETIC_TEMPLATE.get());
                pOutput.accept(modItems.GENE_SAMPLE.get());
                pOutput.accept(modItems.HONEY_COMB_INGOT.get());
                pOutput.accept(modItems.ROYAL_INGOT.get());
                pOutput.accept(modItems.BASIC_LABWARE.get());
                pOutput.accept(modItems.IMPROVED_LABWARE.get());
                pOutput.accept(modItems.ADVANCED_LABWARE.get());

                pOutput.accept(modBlocks.GENOME_EXTRACTOR.get());
                pOutput.accept(modBlocks.GENETIC_IMPRINTER.get());
            })
            .build());

    public static final Supplier<CreativeModeTab> POSSIBLE_SAMPLES = CREATIVE_MODE_TABS.register("gene_creative_tab", ()-> CreativeModeTab.builder().icon(()-> new ItemStack(modItems.GENE_SAMPLE.get()))
            .title(Component.translatable("creativeTab."+ GenIndustryBees.MODID+".possibleSamples"))
            .displayItems((pParameters, pOutput)-> {
                HashMap<Identifier, List<IGene<?>>> uniqueGenes = new HashMap<>();
                ComplicatedBees.GENE_REGISTRY.get().keySet().forEach((type)-> uniqueGenes.put(type, new ArrayList<>()));
                pParameters.holders().lookup(SpeciesRegistration.SPECIES_REGISTRY_KEY).get().listElements().map(Holder.Reference::value).forEach((species)-> species.getDefaultChromosome().getGenes().forEach((type, gene)->{
                    List<IGene<?>> UG = uniqueGenes.get(type);

                    if (!UG.contains(gene)){
                        UG.add(gene);
                    }
                }));
                uniqueGenes.forEach((type,genes)-> pOutput.acceptAll(genes.stream().map((gene)-> geneSampleItem.createGene(type, gene)).toList()));
            })
            .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
