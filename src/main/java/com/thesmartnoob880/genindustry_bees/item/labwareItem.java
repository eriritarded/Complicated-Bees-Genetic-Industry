package com.thesmartnoob880.genindustry_bees.item;

import com.thesmartnoob880.genindustry_bees.util.labwareHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class labwareItem extends Item {
    public labwareItem(Item.Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack pStack, TooltipContext p_41422_, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> pTooltip, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(pStack, p_41422_, display, pTooltip, p_41424_);
        pTooltip.accept(Component.translatable("item.genindustry_bees.labware.labware_tooltip", labwareHelper.getLabwareRate(pStack.getItem())*100).append("%"));
    }
}
