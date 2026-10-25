package com.thesmartnoob880.genindustry_bees.block.entity;

import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.block.entity.AdaptedItemHandler;
import com.accbdd.complicated_bees.item.*;
import com.accbdd.complicated_bees.util.UpgradeHelper;
import com.thesmartnoob880.genindustry_bees.config.Config;
import com.thesmartnoob880.genindustry_bees.item.geneticTemplateItem;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import com.thesmartnoob880.genindustry_bees.screen.geneticImprinterMenu;
import com.thesmartnoob880.genindustry_bees.util.customEnergyStorage;
import com.thesmartnoob880.genindustry_bees.util.enums.EnumErrorCodesImprinter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.thesmartnoob880.genindustry_bees.port.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import com.accbdd.complicated_bees.port.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.thesmartnoob880.genindustry_bees.item.geneticTemplateItem.isCompleteTemplate;

@SuppressWarnings("ALL")
public class geneticImprinterBlockEntity extends com.accbdd.complicated_bees.port.PortBlockEntity implements MenuProvider {
    private static final int BEE_SLOT=0;
    private static final int TEMPLATE_SLOT=1;
    private static final int OUTPUT_SLOT=0;
    private static final int BASE_RF_COST = Config.imprinterBaseRF;
    private int RF_COST;
    private boolean isCrafting;
    private final customEnergyStorage energy = new customEnergyStorage(Config.imprinterRFCapacity, Config.imprinterRFCapacity/2, 0);
    private final LazyOptional<customEnergyStorage> energyOptional = LazyOptional.of(()-> this.energy);

    private final ItemStackHandler inputItems = this.createInputHandler();
    private ItemStackHandler createInputHandler() {
        return new ItemStackHandler(2) {
            public boolean isItemValid(int slot, @NotNull ItemStack stack){
                return switch (slot) {
                    case BEE_SLOT -> stack.getItem() instanceof PrincessItem ||stack.getItem() instanceof DroneItem;
                    case TEMPLATE_SLOT -> stack.getItem() instanceof geneticTemplateItem;
                    default -> false;
                };
            }
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };
    }

    private final LazyOptional<IItemHandlerModifiable> inputItemHandler = LazyOptional.of(()-> new AdaptedItemHandler(this.inputItems){
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    } );

    public ItemStackHandler getInputItems(){
        return this.inputItems;
    }

