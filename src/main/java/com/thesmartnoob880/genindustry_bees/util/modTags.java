package com.thesmartnoob880.genindustry_bees.util;

import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class modTags {

    public static class Items{
        public static final TagKey<Item> LABWARE = tag("labware_items");
        public static final TagKey<Item> GENE_SAMPLE = tag("gene_sample");
        public static final TagKey<Item> GENETIC_TEMPLATE = tag("genetic_template");
        private static TagKey<Item> tag(String name){
            return ItemTags.create(Identifier.fromNamespaceAndPath(GenIndustryBees.MODID, name));
        }
    }
}
