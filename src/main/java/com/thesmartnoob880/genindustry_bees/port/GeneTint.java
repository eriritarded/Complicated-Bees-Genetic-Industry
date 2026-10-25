package com.thesmartnoob880.genindustry_bees.port;
import com.accbdd.complicated_bees.ComplicatedBees;
import com.thesmartnoob880.genindustry_bees.item.geneSampleItem;
import com.thesmartnoob880.genindustry_bees.util.geneColors;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public record GeneTint() implements ItemTintSource {
    public static final MapCodec<GeneTint> CODEC=MapCodec.unit(GeneTint::new);
    public MapCodec<GeneTint> type(){return CODEC;}
    public int calculate(ItemStack stack,ClientLevel level,LivingEntity owner){
        var id=Identifier.tryParse(GeneticData.get(stack).getStringOr(geneSampleItem.KEY_GENE_NAME,""));
        return id==null?0xfff5f5f5:ARGB.opaque(geneColors.GENE_COLORS.getOrDefault(ComplicatedBees.GENE_REGISTRY.get().getValue(id),0xf5f5f5));
    }
}
