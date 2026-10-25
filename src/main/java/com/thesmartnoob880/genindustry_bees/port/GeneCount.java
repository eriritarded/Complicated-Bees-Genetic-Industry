package com.thesmartnoob880.genindustry_bees.port;
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
