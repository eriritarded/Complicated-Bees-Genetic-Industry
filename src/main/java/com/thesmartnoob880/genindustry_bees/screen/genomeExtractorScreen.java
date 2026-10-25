package com.thesmartnoob880.genindustry_bees.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thesmartnoob880.genindustry_bees.GenIndustryBees;
import com.thesmartnoob880.genindustry_bees.util.enums.EnumErrorCodesExtractor;
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

public class genomeExtractorScreen extends AbstractContainerScreen<genomeExtractorMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(GenIndustryBees.MODID, "textures/gui/genome_extractor_gui.png");
    public genomeExtractorScreen(genomeExtractorMenu pMenu, Inventory pInventory, Component pComponent) {
        super(pMenu, pInventory, pComponent,176,187);
        this.inventoryLabelY = this.imageHeight-10000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        if (menu.isCrafting()){
            com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x + 66, y +35, 176, 0, 16, menu.getScaledProgress());
        } else if (this.menu.getExtractorData().get(5) > 0){
            com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE, x+66, y+65, 197, 0, 16, 16);
        }
    }
    private void renderEnergyBar(GuiGraphicsExtractor guiGraphics, int x, int y){
        int energyScaled = this.menu.getEnergyScaled();
        com.accbdd.complicated_bees.port.PortGui.blit(guiGraphics,TEXTURE,x+163,y+16+(75- energyScaled), 192, 75- energyScaled, 5, energyScaled);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        this.renderPowerTooltip(guiGraphics, mouseX, mouseY);
        this.renderWarnings(guiGraphics, mouseX, mouseY);
    }

    protected void renderPowerTooltip(GuiGraphicsExtractor guiGraphics, int pX, int pY){
        super.extractTooltip(guiGraphics, pX, pY);
        if (pX >= this.leftPos+163 && pX < this.leftPos +163 + 5 && pY >= this.topPos + 16 && pY < this.topPos + 16 + 75){
            int power = this.menu.getEnergy();
            int RFCost = this.menu.getEnergyPerTick();
            com.accbdd.complicated_bees.port.PortGui.tooltip(guiGraphics,this.font, List.of(Component.literal(power + " RF"), Component.literal(RFCost+ " RF/t").withStyle(ChatFormatting.GRAY)),pX, pY);
        }
    }

    protected void renderWarnings(GuiGraphicsExtractor guiGraphics, int pX, int pY) {
        super.extractTooltip(guiGraphics, pX, pY);
        if (!menu.isCrafting() && pX >= this.leftPos + 66 && pX < this.leftPos + 66 + 16 && pY >= this.topPos + 65 && pY < this.topPos + 65 + 16) {
            int errorFlags = this.menu.getExtractorData().get(5);
            if (errorFlags > 0) {
                List<Component> errors = new ArrayList<>();

                for (EnumErrorCodesExtractor errorcode : EnumErrorCodesExtractor.values()) {
                    if ((errorFlags & errorcode.value) != 0) {
                        errors.add(errorcode.getExtractorMessage());
                    }
                }

                com.accbdd.complicated_bees.port.PortGui.tooltip(guiGraphics,this.font, errors, Optional.empty(), pX, pY);
            }
        }
    }
}
