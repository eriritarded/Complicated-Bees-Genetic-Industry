from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
base=root/'genetic_industry/src/main/java/com/thesmartnoob880/genindustry_bees'
data='com.thesmartnoob880.genindustry_bees.port.GeneticData'

def remove_method(text,name):
    match=re.search(r'    @Override\s+public [^{}\n]*\b'+name+r'\([^{}]*?\) \{',text)
    if not match:return text
    start=match.start();end=match.end();depth=1
    while depth:
        if text[end]=='{':depth+=1
        elif text[end]=='}':depth-=1
        end+=1
    return text[:start]+text[end:]

for path in base.rglob('*.java'):
 if 'port' in path.parts:continue
 s=path.read_text(encoding='utf-8')
 s=s.replace('import net.neoforged.neoforge.registries.RegistryObject;','import java.util.function.Supplier;').replace('RegistryObject<','Supplier<')
 for a,b in [('ITEMS','ITEM'),('BLOCKS','BLOCK'),('BLOCK_ENTITY_TYPES','BLOCK_ENTITY_TYPE'),('MENU_TYPES','MENU'),('RECIPE_SERIALIZERS','RECIPE_SERIALIZER'),('CREATIVE_MODE_TABS','CREATIVE_MODE_TAB')]:
  s=s.replace('BuiltInRegistries.'+a,'net.minecraft.core.registries.Registries.'+b)
 s=s.replace('IForgeMenuType','IMenuTypeExtension')
 s=s.replace('import net.neoforged.neoforge.common.util.LazyOptional;', 'import com.thesmartnoob880.genindustry_bees.port.LazyOptional;')
 s=s.replace('import net.neoforged.neoforge.common.capabilities.Capability;\n','').replace('import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;\n','')
 s=s.replace('@Mod.EventBusSubscriber(modid = GenIndustryBees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)','@net.neoforged.fml.common.EventBusSubscriber(modid = GenIndustryBees.MODID)')
 s=s.replace('BlockBehaviour.Properties.copy(', 'BlockBehaviour.Properties.ofFullCopy(')
 s=s.replace('BlockEntityType.Builder.of(', 'new BlockEntityType<>(').replace('.build(null)','')
 s=s.replace('ComplicatedBees.GENE_REGISTRY.get().getKeys()', 'ComplicatedBees.GENE_REGISTRY.get().keySet()').replace('ComplicatedBees.GENE_REGISTRY.get().getEntries()', 'ComplicatedBees.GENE_REGISTRY.get().entrySet()')
 s=s.replace('.getValue(Identifier.', '.getValue(Identifier.')
 s=s.replace('.registry(','.lookup(')
 s=re.sub(r'(\w+)\.getTag\(\)',lambda m:data+'.get('+m[1]+')',s)
 s=re.sub(r'(\w+)\.hasTag\(\)',lambda m:data+'.has('+m[1]+')',s)
 s=re.sub(r'(\w+)\.setTag\((\w+)\)',lambda m:data+'.set('+m[1]+','+m[2]+')',s)
 s=s.replace('new ItemStack(modItems.GENE_SAMPLE.get(), 1, root)', 'new ItemStack(modItems.GENE_SAMPLE.get(), 1)')
 s=s.replace('Screen.hasShiftDown()', 'net.minecraft.client.Minecraft.getInstance().hasShiftDown()')
 if 'appendHoverText' in s:
  s=re.sub(r'(appendHoverText\((?:@NotNull )?ItemStack (\w+), )(?:@Nullable )?Level (\w+), ((?:@NotNull )?)List<Component> (\w+), ',r'\1TooltipContext \3, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> \5, ',s)
  for var in re.findall(r'Consumer<Component> (\w+)',s):s=s.replace(var+'.add(',var+'.accept(')
  s=re.sub(r'super.appendHoverText\(([^,]+), ([^,]+), ([^,]+), ([^)]+)\)',r'super.appendHoverText(\1, \2, display, \3, \4)',s)
 if path.name.endswith('BlockEntity.java'):
  s=remove_method(s,'getCapability');s=remove_method(s,'invalidateCaps')
  s=s.replace('protected void saveAdditional(CompoundTag psaveAdd)', 'protected void saveAdditional(CompoundTag psaveAdd, net.minecraft.core.HolderLookup.Provider registries)')
  s=s.replace('super.saveAdditional(psaveAdd)', 'super.saveAdditional(psaveAdd, registries)')
  s=s.replace('public void load(@NotNull CompoundTag pLoad)', 'public void loadAdditional(CompoundTag pLoad, net.minecraft.core.HolderLookup.Provider registries)')
  s=s.replace('public void load(CompoundTag pLoad)', 'public void loadAdditional(CompoundTag pLoad, net.minecraft.core.HolderLookup.Provider registries)')
  s=s.replace('super.load(pLoad)', 'super.loadAdditional(pLoad,registries)')
  s=s.replace('.serializeNBT()', '.serializeNBT(registries)').replace('.deserializeNBT(pLoad.', '.deserializeNBT(registries,pLoad.')
  s=s.replace('this.getInputItems().getStackInSlot(TEMPLATE_SLOT).getTag()',data+'.get(this.getInputItems().getStackInSlot(TEMPLATE_SLOT))')
  if path.name=='geneticImprinterBlockEntity.java':
   start=s.index('        CompoundTag chromosomeA =');end=s.index('\n        return originalBee.copyWithCount(1);',start)
   s=s[:start]+'''        var chromosome=com.accbdd.complicated_bees.bees.Chromosome.deserialize(templateData);
        com.accbdd.complicated_bees.bees.GeneticHelper.setGenome(originalBee,chromosome);
'''+s[end:]
   s=s.replace('return (NbtUtils.compareNbt('+data+'.get(outputItem), '+data+'.get(craftedresult), false)) && outputItem.getItem()==craftedresult.getItem()||outputItem == ItemStack.EMPTY;', 'return outputItem.isEmpty() || ItemStack.isSameItemSameComponents(outputItem,craftedresult);')
  # Read by static capability providers in the mod entrypoint.
  s=s.replace('    private int errorState = 0;', '    public customEnergyStorage getEnergyHandler(){return energy;}\n    private int errorState = 0;')
 if path.name.endswith('Block.java'):
  s=s.replace('extends BaseEntityBlock','extends com.accbdd.complicated_bees.port.PortEntityBlock')
  s=s.replace('import net.neoforged.neoforge.network.NetworkHooks;\n','')
  s=re.sub(r'NetworkHooks.openScreen\(\(\(ServerPlayer\)pPlayer\), \((\w+)\)entity, pPos\)',r'((ServerPlayer)pPlayer).openMenu((\1)entity,pPos)',s)
  s=re.sub(r'@NotNull InteractionResult use\((?:@NotNull )?BlockState pState, Level pLevel, (?:@NotNull )?BlockPos pPos, (?:@NotNull )?Player pPlayer, (?:@NotNull )?InteractionHand pHand, (?:@NotNull )?BlockHitResult pHit\)', 'InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit)',s)
  s=s.replace('    public static final DeferredRegister<Block> BLOCKS', '    public static final net.neoforged.neoforge.registries.DeferredRegister.Blocks BLOCKS')
  s=s.replace('DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK, GenIndustryBees.MODID)','new com.accbdd.complicated_bees.port.RegistrationContext.Blocks(GenIndustryBees.MODID)')
  s=s.replace('    @Override\n    public void onRemove','    @Override\n    public void onRemove')
 if path.name=='modItems.java':
  s=s.replace('DeferredRegister<Item> ITEMS','DeferredRegister.Items ITEMS').replace('DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, GenIndustryBees.MODID)','new com.accbdd.complicated_bees.port.RegistrationContext.Items(GenIndustryBees.MODID)')
  s=s.replace('.setNoRepair()','')
 if path.name.endswith('Screen.java'):s=s.replace('protected void extractBackground(', 'public void extractBackground(')
 path.write_text(s,encoding='utf-8')

