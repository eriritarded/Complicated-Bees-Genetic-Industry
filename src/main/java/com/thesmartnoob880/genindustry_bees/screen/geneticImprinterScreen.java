package com.thesmartnoob880.genindustry_bees.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.util.enums.EnumErrorCodesImprinter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class geneticImprinterScreen extends AbstractContainerScreen<geneticImprinterMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(GenIndustryBees.MODID, "textures/gui/genetic_imprinter_gui.png");
    public geneticImprinterScreen(geneticImprinterMenu pImprinterMenu, Inventory pInventory, Component pComponent) {
        super(pImprinterMenu, pInventory, pComponent,176,158);
        this.inventoryLabelY = this.imageHeight-10000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting()){
            com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x + 78, y +25, 176, 0, menu.getScaledProgress(), 16);
        }else if (this.menu.getImprinterData().get(5) >0){
            com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x+31, y+55, 201, 0, 16, 16);
        }
    }

    private void renderEnergyBar(GuiGraphicsExtractor guiGraphics, int x, int y){
        int energyScaled = this.menu.getEnergyScaled();
        com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE,x+152,y+8+(61- energyScaled), 196, 61- energyScaled, 5, energyScaled);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        this.renderPowerTooltip(guiGraphics, mouseX, mouseY);
        this.renderWarnings(guiGraphics, mouseX, mouseY);
    }

    protected void renderPowerTooltip(GuiGraphicsExtractor graphics, int pX, int pY) {
        super.extractTooltip(graphics, pX, pY);
        if (pX >= this.leftPos+152 && pX < this.leftPos +157 && pY >= this.topPos + 8 && pY < this.topPos +69){
            int power = this.menu.getEnergy();
            int RFCost = this.menu.getEnergyPerTick();
            com.accbdd.complicated_bees.port.PortGui.tooltip(graphics,this.font, List.of(Component.literal(power + " RF"), Component.literal(RFCost+ " RF/t").withStyle(ChatFormatting.GRAY)),pX, pY);
        }
    }

    protected void renderWarnings(GuiGraphicsExtractor guiGraphics, int pX, int pY){
        super.extractTooltip(guiGraphics, pX, pY);
        if (!menu.isCrafting() && pX >= this.leftPos+31 && pX < this.leftPos +47 && pY >= this.topPos + 55 && pY < this.topPos + 71){
            int errorFlags = this.menu.getImprinterData().get(5);
            if (errorFlags > 0) {
                List<Component> errors = new ArrayList<>();
                for (EnumErrorCodesImprinter errorcode : EnumErrorCodesImprinter.values()){
                    if ((errorFlags & errorcode.value) != 0){
                        errors.add(errorcode.getImprinterMessage());
                    }
                }
                com.accbdd.complicated_bees.port.PortGui.tooltip(guiGraphics,this.font, errors, Optional.empty(), pX, pY);
            }
        }
    }
}
