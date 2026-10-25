package com.accbdd.complicated_bees.validation;
import com.accbdd.complicated_bees.ComplicatedBees;
import com.accbdd.complicated_bees.bees.*;
import com.accbdd.complicated_bees.registry.*;
import com.accbdd.complicated_bees.port.*;
import com.accbdd.complicated_bees.item.CombItem;
import com.thesmartnoob880.genindustry_bees.item.modItems;
import com.thesmartnoob880.genindustry_bees.port.GeneticData;
import com.thesmartnoob880.genindustry_bees.screen.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="bees_port_validation",value=Dist.CLIENT)
public final class ClientValidation {
    private static int stage,ticks,total;
    private static final List<String> passed=new ArrayList<>();
    private static void check(boolean value,String name){if(!value)throw new AssertionError(name);passed.add(name);}
    private static void finish(String status,Throwable failure){
        try{Files.writeString(Path.of("client-report.txt"),status+" "+passed.size()+"\n"+String.join("\n",passed)+(failure==null?"":"\n"+failure));}catch(Exception e){throw new RuntimeException(e);}
        if(failure!=null)ComplicatedBees.LOGGER.error("BEES_CLIENT_VALIDATION_FAILED",failure);
        else ComplicatedBees.LOGGER.info("BEES_CLIENT_VALIDATION_PASS: {} assertions",passed.size());
        stage=99;Minecraft.getInstance().stop();
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("bees.validation.client")||stage==99)return;
        var mc=Minecraft.getInstance();ticks++;total++;
        try{
            if(total>2400)throw new AssertionError("Client validation timed out at stage "+stage+" screen="+TargetClient.screen());
            if(stage==0&&TargetClient.screen() instanceof TitleScreen&&ticks>20){
                String address="127.0.0.1:"+System.getProperty("bees.validation.port");
                ConnectScreen.startConnecting(TargetClient.screen(),mc,ServerAddress.parseString(address),new ServerData("Bees validation",address,ServerData.Type.OTHER),false,null);
                stage=1;ticks=0;
            }else if(stage==1&&mc.level!=null&&mc.player!=null&&ticks>80){
                check(PortRecipes.clientRecipes.size()>40,"machine recipes synchronized to a real connected client");
                check(mc.player.getInventory().getNonEquipmentItems().stream().anyMatch(stack->stack.is(modItems.GENE_SAMPLE.get())&&GeneticData.has(stack)),"sample component synchronized in player inventory");
                TargetClient.setScreen(new Gallery());stage=2;ticks=0;
            }else if(stage==2&&ticks>40){
                Capture.save("01-gallery.png");stage=3;ticks=0;
            }else if(stage==3&&ticks>15){
                TargetClient.setScreen(null);use(mc,new BlockPos(0,80,0));stage=4;ticks=0;
            }else if(stage==4&&TargetClient.screen() instanceof geneticImprinterScreen&&ticks>20){
                check(true,"imprinter opens through native block interaction and menu packet");Capture.save("02-imprinter.png");mc.player.closeContainer();stage=5;ticks=0;
            }else if(stage==5&&ticks>15){use(mc,new BlockPos(2,80,0));stage=6;ticks=0;
            }else if(stage==6&&TargetClient.screen() instanceof genomeExtractorScreen&&ticks>20){
                check(true,"extractor opens through native block interaction and menu packet");Capture.save("03-extractor.png");mc.player.closeContainer();stage=7;ticks=0;
            }else if(stage==7&&ticks>15){use(mc,new BlockPos(4,80,0));stage=8;ticks=0;
            }else if(stage==8&&TargetClient.screen() instanceof com.accbdd.complicated_bees.screen.ApiaryScreen&&ticks>20){
                check(true,"apiary opens through native block interaction and menu packet");Capture.save("04-apiary.png");stage=9;ticks=0;
            }else if(stage==9&&ticks>20){
                mc.player.closeContainer();
                Vec3 delta=Vec3.atCenterOf(new BlockPos(4,80,0)).subtract(mc.player.getEyePosition());
                mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
                mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
                stage=10;ticks=0;
            }else if(stage==10&&ticks>50){
                check(mc.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(new BlockPos(4,80,0)),"native client raycast targets apiary for optional Jade tooltip capture");
                Capture.save("05-apiary-tooltip.png");finish("PASS",null);
            }
        }catch(Throwable failure){finish("FAIL",failure);}
    }
    private static void use(Minecraft mc,BlockPos pos){
        mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.SOUTH,pos,false));
    }
    private static final class Gallery extends Screen {
        private final List<ItemStack> items=new ArrayList<>();
        Gallery(){super(Component.literal("Complicated Bees port validation"));
            for(Species species:Minecraft.getInstance().level.registryAccess().lookupOrThrow(SpeciesRegistration.SPECIES_REGISTRY_KEY))if(species.getColor()!=-1)items.addAll(species.toMembers());
            for(var id:Minecraft.getInstance().level.registryAccess().lookupOrThrow(CombRegistration.COMB_REGISTRY_KEY).keySet())items.add(CombItem.setComb(new ItemStack(ItemsRegistration.COMB.get()),id));
            for(var holder:BuiltInRegistries.ITEM.entrySet())if(holder.getKey().identifier().getNamespace().equals("complicated_bees")||holder.getKey().identifier().getNamespace().equals("genindustry_bees")){
                if(holder.getValue() instanceof com.accbdd.complicated_bees.item.BeeItem||holder.getValue() instanceof CombItem)continue;
                items.add(new ItemStack(holder.getValue()));
            }
            check(items.size()>200,"gallery includes all bee castes, combs, machines and addon items: "+items.size());
        }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick){
            graphics.fill(0,0,width,height,0xff20252b);
            graphics.text(font,title,12,8,-1);graphics.text(font,Component.literal(items.size()+" items; native item definitions and original textures"),12,21,0xffb9cbd8);
            int columns=Math.max(1,(width-24)/20);
            for(int i=0;i<items.size();i++)graphics.item(items.get(i),12+(i%columns)*20,40+(i/columns)*20);
        }
        @Override public boolean isPauseScreen(){return false;}
    }
}
