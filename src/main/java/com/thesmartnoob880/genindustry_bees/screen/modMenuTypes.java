package com.thesmartnoob880.genindustry_bees.screen;

import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

public class modMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, GenIndustryBees.MODID);

    public static final Supplier<MenuType<genomeExtractorMenu>> GENOME_EXTRACTOR_MENU =
            registerMenuType("genome_extractor_menu", genomeExtractorMenu::new);
    public static final Supplier<MenuType<geneticImprinterMenu>> GENETIC_IMPRINTER_MENU =
            registerMenuType("genetic_imprinter_menu", geneticImprinterMenu::new);

    private static <T extends AbstractContainerMenu>Supplier<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, ()-> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}
