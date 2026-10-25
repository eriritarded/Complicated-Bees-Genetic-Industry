package com.thesmartnoob880.genindustry_bees.block.entity;

import com.accbdd.complicated_bees.bees.Chromosome;
import com.accbdd.complicated_bees.bees.GeneticHelper;
import com.accbdd.complicated_bees.bees.gene.IGene;
import com.accbdd.complicated_bees.block.entity.AdaptedItemHandler;
import com.accbdd.complicated_bees.item.*;
import com.accbdd.complicated_bees.util.UpgradeHelper;
import com.thesmartnoob880.genindustry_bees.config.Config;
import com.thesmartnoob880.genindustry_bees.item.geneSampleItem;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import com.thesmartnoob880.genindustry_bees.screen.genomeExtractorMenu;
import com.thesmartnoob880.genindustry_bees.util.customEnergyStorage;
import com.thesmartnoob880.genindustry_bees.util.enums.EnumErrorCodesExtractor;
import com.thesmartnoob880.genindustry_bees.util.labwareHelper;
import com.thesmartnoob880.genindustry_bees.util.modTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

import java.util.*;

@SuppressWarnings("ALL")
public class genomeExtractorBlockEntity extends com.accbdd.complicated_bees.port.PortBlockEntity implements MenuProvider {
    private static final int LABWARE_SLOT = 0;
    private static final int BEE_SLOT = 1;
    private static final int GENE_SLOT = 2;
    private static final List<Integer> OUTPUT_SLOTS = List.of(0,1,2,3,4,5,6,7,8);
    private boolean isCrafting;
    private final Random random=new Random();

    private final customEnergyStorage energy = new customEnergyStorage(Config.extractorRFCapacity, Config.extractorRFCapacity/2, 0);
    private final LazyOptional<customEnergyStorage> energyOptional = LazyOptional.of(()-> this.energy);
    private static final int BASE_RF_COST = Config.extractorBaseRF;
    private int RF_COST;

    protected final ContainerData data;
    private final ItemStackHandler upgradeItems = this.createUpgradeHandler();
    private int progress = 0;
    private int maxProgress = this.getMaxProgress();
    private static final int BASE_MAX_PROGRESS = Config.extractorBaseSpeed;

