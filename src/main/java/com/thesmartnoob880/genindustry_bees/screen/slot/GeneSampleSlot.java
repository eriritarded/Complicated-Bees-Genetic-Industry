package com.thesmartnoob880.genindustry_bees.screen.slot;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static com.thesmartnoob880.genindustry_bees.item.geneSampleItem.KEY_GENE_NAME;
import static com.thesmartnoob880.genindustry_bees.item.geneSampleItem.KEY_GENE_VALUE;

public class GeneSampleSlot extends SlotItemHandler {
    private final TagKey<Item> itemTagKey;

    public GeneSampleSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, TagKey<Item> tagKey) {
        super(itemHandler, index, xPosition, yPosition);
        this.itemTagKey = tagKey;
    }

    public boolean mayPlace(ItemStack stack) {
        if (stack.is(this.itemTagKey)){
            if (!com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
                return true;
            }
            assert com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack) !=null;
            return !(com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_NAME) && com.thesmartnoob880.genindustry_bees.port.GeneticData.get(stack).contains(KEY_GENE_VALUE));
        } else {
            return false;
        }
    }
}
