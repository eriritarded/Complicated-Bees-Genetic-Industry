package com.accbdd.complicated_bees.validation;
import net.minecraft.client.*;
public final class Capture {
    public static void save(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),1,c->{});}
}
