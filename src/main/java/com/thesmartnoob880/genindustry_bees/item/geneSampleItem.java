package com.thesmartnoob880.genindustry_bees.item;

import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.bees.gene.GeneTolerant;
import com.accbdd.complicated_bees.bees.gene.IGene;
import com.thesmartnoob880.genindustry_bees.util.geneColors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class geneSampleItem extends Item implements IGeneticHolder{
    public geneSampleItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, TooltipContext p_41422_, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> ptooltipComponents, TooltipFlag p_41424_) {

        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack) != null && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_NAME) && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_VALUE)){
            String transKey = "gene."+com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).getStringOr(KEY_GENE_NAME, "").replace(":", ".");
            IGene<?> defaultGene = ComplicatedBees.GENE_REGISTRY.get().getValue(Identifier.parse(com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).getStringOr(KEY_GENE_NAME, "")));
            IGene<?> gene = defaultGene.deserialize(com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).getCompoundOrEmpty(KEY_GENE_VALUE));
            MutableComponent value = gene.getTranslationKey();
            if (gene instanceof GeneTolerant<?> tolerantGene)
                value = value.append(" / ").append(tolerantGene.getTolerance().getTranslationKey());
            ptooltipComponents.accept(Component.translatable(transKey+"_label").append(": ").append(value).withStyle(Style.EMPTY.withColor(geneColors.GENE_COLORS.get(defaultGene))));

            ptooltipComponents.accept(Component.translatable("item.genindustry_bees.gene_sample.dominance."+ gene.isDominant()).withStyle(Style.EMPTY.withColor(gene.isDominant() ? 15086117 : 2457574)));
        }
    }
    public static final String KEY_GENE_NAME = "geneKey";
    public static final String KEY_GENE_VALUE = "geneValue";

    public static boolean hasGeneData(ItemStack stack){
        return com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack) != null && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_NAME) && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_VALUE);
    }

    @Override
    public @org.jetbrains.annotations.NotNull Component getName(ItemStack stack) {
        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
            if (com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_NAME) && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_VALUE)){
                return Component.translatable("item.genindustry_bees.gene_sample");
            } else {
                return Component.translatable("item.genindustry_bees.gene_sample.blank");
            }
        }else{
            return Component.translatable("item.genindustry_bees.gene_sample.blank");
        }

    }

    public static ItemStack createGene(Identifier location, IGene<?> igene){
        CompoundTag root = new CompoundTag();

        root.putString(KEY_GENE_NAME, location.toString());
        root.put(KEY_GENE_VALUE, igene.serialize());
        ItemStack result = new ItemStack(modItems.GENE_SAMPLE.get(), 1);
        com.thesmartnoob880.genindustry_bees.port.GeneticData.set(result,root);
        return result;
    }

    @Override
    public boolean hasGeneticData(ItemStack stack) {
        return hasGeneData(stack);
    }
}