    private final ItemStackHandler inputItems = this.createInputHandler();
    private ItemStackHandler createInputHandler(){
        return new ItemStackHandler(3){
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return switch (slot) {
                    case LABWARE_SLOT -> labwareHelper.getLabwareRate(stack.getItem())>0.0;
                    case BEE_SLOT -> stack.getItem() instanceof PrincessItem||stack.getItem() instanceof DroneItem;
                    case GENE_SLOT -> stack.getItem() instanceof geneSampleItem && !(geneSampleItem.hasGeneData(stack));
                    default -> false;
                };
            }
            protected void onContentsChanged(int slot){
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
    private ItemStackHandler createOutputHandler() {
        return new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot){
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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private final LazyOptional<IItemHandlerModifiable> conjoinedItemHandler = LazyOptional.of(()-> new CombinedInvWrapper(this.outputItemHandler.resolve().get(), this.inputItemHandler.resolve().get()));
    public LazyOptional<IItemHandlerModifiable> getConjoinedItemHandler(){
        return this.conjoinedItemHandler;
    }

    protected ItemStackHandler createUpgradeHandler(){
        return new ItemStackHandler(3) {
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                genomeExtractorBlockEntity.this.setChanged();
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

    public genomeExtractorBlockEntity(BlockPos pBlockPos, BlockState pBlockState) {
        super(modBlockEntities.GENOME_EXTRACTOR_BE.get(), pBlockPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> genomeExtractorBlockEntity.this.progress;
                    case 1 -> genomeExtractorBlockEntity.this.getMaxProgress();
                    case 2 -> genomeExtractorBlockEntity.this.energy.getEnergyStored();
                    case 3 -> genomeExtractorBlockEntity.this.energy.getMaxEnergyStored();
                    case 4 -> genomeExtractorBlockEntity.this.getRFCost();
                    case 5 -> genomeExtractorBlockEntity.this.errorState;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 -> genomeExtractorBlockEntity.this.progress = i1;
                    case 1 -> genomeExtractorBlockEntity.this.setMaxProgress(i1);
                    case 2 -> genomeExtractorBlockEntity.this.energy.setEnergy(i1);
                    case 4 -> genomeExtractorBlockEntity.this.setRFCost(i1);
                    case 5 -> genomeExtractorBlockEntity.this.errorState = i1;
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

    public int getMaxProgress() {
        return this.setMaxProgress(Math.round((float) BASE_MAX_PROGRESS / UpgradeHelper.getSpeedMod(this.upgradeItems)));
    }

    public int setMaxProgress(int maxProgress) {
        return this.maxProgress = maxProgress;
    }

    public int getRFCost() {
        return this.isCrafting ? this.setRFCost(Math.round((float) BASE_RF_COST / UpgradeHelper.getEfficiencyMod(this.upgradeItems))) : 0;
    }
    public int setRFCost(int RF_COST) {
        return this.RF_COST = RF_COST;
    }





    @Override
    protected void saveAdditional(CompoundTag psaveAdd, net.minecraft.core.HolderLookup.Provider registries) {
        psaveAdd.put("output_inventory", this.getOutputItems().serializeNBT(registries));
        psaveAdd.put("input_inventory", this.getInputItems().serializeNBT(registries));
        psaveAdd.putInt("genome_extractor.progress", progress);
        psaveAdd.putInt("energy", energy.getEnergyStored());
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
            if (pLoad.contains("genome_extractor.progress")) {
                this.progress = pLoad.getIntOr("genome_extractor.progress", 0);
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
        return Component.translatable("block.genindustry_bees.genome_extractor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new genomeExtractorMenu(i, inventory, this, this.data);
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
            if (hasEmptySlot()) {
                removeExtractorError(EnumErrorCodesExtractor.OUTPUT_FULL);
                if (hasRecipe()){
                    if (hasRecipe()&& this.energy.getEnergyStored()> this.getRFCost()){
                        removeExtractorError(EnumErrorCodesExtractor.NO_RF);
                        this.isCrafting = true;
                        this.energy.removeEnergy(this.getRFCost());
                        this.increaseCraftingProgress();
                        setChanged(pLevel, pPos, pState);
                        if(hasProgressFinished()){
                            this.craftItem();
                            this.resetProgress();
                            this.sendUpdate();
                        }
                    } else {
                        addExtractorError(EnumErrorCodesExtractor.NO_RF);
                        this.lowerProgress();
                    }
                } else {
                    this.resetProgress();
                }
            }else {
                addExtractorError(EnumErrorCodesExtractor.OUTPUT_FULL);
            }
        }
        updateActiveState(pPos ,pState);
    }

    private void updateActiveState(BlockPos pos ,BlockState state) {
        boolean poweredState = state.getValue(BlockStateProperties.POWERED);
        if (poweredState != this.isCrafting){
            if (this.level != null) {
                this.level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, this.isCrafting));
            }
        }
        if (getExtractorErrors() >0) {
            if (this.level != null) {
                this.level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            }
        }

        this.setChanged();
    }

    private void resetProgress() {
        progress=0;
    }


    private void craftItem() {
        this.getInputItems().extractItem(GENE_SLOT, 1, false);
        ItemStack labware = this.getInputItems().getStackInSlot(LABWARE_SLOT);
        if (
                labwareHelper.getLabwareRate(labware.getItem()) >= random.nextDouble()
        ){
            Chromosome chromosome = GeneticHelper.getChromosome(this.getInputItems().getStackInSlot(BEE_SLOT), random.nextBoolean());
            Map.Entry<Identifier, IGene<?>> extractedGene =chromosome.getGenes().entrySet().stream().toList().get(random.nextInt(chromosome.getGenes().size()));
            ItemStack result = geneSampleItem.createGene(extractedGene.getKey(), extractedGene.getValue());
            for (int index: OUTPUT_SLOTS) {
               if (this.getOutputItems().getStackInSlot(index).isEmpty()){
                   this.getOutputItems().insertItem(index,result.copy(), false);
                   break;
               }
            }
        }
        this.getInputItems().extractItem(BEE_SLOT, 1, false);
        if (labware.isDamageableItem()) {
            labware.setDamageValue(labware.getDamageValue() + 1);
            if (labware.getDamageValue()>=labware.getMaxDamage())
                labware.shrink(1);
        } else {
            labware.shrink(1);
        }
    }


    private boolean hasRecipe() {
        if (this.getInputItems().getStackInSlot(BEE_SLOT).getItem() instanceof BeeItem beeItem && !(beeItem instanceof QueenItem)){
            removeExtractorError(EnumErrorCodesExtractor.NO_BEES);
        }else{
            addExtractorError(EnumErrorCodesExtractor.NO_BEES);
        }

        if (!this.getInputItems().getStackInSlot(GENE_SLOT).isEmpty()){
            removeExtractorError(EnumErrorCodesExtractor.NO_GENE);
        }else{
            addExtractorError(EnumErrorCodesExtractor.NO_GENE);
        }

        if (!this.getInputItems().getStackInSlot(LABWARE_SLOT).isEmpty()){
            removeExtractorError(EnumErrorCodesExtractor.NO_LABWARE);
        }else{
            addExtractorError(EnumErrorCodesExtractor.NO_LABWARE);
        }

        return (this.getInputItems().getStackInSlot(BEE_SLOT).getItem() instanceof BeeItem beeItem && !(beeItem instanceof QueenItem))
        && (this.getInputItems().getStackInSlot(GENE_SLOT).getItem() == modItems.GENE_SAMPLE.get())
        && (this.getInputItems().getStackInSlot(LABWARE_SLOT).is(modTags.Items.LABWARE));
    }

    private boolean hasEmptySlot() {
        for (int index : OUTPUT_SLOTS){
            if (this.getOutputItems().getStackInSlot(index).isEmpty()) {
                return true;
            }
        }
        return false;
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

    public void addExtractorError(EnumErrorCodesExtractor...error){
        for (EnumErrorCodesExtractor err : error){
            this.errorState |= err.value;
        }
    }

    public void removeExtractorError(EnumErrorCodesExtractor...error){
        for(EnumErrorCodesExtractor err : error) {
            this.errorState &= err.value ^ Integer.MAX_VALUE;
        }
    }

    public int getExtractorErrors(){
        return this.data.get(5);
    }

}