# Every old constructor gets the actual deferred registration key.
for project in ['complicated_bees','genetic_industry']:
 for path in (root/project/'src/main/java').rglob('*.java'):
  if 'port' in path.parts:continue
  s=path.read_text(encoding='utf-8')
  s=s.replace('new Item.Properties()', 'com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()')
  if 'item' in path.parts:s=s.replace('new Properties()', 'com.accbdd.complicated_bees.port.RegistrationContext.itemProperties()')
  s=s.replace('DeferredRegister.createItems(MODID)', 'new com.accbdd.complicated_bees.port.RegistrationContext.Items(MODID)')
  s=s.replace('DeferredRegister.createBlocks(MODID)', 'new com.accbdd.complicated_bees.port.RegistrationContext.Blocks(MODID)')
  s=s.replace('BlockBehaviour.Properties.of()', 'com.accbdd.complicated_bees.port.RegistrationContext.blockProperties()')
  s=re.sub(r'BlockBehaviour.Properties.ofFullCopy\(([^)]+)\)',r'BlockBehaviour.Properties.ofFullCopy(\1).setId(com.accbdd.complicated_bees.port.RegistrationContext.blockKey())',s)
  if path.name!='BlocksRegistration.java':
   for var in ['WAX_PROPERTIES','HONEYPLANK_PROPERTIES']:
    s=s.replace('BlocksRegistration.'+var,'BlocksRegistration.'+var+'.setId(com.accbdd.complicated_bees.port.RegistrationContext.blockKey())')
  path.write_text(s,encoding='utf-8')
