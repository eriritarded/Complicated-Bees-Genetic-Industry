package com.thesmartnoob880.genindustry_bees.compat.jei;

import com.accbdd.complicated_bees.bees.GeneticHelper;
import com.accbdd.complicated_bees.bees.Species;
import com.accbdd.complicated_bees.registry.ItemsRegistration;
import com.accbdd.complicated_bees.registry.SpeciesRegistration;
import com.thesmartnoob880.genindustry_bees.item.geneSampleItem;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import com.thesmartnoob880.genindustry_bees.util.labwareHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JEIChromosomes {
    public static List<ItemStack> validLabware(){
        return labwareHelper.getItems().stream().map(Item::getDefaultInstance).toList();
    }

    public static List<ItemStack> validBees(Species species){
        List<ItemStack> bees = new ArrayList<>();
        bees.add(species.toStack(ItemsRegistration.PRINCESS.get()));
        bees.add(species.toStack(ItemsRegistration.DRONE.get()));
        return bees;
    }

    public static List<ItemStack> possibleGenes(Species species){
        return species.getDefaultChromosome().getGenes().entrySet().stream().map((entry)->{
            ItemStack sample = new ItemStack(modItems.GENE_SAMPLE.get());
            CompoundTag root = new CompoundTag();
            root.putString(geneSampleItem.KEY_GENE_NAME, entry.getKey().toString());
            root.put(geneSampleItem.KEY_GENE_VALUE, entry.getValue().serialize());
            com.thesmartnoob880.genindustry_bees.port.GeneticData.set(sample,root);
            return sample;
        }).toList();
    }

    public static ItemStack blankGene(){
        return new ItemStack(modItems.GENE_SAMPLE.get());
    }

    public static List<ItemStack> allValidBees(){
        List<ItemStack> bees = new ArrayList<>();
        GeneticHelper.getRegistryAccess().lookup(SpeciesRegistration.SPECIES_REGISTRY_KEY).get().forEach((species -> bees.addAll(validBees(species))));
        return bees;
    }

    public static ItemStack completeTemplate(Species species){
        CompoundTag root = species.getDefaultChromosome().serialize();
        ItemStack result = modItems.GENETIC_TEMPLATE.get().getDefaultInstance();
        com.thesmartnoob880.genindustry_bees.port.GeneticData.set(result,root);
        return result.copy();
    }
}
