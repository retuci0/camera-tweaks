package me.retucio.sputnik.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Vector3f;

import java.awt.*;

// literalmente robado de https://github.com/mioclient/oyvey-ported/ (perdón)
public class RenderUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Tessellator tessellator = Tessellator.getInstance();


    /* líneas */

    public static void drawLine(MatrixStack matrices, Vec3d from, Vec3d to, Color color, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
        Vec3d cam = mc.gameRenderer.getCamera().getCameraPos();

        buffer.vertex(matrices.peek(), (float) (from.x - cam.x), (float) (from.y - cam.y), (float) (from.z - cam.z)).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), (float) (to.x - cam.x), (float) (to.y - cam.y), (float) (to.z - cam.z)).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        Layers.lines().draw(buffer.end());
    }

    // https://github.com/TheF1xer/GateClient-1.12.2/blob/main/src/main/java/me/thef1xer/gateclient/util/RenderUtil.java
    public static void drawTracer(MatrixStack matrices, Vec3d pos, Color color, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
        Camera cam = mc.gameRenderer.getCamera();

        Vector3f cameraVector = new Vector3f(0, 0, 1)
                .rotateX((float) Math.toRadians(cam.getPitch()))
                .rotateY((float) -Math.toRadians(cam.getYaw()));

        buffer.vertex(matrices.peek(), cameraVector.x, cameraVector.y, cameraVector.z).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), (float) (pos.x - cam.getCameraPos().x), (float) (pos.y - cam.getCameraPos().y), (float) (pos.z - cam.getCameraPos().z)).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        Layers.lines().draw(buffer.end());
    }

    public static void drawVector(MatrixStack matrices, Vector3f start, Vec3d direction, Color c, float lineWidth) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
        MatrixStack.Entry pose = matrices.peek();

        int color = c.getRGB();
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = ((color >>  0) & 0xFF) / 255f;

        float endX = start.x() + (float) direction.x;
        float endY = start.y() + (float) direction.y;
        float endZ = start.z() + (float) direction.z;

        buffer.vertex(pose.getPositionMatrix(), start.x(), start.y(), start.z())
                .color(r, g, b, a)
                .normal(pose, (float) direction.x, (float) direction.y, (float) direction.z)
                .lineWidth(lineWidth);

        buffer.vertex(pose.getPositionMatrix(), endX, endY, endZ)
                .color(r, g, b, a)
                .normal(pose, (float) direction.x, (float) direction.y, (float) direction.z)
                .lineWidth(lineWidth);

        Layers.lines().draw(buffer.end());
    }


    /* caras */

    public static void drawFilledRect(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        matrices.push();
        matrices.translate(0, 0, 0);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x, y, 0).color(r, g, b, a);

        Layers.quads().draw(buffer.end());
        matrices.pop();
    }

    public static void drawBlockFaceOutlines(MatrixStack matrices, BlockPos pos, Direction face, Color color, float lineWidth, boolean cull) {
        if (lineWidth <= 0) return;
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        float minX = (float) (pos.getX() - cameraPos.x);
        float minY = (float) (pos.getY() - cameraPos.y);
        float minZ = (float) (pos.getZ() - cameraPos.z);
        float maxX = minX + 1;
        float maxY = minY + 1;
        float maxZ = minZ + 1;

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);

        switch (face) {
            case UP:
                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;

            case DOWN:
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;

            case EAST:
                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;

            case WEST:
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;

            case NORTH:
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;

            case SOUTH:
                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
                buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

                break;
        }

        if (cull) Layers.linesCull().draw(buffer.end());
        else Layers.lines().draw(buffer.end());
    }

    // expand es para evitar z-fighting
    public static void drawBlockFaceFilled(MatrixStack matrices, BlockPos pos, Direction face, Color color, float expand, boolean cull) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

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

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

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

        if (cull) Layers.quadsCull().draw(buffer.end());
        else Layers.quads().draw(buffer.end());
    }


    public static void drawTriangle(MatrixStack matrices, float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrices.peek().getPositionMatrix(), x3, y3, 0).color(r, g, b, a);
        Layers.quads().draw(buffer.end());
    }

    // cajas

    public static void drawOutlineBox(MatrixStack matrices, Box box, Color color, float lineWidth, boolean cull) {
        if (lineWidth <= 0) return;
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);

        // parte inferior
        buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        // parte superior
        buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        // líneas verticales
        buffer.vertex(matrices.peek(), minX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, minY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, maxY, minZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), maxX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), maxX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        buffer.vertex(matrices.peek(), minX, minY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);
        buffer.vertex(matrices.peek(), minX, maxY, maxZ).color(color.getRGB()).normal(-1, -1, -1).lineWidth(lineWidth);

        if (cull) Layers.linesCull().draw(buffer.end());
        else Layers.lines().draw(buffer.end());
    }

    public static void drawFilledBox(MatrixStack matrices, Box box, Color color, boolean cull) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        float minX = (float) (box.minX - cameraPos.x);
        float minY = (float) (box.minY - cameraPos.y);
        float minZ = (float) (box.minZ - cameraPos.z);
        float maxX = (float) (box.maxX - cameraPos.x);
        float maxY = (float) (box.maxY - cameraPos.y);
        float maxZ = (float) (box.maxZ - cameraPos.z);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
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

        if (cull) Layers.quadsCull().draw(buffer.end());
        else Layers.quads().draw(buffer.end());
    }


    // formas custom

    public static void drawVoxelShapeOutline(MatrixStack matrices, VoxelShape voxelShape, BlockPos blockPos, Color color, float lineWidth, boolean cull) {
        if (voxelShape.isEmpty()) return;

        voxelShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(
                    blockPos.getX() + minX, blockPos.getY() + minY, blockPos.getZ() + minZ,
                    blockPos.getX() + maxX, blockPos.getY() + maxY, blockPos.getZ() + maxZ
            );

            drawOutlineBox(matrices, box, color, lineWidth, cull);
        });
    }

    public static void drawVoxelShapeFilled(MatrixStack matrices, VoxelShape voxelShape, BlockPos pos, Color color, boolean cull) {
        if (voxelShape.isEmpty()) return;

        voxelShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );

            drawFilledBox(matrices, box, color, cull);
        });
    }


    // bloques

    public static void drawBlockOutline(MatrixStack matrices, BlockPos pos, Color color, float lineWidth, boolean cull) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawOutlineBox(matrices, box, color, lineWidth, cull);
    }

    public static void drawBlockFilled(MatrixStack matrices, BlockPos pos, Color color, boolean cull) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        drawFilledBox(matrices, box, color, cull);
    }




    public static MatrixStack matrixFrom(Vec3d pos) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getCameraPos();

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getPitch() + 180.0F));

        matrices.translate(
                pos.getX() - camPos.getX(),
                pos.getY() - camPos.getY(),
                pos.getZ() - camPos.getZ()
        );

        return matrices;
    }
}
