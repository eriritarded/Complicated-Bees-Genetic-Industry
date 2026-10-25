package com.accbdd.complicated_bees.validation;

import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.bees.*;
import com.accbdd.complicated_bees.component.Bee;
import com.accbdd.complicated_bees.registry.*;
import com.accbdd.complicated_bees.port.*;
import com.thesmartnoob880.genindustry_bees.block.modBlocks;
import com.thesmartnoob880.genindustry_bees.block.entity.*;
import com.thesmartnoob880.genindustry_bees.item.*;
import com.thesmartnoob880.genindustry_bees.port.GeneticData;
import com.thesmartnoob880.genindustry_bees.recipe.RecipeGeneticTemplate;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@Mod("bees_port_validation")
public class PortValidation {
    private final List<String> passed=new ArrayList<>();
    public PortValidation(){NeoForge.EVENT_BUS.addListener(this::started);NeoForge.EVENT_BUS.addListener(this::login);NeoForge.EVENT_BUS.addListener(this::serverTick);}
    private void serverTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){
        if(Boolean.getBoolean("bees.validation.client")&&Files.exists(Path.of("../client/client-report.txt")))event.getServer().halt(false);
    }
    private void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){
        if(!Boolean.getBoolean("bees.validation.client"))return;
        var player=(net.minecraft.server.level.ServerPlayer)event.getEntity();
        for(BlockPos pos:BlockPos.betweenClosed(-2,79,-2,7,79,5))player.level().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        player.teleportTo(1.5,81,3.5);
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
        var level=player.level();
        level.setBlockAndUpdate(new BlockPos(2,80,0),modBlocks.GENOME_EXTRACTOR.get().defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(4,80,0),BlocksRegistration.APIARY.get().defaultBlockState());
        player.getInventory().add(geneSampleItem.createGene(com.accbdd.complicated_bees.bees.gene.GeneSpecies.ID,com.accbdd.complicated_bees.registry.GeneRegistration.SPECIES.get()));
    }
    private void check(boolean condition,String name){if(!condition)throw new AssertionError(name);passed.add(name);}
    private void started(ServerStartedEvent event){
        MinecraftServer server=event.getServer();
        boolean success=false;
        try{
            Files.writeString(Path.of("validation-report.txt"),"RUNNING\n");
            if(Boolean.getBoolean("bees.validation.client"))Files.deleteIfExists(Path.of("../client/client-report.txt"));
            run(server.overworld());
            Files.writeString(Path.of("validation-report.txt"),"PASS "+passed.size()+"\n"+String.join("\n",passed)+"\n");
            ComplicatedBees.LOGGER.info("BEES_VALIDATION_PASS: {} assertions",passed.size());
            success=true;
        }catch(Throwable failure){
            ComplicatedBees.LOGGER.error("BEES_VALIDATION_FAILED after {} assertions",passed.size(),failure);
            try{Files.writeString(Path.of("validation-report.txt"),"FAIL after "+passed.size()+"\n"+failure+"\n"+String.join("\n",passed));}catch(Exception ignored){}
        }finally{if(!Boolean.getBoolean("bees.validation.client")||!success)server.halt(false);}
    }
    private void run(ServerLevel level){
        var speciesRegistry=level.registryAccess().lookupOrThrow(SpeciesRegistration.SPECIES_REGISTRY_KEY);
        check(speciesRegistry.size()>=40,"species datapack registry populated: "+speciesRegistry.size());
        check(level.registryAccess().lookupOrThrow(CombRegistration.COMB_REGISTRY_KEY).size()>10,"comb datapack registry populated");
        check(PortRecipes.manager(level).getAllRecipesFor(EsotericRegistration.MUTATION_RECIPE.get()).size()>20,"mutation recipes loaded");
        check(PortRecipes.manager(level).getAllRecipesFor(EsotericRegistration.CENTRIFUGE_RECIPE.get()).size()>10,"centrifuge recipes loaded");
        Species species=speciesRegistry.stream().filter(s->s.getColor()!=-1).findFirst().orElseThrow();
        ItemStack bee=species.toStack(ItemsRegistration.DRONE.get());
        bee.update(EsotericRegistration.BEE,Bee.DEFAULT,b->b.withAge(0.25f).withAnalyzed(true).withGeneration(7));
        ItemStack roundtrip=PortData.loadStack(level.registryAccess(),(CompoundTag)PortData.saveStack(bee,level.registryAccess()));
        check(ItemStack.isSameItemSameComponents(bee,roundtrip),"bee native item codec preserves genome, age, analysis, generation and species");
        ItemStack template=new ItemStack(modItems.GENETIC_TEMPLATE.get());
        for(var entry:species.getDefaultChromosome().getGenes().entrySet()){
            ItemStack sample=geneSampleItem.createGene(entry.getKey(),entry.getValue());
            ItemStack restored=PortData.loadStack(level.registryAccess(),(CompoundTag)PortData.saveStack(sample,level.registryAccess()));
            check(geneSampleItem.hasGeneData(restored)&&GeneticData.get(sample).equals(GeneticData.get(restored)),"sample codec: "+entry.getKey());
            CraftingInput input=CraftingInput.of(2,1,List.of(template,sample));
            var recipe=new RecipeGeneticTemplate();
            check(recipe.matches(input,level),"template crafting matches: "+entry.getKey());
            template=recipe.assemble(input);
        }
        check(geneticTemplateItem.isCompleteTemplate(template),"assembled template contains every registered gene");
        check(!new RecipeGeneticTemplate().matches(CraftingInput.of(2,2,List.of(template,new ItemStack(modItems.GENE_SAMPLE.get()),new ItemStack(Items.DIAMOND),ItemStack.EMPTY)),level),"template recipe rejects blank samples and unrelated ingredients");
        check(ItemStack.isSameItemSameComponents(template,PortData.loadStack(level.registryAccess(),(CompoundTag)PortData.saveStack(template,level.registryAccess()))),"template native codec preserves all genes");

        BlockPos imprinterPos=new BlockPos(0,80,0),extractorPos=new BlockPos(2,80,0);
        level.getChunk(0,0);
        level.setBlockAndUpdate(imprinterPos,Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(extractorPos,Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(imprinterPos,modBlocks.GENETIC_IMPRINTER.get().defaultBlockState());
        level.setBlockAndUpdate(extractorPos,modBlocks.GENOME_EXTRACTOR.get().defaultBlockState());
        var imprinter=(geneticImprinterBlockEntity)level.getBlockEntity(imprinterPos);
        var extractor=(genomeExtractorBlockEntity)level.getBlockEntity(extractorPos);
        check(imprinter!=null&&extractor!=null,"both addon block entities create in a server world");
        var energy=level.getCapability(Capabilities.Energy.BLOCK,imprinterPos,Direction.UP);
        check(energy!=null,"native transactional energy capability exposed");
        try(var tx=Transaction.openRoot()){check(energy.insert(10000,tx)>0,"energy insertion accepts power");}
        check(energy.getAmountAsLong()==0,"aborted energy transaction rolls back");
        try(var tx=Transaction.openRoot()){energy.insert(10000,tx);tx.commit();}
        check(energy.getAmountAsLong()==10000,"committed energy transaction persists");
        var inventory=level.getCapability(Capabilities.Item.BLOCK,extractorPos,Direction.UP);
        check(inventory!=null&&inventory.size()==12,"native transactional inventory capability exposes output and input slots");
        try(var tx=Transaction.openRoot()){check(inventory.insert(11,ItemResource.of(modItems.GENE_SAMPLE.get()),1,tx)==1,"blank sample insertion reaches correct input slot");}
        check(extractor.getInputItems().getStackInSlot(2).isEmpty(),"aborted inventory transaction rolls back");
        imprinter.getInputItems().setStackInSlot(0,bee.copy());
        imprinter.getInputItems().setStackInSlot(1,template.copy());
        imprinter.getEnergyHandler().setEnergy(imprinter.getEnergyHandler().getMaxEnergyStored());
        runTicker(level,imprinterPos,imprinter,()->!imprinter.getOutputItems().getStackInSlot(0).isEmpty());
        ItemStack imprinted=imprinter.getOutputItems().getStackInSlot(0);
        check(!imprinted.isEmpty(),"registered imprinter ticker produces a bee");
        check(GeneticHelper.getChromosome(imprinted,true).serialize().equals(GeneticData.get(template)),"imprinting writes primary chromosome");
        check(GeneticHelper.getChromosome(imprinted,false).serialize().equals(GeneticData.get(template)),"imprinting writes secondary chromosome");
        check(imprinter.getInputItems().getStackInSlot(0).isEmpty()&&geneticTemplateItem.isCompleteTemplate(imprinter.getInputItems().getStackInSlot(1)),"imprinting consumes one bee and retains the template");
        extractor.getInputItems().setStackInSlot(0,new ItemStack(modItems.ADVANCED_LABWARE.get()));
        extractor.getInputItems().setStackInSlot(1,bee.copy());
        extractor.getInputItems().setStackInSlot(2,new ItemStack(modItems.GENE_SAMPLE.get()));
        extractor.getEnergyHandler().setEnergy(extractor.getEnergyHandler().getMaxEnergyStored());
        runTicker(level,extractorPos,extractor,()->!extractor.getOutputItems().getStackInSlot(0).isEmpty());
        check(geneSampleItem.hasGeneData(extractor.getOutputItems().getStackInSlot(0)),"registered extractor ticker produces a sample with a real gene");
        check(extractor.getInputItems().getStackInSlot(1).isEmpty()&&extractor.getInputItems().getStackInSlot(2).isEmpty(),"extraction consumes bee and blank sample");
        check(extractor.getInputItems().getStackInSlot(0).getDamageValue()==1,"advanced labware takes one durability damage");
        var saved=extractor.saveWithoutMetadata(level.registryAccess());
        var restored=new genomeExtractorBlockEntity(extractorPos,extractor.getBlockState());restored.setLevel(level);
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(ItemStack.isSameItemSameComponents(restored.getOutputItems().getStackInSlot(0),extractor.getOutputItems().getStackInSlot(0)),"block entity native Value I/O preserves gene samples");
        check(restored.getEnergyHandler().getEnergyStored()==extractor.getEnergyHandler().getEnergyStored(),"block entity native Value I/O preserves energy");
        var before=level.getEntitiesOfClass(ItemEntity.class,new AABB(extractorPos).inflate(2)).size();
        level.setBlockAndUpdate(extractorPos,Blocks.AIR.defaultBlockState());
        check(level.getEntitiesOfClass(ItemEntity.class,new AABB(extractorPos).inflate(2)).size()>before,"breaking extractor drops inventory through native removal hook");
        for(var registered:BlocksRegistration.BLOCKS.getEntries()){
            BlockPos pos=new BlockPos(5,80,0);
            level.setBlockAndUpdate(pos,registered.get().defaultBlockState());
            check(level.getBlockState(pos).is(registered.get()),"block placement: "+registered.getId());
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        }
        baseMechanics(level,species);
    }
    private void baseMechanics(ServerLevel level,Species species){
        BlockPos apiaryPos=new BlockPos(8,80,0),centrifugePos=new BlockPos(10,80,0),nestPos=new BlockPos(12,80,0);
        for(BlockPos pos:List.of(apiaryPos,centrifugePos,nestPos))level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(apiaryPos,BlocksRegistration.APIARY.get().defaultBlockState());
        var apiary=(com.accbdd.complicated_bees.block.entity.ApiaryBlockEntity)level.getBlockEntity(apiaryPos);
        // This fixture accelerates tickers synchronously, so initialize the queued world-load callback first.
        apiary.onLoad();
        ItemStack princess=species.toStack(ItemsRegistration.PRINCESS.get()),drone=species.toStack(ItemsRegistration.DRONE.get());
        apiary.getBeeItems().setStackInSlot(0,princess.copy());
        apiary.getBeeItems().setStackInSlot(1,drone.copyWithCount(2));
        runTicker(level,apiaryPos,apiary,()->apiary.getBeeItems().getStackInSlot(0).is(ItemsRegistration.QUEEN.get()));
        ItemStack queen=apiary.getBeeItems().getStackInSlot(0);
        check(queen.is(ItemsRegistration.QUEEN.get()),"registered apiary ticker mates princess and drone into queen");
        check(apiary.getBeeItems().getStackInSlot(1).getCount()==1,"apiary mating consumes exactly one drone");
        check(GeneticHelper.getGenome(queen).equals(GeneticHelper.getGenome(princess))&&queen.get(EsotericRegistration.BEE).mate().equals(GeneticHelper.getGenome(drone)),"apiary queen retains maternal and paternal diploid genomes");
        var apiaryTag=apiary.saveWithoutMetadata(level.registryAccess());
        var apiaryRestored=new com.accbdd.complicated_bees.block.entity.ApiaryBlockEntity(apiaryPos,apiary.getBlockState());apiaryRestored.setLevel(level);
        apiaryRestored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),apiaryTag));
        check(ItemStack.isSameItemSameComponents(queen,apiaryRestored.getBeeItems().getStackInSlot(0)),"apiary native persistence retains queen and mate genomes");

        level.setBlockAndUpdate(centrifugePos,BlocksRegistration.CENTRIFUGE.get().defaultBlockState());
        var centrifuge=(com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity)level.getBlockEntity(centrifugePos);
        centrifuge.getInputItems().setStackInSlot(0,new ItemStack(Items.HONEY_BOTTLE));
        centrifuge.getEnergyHandler().receiveEnergy(100000,false);
        runTicker(level,centrifugePos,centrifuge,()->count(centrifuge.getOutputItemHandler(),ItemsRegistration.HONEY_DROPLET.get())==3);
        check(count(centrifuge.getOutputItemHandler(),ItemsRegistration.HONEY_DROPLET.get())==3,"registered centrifuge ticker processes native honey-bottle recipe into three droplets");
        check(centrifuge.getInputItems().getStackInSlot(0).isEmpty()&&centrifuge.getStoredPower()<100000,"centrifuge consumes input and energy");

        var feature=EsotericRegistration.COMPLICATED_HIVE_FEATURE.get();
        String speciesId=level.registryAccess().lookupOrThrow(SpeciesRegistration.SPECIES_REGISTRY_KEY).getKey(species).toString();
        check(feature.place(new com.accbdd.complicated_bees.worldgen.ComplicatedHiveFeatureConfiguration(speciesId),level,level.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(42),nestPos),"native worldgen feature places a bee nest");
        var nest=(com.accbdd.complicated_bees.block.entity.BeeNestBlockEntity)level.getBlockEntity(nestPos);
        check(nest!=null&&nest.getSpecies()==species,"worldgen nest stores configured species");
        var nestRestored=new com.accbdd.complicated_bees.block.entity.BeeNestBlockEntity(nestPos,nest.getBlockState());nestRestored.setLevel(level);
        nestRestored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),nest.saveWithoutMetadata(level.registryAccess())));
        check(nestRestored.getSpecies()==species,"native nest persistence retains configured species");
        multiblock(level,new BlockPos(32,80,0),true);
        multiblock(level,new BlockPos(40,80,0),false);
    }
    private int count(net.neoforged.neoforge.items.IItemHandler handler,Item item){
        int total=0;for(int slot=0;slot<handler.getSlots();slot++)if(handler.getStackInSlot(slot).is(item))total+=handler.getStackInSlot(slot).getCount();return total;
    }
    private void multiblock(ServerLevel level,BlockPos center,boolean mellarium){
        String name=mellarium?"mellarium":"gyrofuge";
        Block base=mellarium?BlocksRegistration.MELLARIUM_BASE.get():BlocksRegistration.GYROFUGE_BASE.get();
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-1,-1,-1),center.offset(1,1,1)))level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        for(BlockPos pos:BlockPos.betweenClosed(center.offset(-1,-1,-1),center.offset(1,1,1)))level.setBlockAndUpdate(pos,base.defaultBlockState());
        // Invoke the same native placement callback used by BlockItem after placing the last shell block.
        BlockPos corner=center.offset(1,1,1);
        base.setPlacedBy(level,corner,level.getBlockState(corner),null,new ItemStack(base));
        Block controller=mellarium?BlocksRegistration.MELLARIUM_CONTROLLER.get():BlocksRegistration.GYROFUGE_CONTROLLER.get();
        check(level.getBlockState(center).is(controller),name+" shell assembles through block placement callback");
        boolean linked=mellarium?((com.accbdd.complicated_bees.block.entity.mellarium.AbstractMellariumBlockEntity)level.getBlockEntity(corner)).getLogic()!=null:((com.accbdd.complicated_bees.block.entity.gyrofuge.AbstractGyrofugeBlockEntity)level.getBlockEntity(corner)).getLogic()!=null;
        check(linked,name+" shell blocks link to controller logic");
        level.setBlockAndUpdate(corner,Blocks.AIR.defaultBlockState());
        check(level.getBlockState(center).is(base),name+" deconstructs when a shell block is broken through native removal");
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private void runTicker(ServerLevel level,BlockPos pos,BlockEntity entity,java.util.function.BooleanSupplier complete){
        var state=level.getBlockState(pos);
        BlockEntityTicker ticker=((EntityBlock)state.getBlock()).getTicker(level,state,entity.getType());
        if(ticker==null)throw new AssertionError("Missing server ticker");
        for(int tick=0;tick<2400&&!complete.getAsBoolean();tick++)ticker.tick(level,pos,level.getBlockState(pos),entity);
    }
}
