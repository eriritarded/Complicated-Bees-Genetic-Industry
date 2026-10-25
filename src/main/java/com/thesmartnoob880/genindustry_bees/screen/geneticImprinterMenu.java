package com.thesmartnoob880.genindustry_bees.screen;

import com.accbdd.complicated_bees.datagen.ItemTagGenerator;
import com.accbdd.complicated_bees.screen.slot.OutputSlot;
import com.accbdd.complicated_bees.screen.slot.TagSlot;
import com.thesmartnoob880.genindustry_bees.block.entity.geneticImprinterBlockEntity;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import com.thesmartnoob880.genindustry_bees.screen.slot.ValidTemplateItemSlot;
import com.thesmartnoob880.genindustry_bees.util.modTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;


@SuppressWarnings("ConstantValue")
public class geneticImprinterMenu extends AbstractContainerMenu {
    private final Level level;
    private final ContainerData data;
    private final BlockPos pos;

    public geneticImprinterMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData){
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public geneticImprinterMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(modMenuTypes.GENETIC_IMPRINTER_MENU.get(), pContainerId);
        checkContainerSize(inv, 6);

        this.level = inv.player.level();
        this.data = data;
        this.pos = entity.getBlockPos();
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        if (entity instanceof geneticImprinterBlockEntity imprinter){
            //input slots
            this.addSlot(new TagSlot(imprinter.getInputItems(), 0, 17, 25,ItemTagGenerator.BEE));
            this.addSlot(new ValidTemplateItemSlot(imprinter.getInputItems(), 1, 45, 25, modTags.Items.GENETIC_TEMPLATE));
            //upgrade slots
            this.addSlot(new SlotItemHandler(imprinter.getUpgradeItems(), 0, 60, 55));
            this.addSlot(new SlotItemHandler(imprinter.getUpgradeItems(), 1, 80, 55));
            this.addSlot(new SlotItemHandler(imprinter.getUpgradeItems(), 2, 100, 55));
            //output slot
            this.addSlot(new OutputSlot(imprinter.getOutputItems(), 0, 115, 25));
        }
        addDataSlots(data);
    }

    public boolean isCrafting(){
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress =this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 20;
        return maxProgress !=0 && progress !=0 ? progress * progressArrowSize / maxProgress : 0;
    }
    public int getEnergy(){
        return this.data.get(2);
    }

    public int getMaxEnergy(){
        return this.data.get(3);
    }

    public int getEnergyScaled(){
        return (int) ((float)getEnergy()/(float)getMaxEnergy() * 61);
    }

    public int getEnergyPerTick(){
        return this.data.get(4);
    }

    public ContainerData getImprinterData(){
        return this.data;
    }



    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = 6;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, this.pos),
                player, modBlocks.GENETIC_IMPRINTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 76 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 134));
        }
    }
}
