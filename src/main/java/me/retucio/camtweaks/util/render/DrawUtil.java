package me.retucio.camtweaks.util.render;

import net.minecraft.client.gui.DrawContext;

public class DrawUtil {

    public static void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + 1, color);
        ctx.fill(x, y + height - 1, x + width, y + height, color);
        ctx.fill(x, y, x + 1, y + height, color);
        ctx.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawCircle(DrawContext ctx, int x, int y, int radius, int color) {
        for (int i = -radius; i <= radius; i++)
            for (int j = -radius; j <= radius; j++)
                if (i * i + j * j <= radius * radius)
                    ctx.fill(x + i, y + j, x + i + 1, y + j + 1, color);
    }
}