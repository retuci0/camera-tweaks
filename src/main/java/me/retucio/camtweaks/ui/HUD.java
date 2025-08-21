package me.retucio.camtweaks.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

// en un principio esto iba a mostrar en pantalla los módulos activados, pero me parece innecesario. se queda aquí por si acaso
public class HUD {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(DrawContext ctx, RenderTickCounter tc) {}
}