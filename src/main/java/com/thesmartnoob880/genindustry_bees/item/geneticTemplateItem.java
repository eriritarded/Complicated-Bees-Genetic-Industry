package com.thesmartnoob880.genindustry_bees.item;

import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.bees.gene.GeneTolerant;
import com.accbdd.complicated_bees.bees.gene.IGene;
import com.thesmartnoob880.genindustry_bees.util.geneColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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

public class geneticTemplateItem extends Item implements IGeneticHolder{
    public geneticTemplateItem(Properties properties) {
        super(properties);
    }

    public static int getGeneCount(ItemStack stack){
        int count = 0;
        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
            for (Identifier geneResourceKey : ComplicatedBees.GENE_REGISTRY.get().keySet()) {
                if (com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(geneResourceKey.toString())){
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean containsOneOrMoreGenes(ItemStack stack){
        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
            for (Identifier geneResourceKey : ComplicatedBees.GENE_REGISTRY.get().keySet()) {
                String geneKey = geneResourceKey.toString();
                if (com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(geneKey))
                    return true;
            }
        }
        return false;
    }

    public static boolean isCompleteTemplate(ItemStack stack){
        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
            for (Identifier geneResourceKey : ComplicatedBees.GENE_REGISTRY.get().keySet()){
                String geneKey = geneResourceKey.toString();
                if (!com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(geneKey)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext plevel, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> ptooltipComponents, @NotNull TooltipFlag pFlag) {
       if (!com.thesmartnoob880.genindustry_bees.port.GeneticData.has(pStack)) return;
       boolean heldShift = net.minecraft.client.Minecraft.getInstance().hasShiftDown();
           ComplicatedBees.GENE_REGISTRY.get().entrySet().forEach((entry)->{
               String geneKey = entry.getKey().identifier().toString();
               String transKey = "gene."+geneKey.replace(":", ".");
               if (com.thesmartnoob880.genindustry_bees.port.GeneticData.get(pStack).contains(geneKey)){
                   IGene<?> deseralizedGene = entry.getValue().deserialize(com.thesmartnoob880.genindustry_bees.port.GeneticData.get(pStack).getCompoundOrEmpty(geneKey));
                   MutableComponent value = deseralizedGene.getTranslationKey();
                   if (heldShift)
                       value = Component.translatable("item.genindustry_bees.genetic_template.dominance."+deseralizedGene.isDominant()).withStyle(Style.EMPTY.withColor(deseralizedGene.isDominant() ? 15086117 : 2457574));
                   else if (deseralizedGene instanceof GeneTolerant<?> tolerantGene)
                       value = value.append(" / ").append(tolerantGene.getTolerance().getTranslationKey());
                   ptooltipComponents.accept(Component.translatable(transKey+"_label").append(": ").append(value).withStyle(Style.EMPTY.withColor(geneColors.GENE_COLORS.get(entry.getValue()))));
               } else if (heldShift) {
                   ptooltipComponents.accept(Component.translatable(transKey+"_label").append(": ")
                           .withStyle(Style.EMPTY.withColor(geneColors.GENE_COLORS.get(entry.getValue())))
                           .append(Component.translatable("item.genindustry_bees.genetic_template.empty").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY)));
               }
           });
           if (!heldShift&&containsOneOrMoreGenes(pStack)) {
               ptooltipComponents.accept(Component.translatable("item.genindustry_bees.genetic_template.more_info").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
           }
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        if (com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
            if (isCompleteTemplate(stack)){
                return Component.translatable("item.genindustry_bees.genetic_template.complete");
            } else if (containsOneOrMoreGenes(stack)){
                return Component.translatable("item.genindustry_bees.genetic_template.incomplete");
            } else{
                return Component.translatable("item.genindustry_bees.genetic_template.blank");
            }
        } else{
            return Component.translatable("item.genindustry_bees.genetic_template.blank");
        }
    }

    @Override
    public boolean hasGeneticData(ItemStack stack) {
        return containsOneOrMoreGenes(stack);
    }
}