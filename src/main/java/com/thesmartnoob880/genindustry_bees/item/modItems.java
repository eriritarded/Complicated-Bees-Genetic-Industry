package com.thesmartnoob880.genindustry_bees.item;

import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

public class modItems {
    public static final DeferredRegister.Items ITEMS =
            new com.accbdd.complicated_bees.port.RegistrationContext.Items(GenIndustryBees.MODID);

    public static final Supplier<geneSampleItem> GENE_SAMPLE = ITEMS.register("gene_sample", () -> new geneSampleItem(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()));
    public static final Supplier<geneticTemplateItem> GENETIC_TEMPLATE = ITEMS.register("genetic_template", () -> new geneticTemplateItem(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()));
    public static final Supplier<labwareItem> BASIC_LABWARE = ITEMS.register("basic_labware", () -> new labwareItem(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties().stacksTo(8)));
    public static final Supplier<labwareItem> IMPROVED_LABWARE = ITEMS.register("improved_labware", () -> new labwareItem(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties().durability(16)));
    public static final Supplier<labwareItem> ADVANCED_LABWARE = ITEMS.register("advanced_labware", () -> new labwareItem(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties().durability(64).fireResistant()));
    public static final Supplier<Item> HONEY_COMB_INGOT = ITEMS.register("honey_comb_ingot", () -> new Item(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()));
    public static final Supplier<Item> ROYAL_INGOT = ITEMS.register("royal_ingot", () -> new Item(com.accbdd.complicated_bees.port.RegistrationContext.itemProperties().fireResistant()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