    private final ItemStackHandler outputItems = this.createOutputHandler();
    private ItemStackHandler createOutputHandler(){
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                setChanged();
            }
        };
    }

    private final LazyOptional<IItemHandlerModifiable> outputItemHandler = LazyOptional.of(()-> new AdaptedItemHandler(this.outputItems){
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;}
    });

    public ItemStackHandler getOutputItems(){
        return this.outputItems;
    }
    public LazyOptional<IItemHandlerModifiable> getOutputItemHandler(){
        return this.outputItemHandler;
    }

    private final LazyOptional<IItemHandlerModifiable> conjoinedItemHandler = LazyOptional.of(()-> new CombinedInvWrapper((IItemHandlerModifiable)this.outputItemHandler.resolve().get(), (IItemHandlerModifiable)this.inputItemHandler.resolve().get()));
    public LazyOptional<IItemHandlerModifiable> getConjoinedItemHandler(){
        return this.conjoinedItemHandler;
    }
    private final ItemStackHandler upgradeItems = this.createUpgradeHandler();

    protected ItemStackHandler createUpgradeHandler() {
        return new ItemStackHandler(3){
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                geneticImprinterBlockEntity.this.setChanged();
            }

            public int getSlotLimit(int slot) {
                return 1;
            }

            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return switch (slot){
                    case 0, 1, 2 -> stack.getItem() instanceof UpgradeItem;
                    default -> false;
                };
            }
        };
    }

    private final LazyOptional<IItemHandler> upgradeItemHandler = LazyOptional.of(() -> new AdaptedItemHandler(this.upgradeItems) {
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof UpgradeItem;
        }
    });
    public LazyOptional<IItemHandler> getUpgradeItemHandler() {
        return this.upgradeItemHandler;
    }
    public ItemStackHandler getUpgradeItems(){
        return this.upgradeItems;
    }

    private int progress = 0;
    private int maxProgress = this.getMaxProgress();
    private static final int BASE_MAX_PROGRESS = Config.imprinterBaseSpeed;
    protected final ContainerData data;

    public geneticImprinterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(modBlockEntities.GENETIC_IMPRINTER_BE.get(), blockPos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> geneticImprinterBlockEntity.this.progress;
                    case 1 -> geneticImprinterBlockEntity.this.getMaxProgress();
                    case 2 -> geneticImprinterBlockEntity.this.energy.getEnergyStored();
                    case 3 -> geneticImprinterBlockEntity.this.energy.getMaxEnergyStored();
                    case 4 -> geneticImprinterBlockEntity.this.getRFCost();
                    case 5 -> geneticImprinterBlockEntity.this.errorState;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 -> geneticImprinterBlockEntity.this.progress = i1;
                    case 1 -> geneticImprinterBlockEntity.this.setMaxProgress(i1);
                    case 2 -> geneticImprinterBlockEntity.this.energy.setEnergy(i1);
                    case 4 -> geneticImprinterBlockEntity.this.setRFCost(i1);
                    case 5 -> geneticImprinterBlockEntity.this.errorState =i1;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    public customEnergyStorage getEnergyHandler(){return energy;}
    private int errorState = 0;

    public int getRFCost() {
        return this.isCrafting ? this.setRFCost(Math.round((float) BASE_RF_COST / UpgradeHelper.getEfficiencyMod(this.upgradeItems))) : 0;
    }
    public int setRFCost(int RF_COST) {
        return this.RF_COST = RF_COST;
    }

    public int getMaxProgress() {
        return this.setMaxProgress(Math.round((float)BASE_MAX_PROGRESS / UpgradeHelper.getSpeedMod(this.upgradeItems)));
    }
    public int setMaxProgress(int maxProgress){
        return this.maxProgress = maxProgress;
    }





    @Override
    protected void saveAdditional(CompoundTag psaveAdd, net.minecraft.core.HolderLookup.Provider registries) {
        psaveAdd.put("output_inventory", this.getOutputItems().serializeNBT(registries));
        psaveAdd.put("input_inventory", this.getInputItems().serializeNBT(registries));
        psaveAdd.putInt("genetic_imprinter.progress", this.progress);
        psaveAdd.putInt("energy",this.energy.getEnergyStored());
        psaveAdd.put("upgrade_inventory", this.upgradeItems.serializeNBT(registries));
        super.saveAdditional(psaveAdd, registries);
    }

    @Override
    public void loadAdditional(CompoundTag pLoad, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pLoad,registries);
        if (pLoad.contains("output_inventory")) {
            this.getOutputItems().deserializeNBT(registries,pLoad.getCompoundOrEmpty("output_inventory"));
        }
        if (pLoad.contains("input_inventory")) {
            this.getInputItems().deserializeNBT(registries,pLoad.getCompoundOrEmpty("input_inventory"));
        }
        if (pLoad.contains("genetic_imprinter.progress")) {
            this.progress = pLoad.getIntOr("genetic_imprinter.progress", 0);
        }
        if (pLoad.contains("energy")) {
            this.energy.setEnergy(pLoad.getIntOr("energy", 0));
        }
        if (pLoad.contains("upgrade_inventory")) {
            this.upgradeItems.deserializeNBT(registries,pLoad.getCompoundOrEmpty("upgrade_inventory"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.genindustry_bees.genetic_imprinter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new geneticImprinterMenu(i, inventory, this, this.data);
    }

    private boolean hasProgressFinished() {
        return progress >= this.getMaxProgress();
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private void lowerProgress() {
        this.isCrafting = false;
        if (this.progress > 0) {
            --this.progress;
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.energy != null){
            if (hasRecipe()&&isOutputFull()) {
                if (outputMatches()){
                    if (this.energy.getEnergyStored()> this.getRFCost()){
                        removeImprinterError(EnumErrorCodesImprinter.NO_RF);
                        this.isCrafting = true;
                        this.energy.removeEnergy(this.getRFCost());
                        this.increaseCraftingProgress();
                        setChanged(pLevel, pPos, pState);
                        if(hasProgressFinished()){
                            this.craft();
                            this.resetProgress();
                            this.sendUpdate();
                        }
                    } else {
                        addImprinterError(EnumErrorCodesImprinter.NO_RF);
                        this.lowerProgress();
                    }
                } else {
                    this.resetProgress();
                }
            } else if (this.progress > 0){
                this.resetProgress();
            }
        }
        updateActiveState(pPos ,pState);
    }

    private void updateActiveState(BlockPos pos, BlockState state) {
        boolean poweredState = state.getValue(BlockStateProperties.POWERED);
        if (poweredState != this.isCrafting){
            this.level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, this.isCrafting));
        }
        if (getImprinterErrors() > 0){
            this.level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }
    }

    private void resetProgress() {
        progress=0;
    }

    private ItemStack getCraftedItem() {
        ItemStack originalBee = this.getInputItems().getStackInSlot(BEE_SLOT).copyWithCount(1);
        CompoundTag templateData = com.thesmartnoob880.genindustry_bees.port.GeneticData.get(this.getInputItems().getStackInSlot(TEMPLATE_SLOT));
        if(templateData == null||originalBee.isEmpty()){
            return ItemStack.EMPTY;
        }
        var chromosome=com.accbdd.complicated_bees.bees.Chromosome.deserialize(templateData);
        com.accbdd.complicated_bees.bees.GeneticHelper.setGenome(originalBee,chromosome);

        return originalBee.copyWithCount(1);
    }

    private void craft(){
        ItemStack output = this.getCraftedItem();
        this.getInputItems().getStackInSlot(BEE_SLOT).shrink(1);
        ItemStack outputSlotContents = this.getOutputItems().getStackInSlot(OUTPUT_SLOT);
        if (output.equals(outputSlotContents)){
            outputSlotContents.grow(output.getCount());
            this.getOutputItems().setStackInSlot(OUTPUT_SLOT,outputSlotContents);
        }else {
            this.getOutputItems().insertItem(0, output, false);
        }
    }

    private boolean hasRecipe() {
        if (!this.getInputItems().getStackInSlot(BEE_SLOT).isEmpty()){
            removeImprinterError(EnumErrorCodesImprinter.NO_BEES);
        }else{
            addImprinterError(EnumErrorCodesImprinter.NO_BEES);
        }

        if (!this.getInputItems().getStackInSlot(TEMPLATE_SLOT).isEmpty() && isCompleteTemplate(this.getInputItems().getStackInSlot(TEMPLATE_SLOT))){
            removeImprinterError(EnumErrorCodesImprinter.NO_TEMPLATE);
        }else{
            addImprinterError(EnumErrorCodesImprinter.NO_TEMPLATE);
        }

        return (this.getInputItems().getStackInSlot(BEE_SLOT).getItem() instanceof BeeItem beeItem && !(beeItem instanceof QueenItem))
                && (this.getInputItems().getStackInSlot(TEMPLATE_SLOT).getItem() == modItems.GENETIC_TEMPLATE.get());
    }
    public LazyOptional<customEnergyStorage> getEnergyOptional(){
        return this.energyOptional;
    }
    public customEnergyStorage getEnergy(){
        return this.energy;
    }
    private void sendUpdate(){
        setChanged();
        if (this.level != null)
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private boolean outputMatches(){
        ItemStack outputItem= this.getOutputItems().getStackInSlot(OUTPUT_SLOT);
        ItemStack craftedresult =this.getCraftedItem();
        return outputItem.isEmpty() || ItemStack.isSameItemSameComponents(outputItem,craftedresult);
    }
    private boolean isOutputFull(){
        return this.getOutputItems().getStackInSlot(OUTPUT_SLOT).getCount() < this.getOutputItems().getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    public boolean hasTemplateItem(){
        return this.getInputItems().getStackInSlot(1).is(modItems.GENETIC_TEMPLATE.get());
    }

    public void addImprinterError(EnumErrorCodesImprinter...error){
        for (EnumErrorCodesImprinter err : error){
            this.errorState |= err.value;
        }
    }

    public void removeImprinterError(EnumErrorCodesImprinter...error){
        for (EnumErrorCodesImprinter err : error) {
            this.errorState &= err.value ^ Integer.MAX_VALUE;
        }
    }

    public int getImprinterErrors(){
        return this.data.get(5);
    }
}
