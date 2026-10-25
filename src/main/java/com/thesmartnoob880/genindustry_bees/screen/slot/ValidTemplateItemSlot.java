package com.thesmartnoob880.genindustry_bees.screen.slot;

import com.thesmartnoob880.genindustry_bees.item.geneticTemplateItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static com.thesmartnoob880.genindustry_bees.item.geneticTemplateItem.isCompleteTemplate;

public class ValidTemplateItemSlot extends SlotItemHandler {
    private final TagKey<Item> tag;
    public ValidTemplateItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, TagKey<Item> itemTagKey) {
        super(itemHandler, index, xPosition, yPosition);
        this.tag = itemTagKey;
    }

    public boolean mayPlace(ItemStack stack) {
        if (stack.is(this.tag)){
            if (!com.thesmartnoob880.genindustry_bees.port.GeneticData.has(stack)){
                return false;
            }
            assert stack.getItem() instanceof geneticTemplateItem;
            return isCompleteTemplate(stack);
        }
        return false;
    }
}
