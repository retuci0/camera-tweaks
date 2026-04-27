package me.retucio.sputnik.util.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;


public class DrawUtil {

    public static void drawBorder(GuiGraphicsExtractor gui, int x, int y, int width, int height, int color) {
        gui.fill(x, y, x + width, y + 1, color);
        gui.fill(x, y + height - 1, x + width, y + height, color);
        gui.fill(x, y, x + 1, y + height, color);
        gui.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawCircle(GuiGraphicsExtractor gui, int x, int y, int radius, int color) {
        for (int i = -radius; i <= radius; i++)
            for (int j = -radius; j <= radius; j++)
                if (i * i + j * j <= radius * radius)
                    gui.fill(x + i, y + j, x + i + 1, y + j + 1, color);
    }

    public static void drawCheckerBoard(GuiGraphicsExtractor gui, int x, int y, int width, int height, int checkerSize, int color1, int color2) {
        for (int ax = 0; ax < width - 4; ax += checkerSize * 2) {
            for (int ay = 0; ay < height; ay += checkerSize * 2) {
                gui.fill(x + ax, y + ay,
                        x + ax + checkerSize, y + ay + checkerSize, color1);

                if (ax + checkerSize < width - 4 && ay + checkerSize < height) {
                    gui.fill(x + ax + checkerSize, y + ay + checkerSize,
                            x + ax + checkerSize * 2, y + ay + checkerSize * 2, color1);
                }
            }
        }

        for (int ax = checkerSize; ax < width - 4; ax += checkerSize * 2) {
            for (int ay = 0; ay < height; ay += checkerSize * 2) {
                gui.fill(x + ax, y + ay,
                        x + ax + checkerSize, y + ay + checkerSize, color2);
            }
        }

        for (int ax = 0; ax < width - 4; ax += checkerSize * 2) {
            for (int ay = checkerSize; ay < height; ay += checkerSize * 2) {
                gui.fill(x + ax, y + ay,
                        x + ax + checkerSize, y + ay + checkerSize, color2);
            }
        }
    }
}