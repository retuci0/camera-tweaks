package me.retucio.camtweaks.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

// literalmente robado de https://github.com/mioclient/oyvey-ported/ (perdón)
public class RenderUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Tessellator tesselator = Tessellator.getInstance();

    public static void drawOutlineBox(MatrixStack stack, Box box, Color color, double lineWidth) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);

        VertexRendering.drawBox(stack.peek(), buffer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);

        Layers.getGlobalLines(lineWidth).draw(buffer.end());
    }

    public static void drawFilledBox(MatrixStack matrices, Box box, Color color) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(color.getRGB());

        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(color.getRGB());

        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(color.getRGB());

        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(color.getRGB());

        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(color.getRGB());

        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(color.getRGB());
        buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(color.getRGB());

        Layers.getGlobalQuads().draw(buffer.end());
    }
}
