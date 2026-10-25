package com.thesmartnoob880.genindustry_bees.util;

import com.accbdd.complicated_bees.bees.gene.IGene;
import com.accbdd.complicated_bees.registry.GeneRegistration;
import net.minecraft.ChatFormatting;
import java.util.HashMap;

public class geneColors {

    public static void initalLoad(){

    }
    public static final HashMap<IGene<?>, Integer> GENE_COLORS =new HashMap<>();

    static {
        GENE_COLORS.put(GeneRegistration.ACTIVE_TIME.get(), 0Xffff00);
        GENE_COLORS.put(GeneRegistration.CAVE_DWELLING.get(), 0x555555);
        GENE_COLORS.put(GeneRegistration.EFFECT.get(), 0x05fcf4);
        GENE_COLORS.put(GeneRegistration.FERTILITY.get(), 0xff03f2);
        GENE_COLORS.put(GeneRegistration.FLOWER.get(), 0x00ff00);
        GENE_COLORS.put(GeneRegistration.HUMIDITY.get(), 0xe05b22);
        GENE_COLORS.put(GeneRegistration.LIFESPAN.get(), 0xffffff);
        GENE_COLORS.put(GeneRegistration.PRODUCTIVITY.get(), 0xb2f016);
        GENE_COLORS.put(GeneRegistration.SPECIES.get(), 0xFFAA00);
        GENE_COLORS.put(GeneRegistration.TEMPERATURE.get(), 0xff0000);
        GENE_COLORS.put(GeneRegistration.TERRITORY.get(), 0x892ed9);
        GENE_COLORS.put(GeneRegistration.WEATHERPROOF.get(), 0x0000ff);

    }
}
