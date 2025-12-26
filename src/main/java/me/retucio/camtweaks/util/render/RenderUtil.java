package me.retucio.camtweaks.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

// literalmente robado de https://github.com/mioclient/oyvey-ported/ (perdón)
public class RenderUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Tessellator tesselator = Tessellator.getInstance();

    public static void drawBlockOutline(MatrixStack matrices, BlockPos pos, Color color, double lineWidth) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawOutlineBox(matrices, box, color, lineWidth);
    }

    public static void drawBlockFilled(MatrixStack matrices, BlockPos pos, Color color) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawFilledBox(matrices, box, color);
    }

    // expand es para evitar z-fighting
    public static void drawBlockFaceFilled(MatrixStack matrices, BlockPos pos, Direction face, Color color, float expand) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        float minX = (float) (pos.getX() - cameraPos.x);
        float minY = (float) (pos.getY() - cameraPos.y);
        float minZ = (float) (pos.getZ() - cameraPos.z);
        float maxX = minX + 1;
        float maxY = minY + 1;
        float maxZ = minZ + 1;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        if (expand != 0) {
            switch (face) {
                case DOWN: minY -= expand; maxY = minY + 0.001f; break;
                case UP: minY = maxY; maxY += expand; break;
                case NORTH: minZ -= expand; maxZ = minZ + 0.001f; break;
                case SOUTH: minZ = maxZ; maxZ += expand; break;
                case WEST: minX -= expand; maxX = minX + 0.001f; break;
                case EAST: minX = maxX; maxX += expand; break;
            }
        }

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        switch (face) {
            case DOWN:
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(r, g, b, a);
                break;

            case UP:
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(r, g, b, a);
                break;

            case NORTH:
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(r, g, b, a);
                break;

            case SOUTH:
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(r, g, b, a);
                break;

            case WEST:
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, minY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), minX, maxY, minZ).color(r, g, b, a);
                break;

            case EAST:
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, minZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, maxY, maxZ).color(r, g, b, a);
                buffer.vertex(matrices.peek().getPositionMatrix(), maxX, minY, maxZ).color(r, g, b, a);
                break;
        }

        Layers.getGlobalQuads().draw(buffer.end());
    }

    public static void drawLine(MatrixStack matrices, Vec3d start, Vec3d end, Color color, float width) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        float x1 = (float) (start.x - cameraPos.x);
        float y1 = (float) (start.y - cameraPos.y);
        float z1 = (float) (start.z - cameraPos.z);
        float x2 = (float) (end.x - cameraPos.x);
        float y2 = (float) (end.y - cameraPos.y);
        float z2 = (float) (end.z - cameraPos.z);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y2, z2).color(r, g, b, a);

        Layers.getGlobalLines(width).draw(buffer.end());
    }

    public static void drawOutlineRect(MatrixStack matrices, float x, float y, float w, float h, Color color, int linewidth) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        matrices.push();
        matrices.translate(0, 0, 0);

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getPositionMatrix(), x, y, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + w, y, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + w, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x, y, 0).color(r, g, b, a);

        Layers.getGlobalLines(linewidth).draw(buffer.end());
        matrices.pop();
    }

    public static void drawFilledRect(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        matrices.push();
        matrices.translate(0, 0, 0);

        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x, y, 0).color(r, g, b, a);

        Layers.getGlobalQuads().draw(buffer.end());
        matrices.pop();
    }

    public static void drawOutlineBox(MatrixStack matrices, Box box, Color color, double lineWidth) {
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

        VertexRendering.drawBox(matrices.peek(), buffer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);

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
