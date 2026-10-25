package com.thesmartnoob880.genindustry_bees.screen;

import com.accbdd.complicated_bees.datagen.ItemTagGenerator;
import com.accbdd.complicated_bees.screen.slot.OutputSlot;
import com.accbdd.complicated_bees.screen.slot.TagSlot;
import com.thesmartnoob880.genindustry_bees.block.entity.genomeExtractorBlockEntity;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import com.thesmartnoob880.genindustry_bees.screen.slot.GeneSampleSlot;
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
public class genomeExtractorMenu extends AbstractContainerMenu {

    private final Level level;
    private final ContainerData data;
    private final BlockPos pos;
    @SuppressWarnings({"resource", "DataFlowIssue"})
    public genomeExtractorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData){
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public genomeExtractorMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data){
        super(modMenuTypes.GENOME_EXTRACTOR_MENU.get(), pContainerId);
        checkContainerSize(inv, 15);

        this.level = inv.player.level();
        this.data = data;
        this.pos = entity.getBlockPos();
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        if (entity instanceof  genomeExtractorBlockEntity extractor){
            //input slots
            this.addSlot(new TagSlot(extractor.getInputItems(), 0, 28, 27, modTags.Items.LABWARE));
            this.addSlot(new TagSlot(extractor.getInputItems(), 1, 14, 53, ItemTagGenerator.BEE));
            this.addSlot(new GeneSampleSlot(extractor.getInputItems(), 2, 42, 53, modTags.Items.GENE_SAMPLE));
            //upgrade slots
            this.addSlot(new SlotItemHandler(extractor.getUpgradeItems(), 0, 8, 83));
            this.addSlot(new SlotItemHandler(extractor.getUpgradeItems(), 1, 28, 83));
            this.addSlot(new SlotItemHandler(extractor.getUpgradeItems(), 2, 48, 83));
            //output slots
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 0, 89, 20));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 1, 114, 20));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 2, 139, 20));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 3, 89, 45));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 4, 114, 45));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 5, 139, 45));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 6, 89, 70));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 7, 114, 70));
            this.addSlot(new OutputSlot(extractor.getOutputItems(), 8, 139, 70));

        addDataSlots(data);
        }
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
        return (int) ((float)getEnergy()/(float)getMaxEnergy() * 75);
    }

    public int getEnergyPerTick(){
        return this.data.get(4);
    }

    public ContainerData getExtractorData() {
        return this.data;
    }


    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 15;

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
                player, modBlocks.GENOME_EXTRACTOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 105 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 163));
        }
    }
}
